package org.assistant.tools.doc.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import org.assistant.tools.doc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Parses JAX-RS (Jakarta / javax.ws.rs) annotations to extract API definitions.
 * <p>
 * Supported annotations:
 * <ul>
 * <li>Class-level: {@code @Path}, {@code @Consumes}, {@code @Produces}</li>
 * <li>Method-level: {@code @GET}, {@code @POST}, {@code @PUT}, {@code @DELETE},
 * {@code @PATCH}, {@code @HEAD}, {@code @OPTIONS}, {@code @Path}</li>
 * <li>Parameter-level: {@code @QueryParam}, {@code @PathParam},
 * {@code @HeaderParam},
 * {@code @FormParam}, {@code @CookieParam}, {@code @BeanParam},
 * {@code @DefaultValue}</li>
 * </ul>
 */
public class JaxRsApiParser implements FrameworkApiParser {

    private static final Logger log = LoggerFactory.getLogger(JaxRsApiParser.class);

    /** HTTP method annotation names */
    private static final Set<String> HTTP_METHOD_ANNOTATIONS = Set.of(
            "GET", "POST", "PUT", "DELETE", "PATCH", "HEAD", "OPTIONS");

    /** Parameter annotation name → ParamLocation */
    private static final Map<String, ParamLocation> PARAM_ANNOTATIONS = new LinkedHashMap<>();

    static {
        PARAM_ANNOTATIONS.put("QueryParam", ParamLocation.QUERY);
        PARAM_ANNOTATIONS.put("PathParam", ParamLocation.PATH);
        PARAM_ANNOTATIONS.put("HeaderParam", ParamLocation.HEADER);
        PARAM_ANNOTATIONS.put("FormParam", ParamLocation.FORM);
        PARAM_ANNOTATIONS.put("CookieParam", ParamLocation.COOKIE);
    }

    @Override
    public String getFrameworkName() {
        return "JAX-RS";
    }

    @Override
    public boolean supports(CompilationUnit cu) {
        for (var type : cu.findAll(ClassOrInterfaceDeclaration.class)) {
            // A JAX-RS resource typically has @Path on the class
            if (type.getAnnotationByName("Path").isPresent()) {
                return true;
            }
            // Or methods with HTTP method annotations
            for (MethodDeclaration method : type.getMethods()) {
                for (var ann : method.getAnnotations()) {
                    if (HTTP_METHOD_ANNOTATIONS.contains(ann.getNameAsString())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<ApiGroup> parse(CompilationUnit cu) {
        return parse(cu, null);
    }

    @Override
    public List<ApiGroup> parse(CompilationUnit cu, TypeResolver typeResolver) {
        List<ApiGroup> groups = new ArrayList<>();

        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ClassOrInterfaceDeclaration clazz, Void arg) {
                ApiGroup group = parseClass(cu, clazz);
                if (group != null && !group.getApis().isEmpty()) {
                    // Resolve parameter and return type fields
                    if (typeResolver != null) {
                        for (WebApiInfo api : group.getApis()) {
                            resolveFields(api, typeResolver);
                        }
                    }
                    groups.add(group);
                }
                super.visit(clazz, arg);
            }
        }, null);

        return groups;
    }

    private void resolveFields(WebApiInfo api, TypeResolver typeResolver) {
        for (ApiParam param : api.getParams()) {
            List<FieldInfo> fields = typeResolver.resolve(param.getDataType());
            if (!fields.isEmpty()) {
                param.setFields(fields);
            }
        }
        if (api.getReturnType() != null) {
            List<FieldInfo> returnFields = typeResolver.resolve(api.getReturnType());
            if (!returnFields.isEmpty()) {
                api.setReturnTypeFields(returnFields);
            }
        }
    }

    private ApiGroup parseClass(CompilationUnit cu, ClassOrInterfaceDeclaration clazz) {
        // Check if this class has any JAX-RS annotations
        String classPath = extractPathAnnotationValue(clazz.getAnnotations());
        boolean hasHttpMethods = clazz.getMethods().stream()
                .flatMap(m -> m.getAnnotations().stream())
                .anyMatch(a -> HTTP_METHOD_ANNOTATIONS.contains(a.getNameAsString()));

        if (classPath == null && !hasHttpMethods) {
            return null;
        }

        ApiGroup group = new ApiGroup();
        group.setName(clazz.getNameAsString());
        group.setControllerClass(getFullyQualifiedName(cu, clazz));
        group.setBasePath(classPath != null ? classPath : "");
        group.setDescription(extractJavadocDescription(clazz));

        // Class-level @Consumes/@Produces
        List<String> classConsumes = extractMediaTypes(clazz, "Consumes");
        List<String> classProduces = extractMediaTypes(clazz, "Produces");

        for (MethodDeclaration method : clazz.getMethods()) {
            WebApiInfo api = parseMethod(method, group.getBasePath(), group.getControllerClass(),
                    classConsumes, classProduces);
            if (api != null) {
                group.addApi(api);
            }
        }

        return group;
    }

    private WebApiInfo parseMethod(MethodDeclaration method, String basePath, String controllerClass,
            List<String> classConsumes, List<String> classProduces) {
        // Find HTTP method annotation
        String httpMethod = null;
        for (var ann : method.getAnnotations()) {
            if (HTTP_METHOD_ANNOTATIONS.contains(ann.getNameAsString())) {
                httpMethod = ann.getNameAsString();
                break;
            }
        }

        if (httpMethod == null) {
            return null; // Not an endpoint
        }

        // Get method-level path
        String methodPath = extractPathAnnotationValue(method.getAnnotations());
        String fullPath = combinePaths(basePath, methodPath != null ? methodPath : "");

        WebApiInfo api = new WebApiInfo();
        api.setMethod(httpMethod);
        api.setPath(fullPath);
        api.setMethodName(method.getNameAsString());
        api.setControllerClass(controllerClass);
        api.setReturnType(method.getTypeAsString());
        api.setDeprecated(method.getAnnotationByName("Deprecated").isPresent());

        // Method-level consumes/produces override class-level
        List<String> methodConsumes = extractMediaTypes(method, "Consumes");
        List<String> methodProduces = extractMediaTypes(method, "Produces");
        api.setConsumes(methodConsumes.isEmpty() ? classConsumes : methodConsumes);
        api.setProduces(methodProduces.isEmpty() ? classProduces : methodProduces);

        // Extract Javadoc
        Map<String, String> paramDocs = new HashMap<>();
        if (method.getJavadoc().isPresent()) {
            Javadoc javadoc = method.getJavadoc().get();
            api.setSummary(javadoc.getDescription().toText().trim().split("\n")[0]);
            api.setDescription(javadoc.getDescription().toText().trim());

            for (JavadocBlockTag tag : javadoc.getBlockTags()) {
                if (tag.getType() == JavadocBlockTag.Type.PARAM) {
                    tag.getName().ifPresent(name -> paramDocs.put(name, tag.getContent().toText().trim()));
                }
            }
        }

        // Parse parameters
        for (Parameter param : method.getParameters()) {
            ApiParam apiParam = parseParameter(param, paramDocs);
            api.addParam(apiParam);
        }

        api.setTags(List.of(controllerClass.substring(controllerClass.lastIndexOf('.') + 1)));

        return api;
    }

    private ApiParam parseParameter(Parameter param, Map<String, String> paramDocs) {
        ApiParam apiParam = new ApiParam();
        apiParam.setName(param.getNameAsString());
        apiParam.setDataType(param.getTypeAsString());
        apiParam.setDescription(paramDocs.getOrDefault(param.getNameAsString(), ""));

        // Check for @DefaultValue
        for (var ann : param.getAnnotations()) {
            if ("DefaultValue".equals(ann.getNameAsString())) {
                if (ann.isSingleMemberAnnotationExpr()) {
                    apiParam.setDefaultValue(extractStringValue(ann.asSingleMemberAnnotationExpr().getMemberValue()));
                }
            }
        }

        // Determine location from JAX-RS param annotations
        boolean annotated = false;
        for (var ann : param.getAnnotations()) {
            String annName = ann.getNameAsString();
            ParamLocation location = PARAM_ANNOTATIONS.get(annName);
            if (location != null) {
                apiParam.setIn(location);
                annotated = true;

                // Extract the value attribute (param name in URL)
                if (ann.isSingleMemberAnnotationExpr()) {
                    String value = extractStringValue(ann.asSingleMemberAnnotationExpr().getMemberValue());
                    if (value != null && !value.isEmpty()) {
                        apiParam.setName(value);
                    }
                }

                if (location == ParamLocation.PATH) {
                    apiParam.setRequired(true);
                }
                break;
            }

            // @BeanParam → body
            if ("BeanParam".equals(annName)) {
                apiParam.setIn(ParamLocation.BODY);
                annotated = true;
                break;
            }
        }

        if (!annotated) {
            // Un-annotated params in JAX-RS are entity body params
            apiParam.setIn(ParamLocation.BODY);
        }

        return apiParam;
    }

    // --- Utility methods ---

    private String extractPathAnnotationValue(List<AnnotationExpr> annotations) {
        for (AnnotationExpr ann : annotations) {
            if (!"Path".equals(ann.getNameAsString())) {
                continue;
            }
            if (ann.isSingleMemberAnnotationExpr()) {
                return extractStringValue(ann.asSingleMemberAnnotationExpr().getMemberValue());
            }
            if (ann.isNormalAnnotationExpr()) {
                for (MemberValuePair pair : ann.asNormalAnnotationExpr().getPairs()) {
                    if ("value".equals(pair.getNameAsString())) {
                        return extractStringValue(pair.getValue());
                    }
                }
            }
            return ""; // @Path with no value
        }
        return null;
    }

    private List<String> extractMediaTypes(Object annotated, String annotationName) {
        List<AnnotationExpr> annotations;
        if (annotated instanceof ClassOrInterfaceDeclaration) {
            annotations = ((ClassOrInterfaceDeclaration) annotated).getAnnotations();
        } else if (annotated instanceof MethodDeclaration) {
            annotations = ((MethodDeclaration) annotated).getAnnotations();
        } else {
            return Collections.emptyList();
        }

        for (AnnotationExpr ann : annotations) {
            if (!annotationName.equals(ann.getNameAsString())) {
                continue;
            }
            List<String> types = new ArrayList<>();
            if (ann.isSingleMemberAnnotationExpr()) {
                var memberValue = ann.asSingleMemberAnnotationExpr().getMemberValue();
                if (memberValue.isArrayInitializerExpr()) {
                    memberValue.asArrayInitializerExpr().getValues()
                            .forEach(e -> types.add(extractStringValue(e)));
                } else {
                    types.add(extractStringValue(memberValue));
                }
            }
            return types;
        }
        return Collections.emptyList();
    }

    private String extractStringValue(com.github.javaparser.ast.expr.Expression expr) {
        if (expr == null)
            return "";
        if (expr.isStringLiteralExpr()) {
            return expr.asStringLiteralExpr().asString();
        }
        // Handle MediaType.APPLICATION_JSON etc.
        return expr.toString().replace("\"", "");
    }

    private String combinePaths(String base, String path) {
        if (base == null)
            base = "";
        if (path == null)
            path = "";
        if (!base.isEmpty() && !base.startsWith("/"))
            base = "/" + base;
        if (!path.isEmpty() && !path.startsWith("/"))
            path = "/" + path;
        String combined = base + path;
        return combined.isEmpty() ? "/" : combined;
    }

    private String getFullyQualifiedName(CompilationUnit cu, ClassOrInterfaceDeclaration clazz) {
        return cu.getPackageDeclaration()
                .map(pkg -> pkg.getNameAsString() + "." + clazz.getNameAsString())
                .orElse(clazz.getNameAsString());
    }

    private String extractJavadocDescription(ClassOrInterfaceDeclaration clazz) {
        return clazz.getJavadoc()
                .map(jd -> jd.getDescription().toText().trim())
                .orElse("");
    }

}
