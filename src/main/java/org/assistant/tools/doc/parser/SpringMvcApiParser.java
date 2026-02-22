package org.assistant.tools.doc.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.javadoc.Javadoc;
import com.github.javaparser.javadoc.JavadocBlockTag;
import org.assistant.tools.doc.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import org.assistant.tools.doc.FieldInfo;

/**
 * Parses Spring MVC annotations to extract API definitions.
 * <p>
 * Supported annotations:
 * <ul>
 * <li>Class-level: {@code @RestController}, {@code @Controller},
 * {@code @RequestMapping}</li>
 * <li>Method-level: {@code @GetMapping}, {@code @PostMapping},
 * {@code @PutMapping},
 * {@code @DeleteMapping}, {@code @PatchMapping}, {@code @RequestMapping}</li>
 * <li>Parameter-level: {@code @RequestParam}, {@code @PathVariable},
 * {@code @RequestBody},
 * {@code @RequestHeader}, {@code @CookieValue}</li>
 * </ul>
 */
public class SpringMvcApiParser implements FrameworkApiParser {

    private static final Logger log = LoggerFactory.getLogger(SpringMvcApiParser.class);

    /** Method-level annotation name → HTTP method */
    private static final Map<String, String> METHOD_MAPPING_ANNOTATIONS = new LinkedHashMap<>();

    static {
        METHOD_MAPPING_ANNOTATIONS.put("GetMapping", "GET");
        METHOD_MAPPING_ANNOTATIONS.put("PostMapping", "POST");
        METHOD_MAPPING_ANNOTATIONS.put("PutMapping", "PUT");
        METHOD_MAPPING_ANNOTATIONS.put("DeleteMapping", "DELETE");
        METHOD_MAPPING_ANNOTATIONS.put("PatchMapping", "PATCH");
    }

    /** Parameter annotation name → ParamLocation */
    private static final Map<String, ParamLocation> PARAM_ANNOTATIONS = new LinkedHashMap<>();

    static {
        PARAM_ANNOTATIONS.put("RequestParam", ParamLocation.QUERY);
        PARAM_ANNOTATIONS.put("PathVariable", ParamLocation.PATH);
        PARAM_ANNOTATIONS.put("RequestBody", ParamLocation.BODY);
        PARAM_ANNOTATIONS.put("RequestHeader", ParamLocation.HEADER);
        PARAM_ANNOTATIONS.put("CookieValue", ParamLocation.COOKIE);
    }

    private static final Set<String> CONTROLLER_ANNOTATIONS = Set.of(
            "RestController", "Controller");

    @Override
    public String getFrameworkName() {
        return "Spring MVC";
    }

    @Override
    public boolean supports(CompilationUnit cu) {
        for (var type : cu.findAll(ClassOrInterfaceDeclaration.class)) {
            for (var ann : type.getAnnotations()) {
                String name = ann.getNameAsString();
                if (CONTROLLER_ANNOTATIONS.contains(name)) {
                    return true;
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
                if (!isController(clazz)) {
                    super.visit(clazz, arg);
                    return;
                }

                ApiGroup group = new ApiGroup();
                group.setName(clazz.getNameAsString());
                group.setControllerClass(getFullyQualifiedName(cu, clazz));
                group.setBasePath(getClassLevelPath(clazz));
                group.setDescription(extractJavadocDescription(clazz));

                for (MethodDeclaration method : clazz.getMethods()) {
                    List<WebApiInfo> apis = parseMethod(method, group.getBasePath(), group.getControllerClass());
                    // Resolve parameter and return type fields
                    if (typeResolver != null) {
                        for (WebApiInfo api : apis) {
                            resolveFields(api, typeResolver);
                        }
                    }
                    apis.forEach(group::addApi);
                }

                if (!group.getApis().isEmpty()) {
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
        // Also resolve return type fields
        if (api.getReturnType() != null) {
            List<FieldInfo> returnFields = typeResolver.resolve(api.getReturnType());
            if (!returnFields.isEmpty()) {
                api.setReturnTypeFields(returnFields);
            }
        }
    }

    private boolean isController(ClassOrInterfaceDeclaration clazz) {
        for (var ann : clazz.getAnnotations()) {
            if (CONTROLLER_ANNOTATIONS.contains(ann.getNameAsString())) {
                return true;
            }
        }
        return false;
    }

    private String getClassLevelPath(ClassOrInterfaceDeclaration clazz) {
        for (var ann : clazz.getAnnotations()) {
            if ("RequestMapping".equals(ann.getNameAsString())) {
                return extractPathFromAnnotation(ann);
            }
        }
        return "";
    }

    private List<WebApiInfo> parseMethod(MethodDeclaration method, String basePath, String controllerClass) {
        List<WebApiInfo> results = new ArrayList<>();

        for (var ann : method.getAnnotations()) {
            String annName = ann.getNameAsString();

            String httpMethod = null;
            if ("RequestMapping".equals(annName)) {
                httpMethod = extractMethodFromRequestMapping(ann);
            } else if (METHOD_MAPPING_ANNOTATIONS.containsKey(annName)) {
                httpMethod = METHOD_MAPPING_ANNOTATIONS.get(annName);
            }

            if (httpMethod == null) {
                continue;
            }

            String methodPath = extractPathFromAnnotation(ann);
            String fullPath = combinePaths(basePath, methodPath);

            WebApiInfo api = new WebApiInfo();
            api.setMethod(httpMethod);
            api.setPath(fullPath);
            api.setMethodName(method.getNameAsString());
            api.setControllerClass(controllerClass);
            api.setReturnType(method.getTypeAsString());
            api.setDeprecated(method.getAnnotationByName("Deprecated").isPresent());

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

            // Extract consumes/produces
            extractConsumesProduces(ann, api);

            // Parse parameters
            for (Parameter param : method.getParameters()) {
                ApiParam apiParam = parseParameter(param, paramDocs);
                api.addParam(apiParam);
            }

            api.setTags(List.of(controllerClass.substring(controllerClass.lastIndexOf('.') + 1)));

            results.add(api);
        }

        return results;
    }

    private ApiParam parseParameter(Parameter param, Map<String, String> paramDocs) {
        ApiParam apiParam = new ApiParam();
        apiParam.setName(param.getNameAsString());
        apiParam.setDataType(param.getTypeAsString());
        apiParam.setDescription(paramDocs.getOrDefault(param.getNameAsString(), ""));

        // Determine location from annotations
        boolean annotated = false;
        for (var ann : param.getAnnotations()) {
            String annName = ann.getNameAsString();
            ParamLocation location = PARAM_ANNOTATIONS.get(annName);
            if (location != null) {
                apiParam.setIn(location);
                annotated = true;

                // Extract attribute values
                if (ann.isNormalAnnotationExpr()) {
                    NormalAnnotationExpr normal = ann.asNormalAnnotationExpr();
                    for (MemberValuePair pair : normal.getPairs()) {
                        switch (pair.getNameAsString()) {
                            case "value", "name" -> apiParam.setName(extractStringValue(pair.getValue()));
                            case "required" -> apiParam.setRequired(extractBooleanValue(pair.getValue()));
                            case "defaultValue" -> apiParam.setDefaultValue(extractStringValue(pair.getValue()));
                        }
                    }
                } else if (ann.isSingleMemberAnnotationExpr()) {
                    // @RequestParam("name")
                    String value = extractStringValue(ann.asSingleMemberAnnotationExpr().getMemberValue());
                    if (value != null && !value.isEmpty()) {
                        apiParam.setName(value);
                    }
                }

                // @PathVariable is always required unless specified
                if (location == ParamLocation.PATH) {
                    apiParam.setRequired(true);
                }

                break;
            }
        }

        if (!annotated) {
            // Unannotated params in Spring are treated as query params by default
            apiParam.setIn(ParamLocation.QUERY);
        }

        return apiParam;
    }

    private void extractConsumesProduces(AnnotationExpr ann, WebApiInfo api) {
        if (!ann.isNormalAnnotationExpr()) {
            return;
        }
        for (MemberValuePair pair : ann.asNormalAnnotationExpr().getPairs()) {
            String key = pair.getNameAsString();
            if ("consumes".equals(key)) {
                api.setConsumes(extractStringListValue(pair.getValue()));
            } else if ("produces".equals(key)) {
                api.setProduces(extractStringListValue(pair.getValue()));
            }
        }
    }

    private String extractMethodFromRequestMapping(AnnotationExpr ann) {
        if (ann.isNormalAnnotationExpr()) {
            for (MemberValuePair pair : ann.asNormalAnnotationExpr().getPairs()) {
                if ("method".equals(pair.getNameAsString())) {
                    String val = pair.getValue().toString();
                    // RequestMethod.GET → GET
                    if (val.contains(".")) {
                        val = val.substring(val.lastIndexOf('.') + 1);
                    }
                    return val;
                }
            }
        }
        // No method specified → defaults to all (we record as GET)
        return "GET";
    }

    /**
     * Extract path value from a mapping annotation.
     */
    private String extractPathFromAnnotation(AnnotationExpr ann) {
        if (ann.isSingleMemberAnnotationExpr()) {
            return extractStringValue(ann.asSingleMemberAnnotationExpr().getMemberValue());
        }
        if (ann.isNormalAnnotationExpr()) {
            for (MemberValuePair pair : ann.asNormalAnnotationExpr().getPairs()) {
                String name = pair.getNameAsString();
                if ("value".equals(name) || "path".equals(name)) {
                    Expression value = pair.getValue();
                    if (value.isArrayInitializerExpr()) {
                        // Multiple paths — take the first
                        NodeList<Expression> values = value.asArrayInitializerExpr().getValues();
                        if (!values.isEmpty()) {
                            return extractStringValue(values.get(0));
                        }
                    }
                    return extractStringValue(value);
                }
            }
        }
        return "";
    }

    private String extractStringValue(Expression expr) {
        if (expr == null)
            return "";
        if (expr.isStringLiteralExpr()) {
            return expr.asStringLiteralExpr().asString();
        }
        // Handle string concatenation or field access as raw string
        return expr.toString().replace("\"", "");
    }

    private boolean extractBooleanValue(Expression expr) {
        if (expr == null)
            return false;
        if (expr.isBooleanLiteralExpr()) {
            return expr.asBooleanLiteralExpr().getValue();
        }
        return Boolean.parseBoolean(expr.toString());
    }

    private List<String> extractStringListValue(Expression expr) {
        List<String> result = new ArrayList<>();
        if (expr.isArrayInitializerExpr()) {
            for (Expression e : expr.asArrayInitializerExpr().getValues()) {
                result.add(extractStringValue(e));
            }
        } else {
            result.add(extractStringValue(expr));
        }
        return result;
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
