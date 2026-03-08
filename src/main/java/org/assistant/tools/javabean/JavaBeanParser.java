package org.assistant.tools.javabean;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Parses Java source code into a map of class-name → list-of-fields.
 * Every class found in the CompilationUnit is indexed so nested types
 * can be resolved during code/JSON generation.
 */
public class JavaBeanParser {

    /** Lightweight model for a single field. */
    public static class FieldInfo {
        private final String name;
        private final String typeName; // raw type string, e.g. "String", "List<Address>"
        private final String baseType; // outer type, e.g. "List" for "List<Address>"
        private final String genericArg; // first generic arg, e.g. "Address" for "List<Address>"

        public FieldInfo(String name, String typeName) {
            this.name = name;
            this.typeName = typeName;
            // parse base + generic
            int lt = typeName.indexOf('<');
            if (lt >= 0) {
                this.baseType = typeName.substring(0, lt).trim();
                // strip outer "<" and ">"
                this.genericArg = typeName.substring(lt + 1, typeName.lastIndexOf('>')).trim();
            } else {
                this.baseType = typeName;
                this.genericArg = null;
            }
        }

        public String getName() {
            return name;
        }

        public String getTypeName() {
            return typeName;
        }

        public String getBaseType() {
            return baseType;
        }

        /** Returns the first generic argument, or null if not generic. */
        public String getGenericArg() {
            return genericArg;
        }

        /** True for Collection types (List, Set, Queue …). */
        public boolean isCollection() {
            return baseType.equals("List") || baseType.equals("ArrayList") ||
                    baseType.equals("Set") || baseType.equals("HashSet") ||
                    baseType.equals("LinkedHashSet") || baseType.equals("Queue") ||
                    baseType.equals("Collection");
        }

        /** True for Map types. */
        public boolean isMap() {
            return baseType.equals("Map") || baseType.equals("HashMap") ||
                    baseType.equals("LinkedHashMap") || baseType.equals("TreeMap");
        }

        /** True for primitive + common value types. */
        public boolean isSimpleType() {
            return isPrimitive(baseType) || isWrapperOrString(baseType);
        }

        public static boolean isPrimitive(String t) {
            return switch (t) {
                case "int", "long", "double", "float", "boolean", "byte", "short", "char" -> true;
                default -> false;
            };
        }

        public static boolean isWrapperOrString(String t) {
            return switch (t) {
                case "Integer", "Long", "Double", "Float", "Boolean", "Byte", "Short",
                        "Character", "String", "BigDecimal", "BigInteger",
                        "Date", "LocalDate", "LocalDateTime", "LocalTime",
                        "Instant", "ZonedDateTime", "OffsetDateTime" ->
                    true;
                default -> false;
            };
        }

        @Override
        public String toString() {
            return typeName + " " + name;
        }
    }

    /** Model for a single class found in the parsed CompilationUnit. */
    public static class ClassInfo {
        private final String className;
        private final List<FieldInfo> fields = new ArrayList<>();

        public ClassInfo(String className) {
            this.className = className;
        }

        public String getClassName() {
            return className;
        }

        public List<FieldInfo> getFields() {
            return fields;
        }
    }

    /**
     * Parse source code and return all classes found, keyed by simple class name.
     *
     * @param source full Java source code (may contain multiple class declarations)
     * @return ordered map of class-name → ClassInfo; empty map on parse failure
     * @throws IllegalArgumentException if the source cannot be parsed
     */
    public static Map<String, ClassInfo> parse(String source) {
        JavaParser parser = new JavaParser();
        ParseResult<CompilationUnit> result = parser.parse(source);
        if (!result.isSuccessful() || result.getResult().isEmpty()) {
            String issues = result.getProblems().toString();
            throw new IllegalArgumentException("Failed to parse Java source:\n" + issues);
        }

        CompilationUnit cu = result.getResult().get();
        Map<String, ClassInfo> classMap = new LinkedHashMap<>();

        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(cls -> {
            if (cls.isInterface())
                return;
            String name = cls.getNameAsString();
            ClassInfo ci = new ClassInfo(name);

            for (FieldDeclaration fd : cls.getFields()) {
                // skip static fields
                if (fd.isStatic())
                    continue;
                for (VariableDeclarator vd : fd.getVariables()) {
                    // Use the full variable type (includes generic arguments kept by parser)
                    String fullType = vd.getType().asString();
                    ci.getFields().add(new FieldInfo(vd.getNameAsString(), fullType));
                }
            }

            classMap.put(name, ci);
        });

        return classMap;
    }

    /**
     * Convenience: return the first (primary) class in the parsed unit.
     */
    public static Optional<ClassInfo> parsePrimary(String source) {
        Map<String, ClassInfo> map = parse(source);
        return map.isEmpty() ? Optional.empty() : Optional.of(map.values().iterator().next());
    }
}
