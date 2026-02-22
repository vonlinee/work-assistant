package org.assistant.tools.doc.parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import org.assistant.tools.doc.FieldInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * Resolves complex Java types (DTOs/POJOs) by finding their source files
 * in the project source tree and extracting field information.
 *
 * <h3>How it works</h3>
 * <ol>
 * <li>Builds an index of all Java files → fully qualified class names on first
 * use</li>
 * <li>When asked to resolve a type, looks up the class in the index</li>
 * <li>Parses the source file and extracts field declarations with Javadoc</li>
 * <li>Recursively resolves nested complex types (with depth limit)</li>
 * </ol>
 *
 * <h3>Extensibility</h3>
 * <p>
 * Override {@link #isComplexType(String)} to customize which types are
 * resolved.
 * Override {@link #extractFieldAnnotations(FieldDeclaration, FieldInfo)} to
 * support
 * additional validation/documentation annotations.
 * </p>
 */
public class TypeResolver {

    private static final Logger log = LoggerFactory.getLogger(TypeResolver.class);

    /** Simple/primitive types that should NOT be resolved */
    private static final Set<String> SIMPLE_TYPES = Set.of(
            "String", "Integer", "int", "Long", "long", "Short", "short",
            "Double", "double", "Float", "float", "Boolean", "boolean",
            "Byte", "byte", "Character", "char", "BigDecimal", "BigInteger",
            "Date", "LocalDate", "LocalDateTime", "LocalTime", "Instant",
            "ZonedDateTime", "OffsetDateTime", "Timestamp",
            "Object", "Void", "void", "Map", "HashMap", "LinkedHashMap",
            "MultipartFile", "InputStream", "OutputStream",
            "HttpServletRequest", "HttpServletResponse",
            "ResponseEntity", "ModelAndView");

    /**
     * Wrapper/collection types whose generic argument should be resolved instead
     */
    private static final Set<String> WRAPPER_TYPES = Set.of(
            "List", "ArrayList", "LinkedList",
            "Set", "HashSet", "LinkedHashSet", "TreeSet",
            "Collection", "Iterable",
            "Optional",
            "ResponseEntity", "Result", "ApiResponse", "BaseResponse",
            "Page", "PageInfo");

    /** Max recursion depth for nested type resolution */
    private static final int MAX_DEPTH = 3;

    private final List<Path> sourceRoots;
    private final JavaParser javaParser;

    /** Cache: simple class name → list of source file paths */
    private Map<String, List<Path>> classIndex;

    /** Cache: fully qualified class name → parsed fields */
    private final Map<String, List<FieldInfo>> resolvedCache = new HashMap<>();

    /** Track types being resolved to prevent infinite recursion */
    private final Set<String> resolving = new HashSet<>();

    public TypeResolver(List<Path> sourceRoots) {
        this.sourceRoots = sourceRoots;
        this.javaParser = new JavaParser();
    }

    /**
     * Resolve a Java type name to its field info.
     * Returns empty list for simple/primitive types.
     *
     * @param typeName the Java type (may include generics, e.g. "List<UserDto>")
     * @return list of fields, empty if not resolvable or simple type
     */
    public List<FieldInfo> resolve(String typeName) {
        return resolve(typeName, 0);
    }

    private List<FieldInfo> resolve(String typeName, int depth) {
        if (typeName == null || typeName.isEmpty() || depth > MAX_DEPTH) {
            return Collections.emptyList();
        }

        // Strip generics wrapper: List<UserDto> → UserDto
        String innerType = unwrapType(typeName);

        if (!isComplexType(innerType)) {
            return Collections.emptyList();
        }

        if (resolvedCache.containsKey(innerType)) {
            return resolvedCache.get(innerType);
        }

        // Prevent infinite recursion for circular references
        if (resolving.contains(innerType)) {
            return Collections.emptyList();
        }
        resolving.add(innerType);

        try {
            List<FieldInfo> fields = resolveFromSource(innerType, depth);
            resolvedCache.put(innerType, fields);
            return fields;
        } finally {
            resolving.remove(innerType);
        }
    }

    private List<FieldInfo> resolveFromSource(String simpleTypeName, int depth) {
        ensureIndexBuilt();

        List<Path> candidates = classIndex.getOrDefault(simpleTypeName, Collections.emptyList());
        if (candidates.isEmpty()) {
            return Collections.emptyList();
        }

        // Try each candidate (may be multiple files with the same class name)
        for (Path sourcePath : candidates) {
            List<FieldInfo> fields = parseClassFields(sourcePath, simpleTypeName, depth);
            if (!fields.isEmpty()) {
                return fields;
            }
        }

        return Collections.emptyList();
    }

    private List<FieldInfo> parseClassFields(Path sourcePath, String className, int depth) {
        try {
            ParseResult<CompilationUnit> result = javaParser.parse(sourcePath);
            if (!result.isSuccessful() || result.getResult().isEmpty()) {
                return Collections.emptyList();
            }

            CompilationUnit cu = result.getResult().get();

            // Find the class declaration
            for (ClassOrInterfaceDeclaration clazz : cu.findAll(ClassOrInterfaceDeclaration.class)) {
                if (clazz.getNameAsString().equals(className)) {
                    return extractFields(clazz, depth);
                }
            }

            // Check if it's an enum
            for (EnumDeclaration enumDecl : cu.findAll(EnumDeclaration.class)) {
                if (enumDecl.getNameAsString().equals(className)) {
                    // Enums are not complex types with fields to expand
                    return Collections.emptyList();
                }
            }

        } catch (IOException e) {
            log.debug("Failed to parse source file: {}", sourcePath, e);
        }

        return Collections.emptyList();
    }

    private List<FieldInfo> extractFields(ClassOrInterfaceDeclaration clazz, int depth) {
        List<FieldInfo> fields = new ArrayList<>();

        for (FieldDeclaration fieldDecl : clazz.getFields()) {
            // Skip static fields
            if (fieldDecl.isStatic()) {
                continue;
            }

            for (VariableDeclarator var : fieldDecl.getVariables()) {
                FieldInfo field = new FieldInfo();
                field.setName(var.getNameAsString());
                field.setType(var.getTypeAsString());

                // Extract Javadoc comment
                fieldDecl.getJavadoc().ifPresent(javadoc -> {
                    String desc = javadoc.getDescription().toText().trim();
                    field.setDescription(desc);
                    field.setComment(desc);
                });

                // If no Javadoc, check for single-line comment
                if ((field.getDescription() == null || field.getDescription().isEmpty())
                        && fieldDecl.getComment().isPresent()) {
                    String comment = fieldDecl.getComment().get().getContent().trim();
                    // Strip leading * or // characters
                    comment = comment.replaceAll("^[/*]+\\s*", "").replaceAll("\\s*[/*]+$", "").trim();
                    field.setDescription(comment);
                    field.setComment(comment);
                }

                // Extract default value from initializer
                var.getInitializer().ifPresent(init -> {
                    String initStr = init.toString();
                    // Skip complex initializers
                    if (!initStr.contains("new ") && initStr.length() < 50) {
                        field.setDefaultValue(initStr.replace("\"", ""));
                    }
                });

                // Extract annotation info
                extractFieldAnnotations(fieldDecl, field);

                // Recursively resolve nested complex types
                List<FieldInfo> children = resolve(var.getTypeAsString(), depth + 1);
                if (!children.isEmpty()) {
                    field.setChildren(children);
                }

                fields.add(field);
            }
        }

        return fields;
    }

    /**
     * Extract validation/documentation annotations from a field.
     * Override to add support for custom annotations.
     */
    protected void extractFieldAnnotations(FieldDeclaration fieldDecl, FieldInfo field) {
        for (AnnotationExpr ann : fieldDecl.getAnnotations()) {
            String annName = ann.getNameAsString();
            switch (annName) {
                case "NotNull", "NotBlank", "NotEmpty" -> field.setRequired(true);
                case "Schema", "ApiModelProperty" -> {
                    // Swagger/OpenAPI annotations
                    if (ann.isNormalAnnotationExpr()) {
                        for (MemberValuePair pair : ann.asNormalAnnotationExpr().getPairs()) {
                            switch (pair.getNameAsString()) {
                                case "description", "value" -> {
                                    String desc = pair.getValue().toString().replace("\"", "");
                                    if (field.getDescription() == null || field.getDescription().isEmpty()) {
                                        field.setDescription(desc);
                                    }
                                }
                                case "example" -> field.setExample(pair.getValue().toString().replace("\"", ""));
                                case "required" -> field.setRequired(Boolean.parseBoolean(pair.getValue().toString()));
                            }
                        }
                    }
                }
                case "JsonProperty" -> {
                    // Jackson annotation — may override field name
                    if (ann.isSingleMemberAnnotationExpr()) {
                        String jsonName = ann.asSingleMemberAnnotationExpr()
                                .getMemberValue().toString().replace("\"", "");
                        if (!jsonName.isEmpty()) {
                            field.setName(jsonName);
                        }
                    } else if (ann.isNormalAnnotationExpr()) {
                        for (MemberValuePair pair : ann.asNormalAnnotationExpr().getPairs()) {
                            if ("value".equals(pair.getNameAsString())) {
                                String jsonName = pair.getValue().toString().replace("\"", "");
                                if (!jsonName.isEmpty()) {
                                    field.setName(jsonName);
                                }
                            }
                            if ("required".equals(pair.getNameAsString())) {
                                field.setRequired(Boolean.parseBoolean(pair.getValue().toString()));
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Determine if a type name is a complex type that should be resolved.
     * Override to customize the heuristic.
     *
     * @param typeName simple type name (no generics)
     * @return true if the type should be resolved
     */
    protected boolean isComplexType(String typeName) {
        if (typeName == null || typeName.isEmpty())
            return false;
        if (SIMPLE_TYPES.contains(typeName))
            return false;
        if (typeName.endsWith("[]"))
            return false; // arrays of primitives
        // Starts with lowercase → likely a primitive or package
        if (Character.isLowerCase(typeName.charAt(0)))
            return false;
        return true;
    }

    /**
     * Unwrap generic wrapper types to get the inner type.
     * e.g. "List<UserDto>" → "UserDto", "ResponseEntity<List<Order>>" → "Order"
     */
    private String unwrapType(String typeName) {
        String current = typeName;
        for (int i = 0; i < 5; i++) { // max unwrap iterations
            int ltIdx = current.indexOf('<');
            if (ltIdx < 0)
                break;

            String outer = current.substring(0, ltIdx);
            String inner = current.substring(ltIdx + 1, current.lastIndexOf('>'));

            if (WRAPPER_TYPES.contains(outer)) {
                current = inner.trim();
            } else {
                // Not a wrapper — the outer type itself is complex
                return outer;
            }
        }
        return current;
    }

    private void ensureIndexBuilt() {
        if (classIndex != null)
            return;
        classIndex = new HashMap<>();

        for (Path sourceRoot : sourceRoots) {
            if (!Files.isDirectory(sourceRoot))
                continue;
            try {
                Files.walkFileTree(sourceRoot, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (file.toString().endsWith(".java")) {
                            String fileName = file.getFileName().toString();
                            String className = fileName.substring(0, fileName.length() - 5);
                            classIndex.computeIfAbsent(className, k -> new ArrayList<>()).add(file);
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                log.warn("Failed to index source root: {}", sourceRoot, e);
            }
        }
    }
}
