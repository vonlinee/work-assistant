package org.assistant.tools.javabean;

import org.assistant.tools.javabean.JavaBeanParser.ClassInfo;
import org.assistant.tools.javabean.JavaBeanParser.FieldInfo;

import java.util.Map;

/**
 * Generates Java object-creation code from a parsed JavaBean class.
 *
 * <p>
 * Example output for a class {@code Person} with fields {@code String name}
 * and {@code Address address}:
 * 
 * <pre>
 * Person obj = new Person();
 * obj.setName("mockName");
 * Address objAddress = new Address();
 * objAddress.setCity("mockCity");
 * obj.setAddress(objAddress);
 * </pre>
 */
public class ObjectCreationGenerator {

    private final JavaBeanConfig config;
    /**
     * All classes found in the same CompilationUnit, used for nested-type
     * resolution.
     */
    private final Map<String, ClassInfo> classMap;

    private int counter = 0; // ensures unique variable names

    public ObjectCreationGenerator(JavaBeanConfig config, Map<String, ClassInfo> classMap) {
        this.config = config;
        this.classMap = classMap;
    }

    /**
     * Generate object-creation code for the given {@link ClassInfo}.
     *
     * @param classInfo parsed class model
     * @return generated Java source code as a string
     */
    public String generate(ClassInfo classInfo) {
        counter = 0;
        StringBuilder sb = new StringBuilder();
        String rootVar = config.getVarName();
        generateClass(sb, classInfo, rootVar, 0);
        return sb.toString();
    }

    // ─────────────────────────────────────────────────────────────────────────

    private void generateClass(StringBuilder sb, ClassInfo ci, String varName, int depth) {
        String className = ci.getClassName();
        sb.append(className).append(" ").append(varName)
                .append(" = new ").append(className).append("();\n");

        for (FieldInfo fi : ci.getFields()) {
            generateField(sb, fi, varName, depth);
        }
    }

    private void generateField(StringBuilder sb, FieldInfo fi, String parentVar, int depth) {
        String setter = config.isUseSetters()
                ? toSetterName(fi.getName())
                : fi.getName();

        String value = resolveValue(fi, parentVar, depth, sb);

        if (config.isUseSetters()) {
            sb.append(parentVar).append(".").append(setter)
                    .append("(").append(value).append(");\n");
        } else {
            sb.append(parentVar).append(".").append(fi.getName())
                    .append(" = ").append(value).append(";\n");
        }
    }

    /**
     * Returns the in-line value expression OR emits helper variable declarations
     * into {@code sb} and returns the variable name.
     */
    private String resolveValue(FieldInfo fi, String parentVar, int depth, StringBuilder sb) {
        String base = fi.getBaseType();

        // ── primitive / wrapper / String ──────────────────────────────────────
        if (fi.isSimpleType()) {
            return simpleMockValue(base, fi.getName());
        }

        // ── enum placeholder ──────────────────────────────────────────────────
        // We can't enumerate enum values without loading the class; emit null.
        // ── depth guard ───────────────────────────────────────────────────────
        if (depth >= config.getMaxDepth()) {
            return config.isNullBeyondMaxDepth() ? "null" : "new " + base + "()";
        }

        // ── Collection ────────────────────────────────────────────────────────
        if (fi.isCollection()) {
            String arg = fi.getGenericArg();
            return buildCollectionValue(fi, arg, parentVar, depth, sb);
        }

        // ── Map ───────────────────────────────────────────────────────────────
        if (fi.isMap()) {
            return "new java.util.LinkedHashMap<>()";
        }

        // ── array ─────────────────────────────────────────────────────────────
        if (base.endsWith("[]")) {
            String component = base.substring(0, base.length() - 2).trim();
            String arrayVar = uniqueVar(fi.getName() + "Array");
            sb.append(base).append(" ").append(arrayVar)
                    .append(" = new ").append(component).append("[")
                    .append(config.getMockCollectionSize()).append("];\n");
            return arrayVar;
        }

        // ── nested complex type defined in the same CompilationUnit ───────────
        if (classMap.containsKey(base)) {
            ClassInfo nested = classMap.get(base);
            String nestedVar = uniqueVar(fi.getName());
            // Temporarily capture the nested class generation
            StringBuilder nested_sb = new StringBuilder();
            generateClass(nested_sb, nested, nestedVar, depth + 1);
            sb.append(nested_sb);
            return nestedVar;
        }

        // ── unknown complex type: just new it ─────────────────────────────────
        String unknownVar = uniqueVar(fi.getName());
        sb.append(base).append(" ").append(unknownVar)
                .append(" = new ").append(base).append("();\n");
        return unknownVar;
    }

    private String buildCollectionValue(FieldInfo fi, String elementType,
            String parentVar, int depth, StringBuilder sb) {
        String listVar = uniqueVar(fi.getName() + "List");

        // determine concrete implementation for declaration
        String impl = switch (fi.getBaseType()) {
            case "Set", "HashSet" -> "java.util.HashSet";
            case "LinkedHashSet" -> "java.util.LinkedHashSet";
            default -> "java.util.ArrayList";
        };

        sb.append(fi.getTypeName()).append(" ").append(listVar)
                .append(" = new ").append(impl).append("<>();\n");

        if (elementType == null || elementType.isEmpty()) {
            return listVar;
        }

        for (int i = 0; i < config.getMockCollectionSize(); i++) {
            if (FieldInfo.isPrimitive(elementType) || FieldInfo.isWrapperOrString(elementType)) {
                sb.append(listVar).append(".add(")
                        .append(simpleMockValue(elementType, fi.getName() + i)).append(");\n");
            } else if (classMap.containsKey(elementType)) {
                ClassInfo nested = classMap.get(elementType);
                String itemVar = uniqueVar(fi.getName() + "Item" + i);
                StringBuilder nested_sb = new StringBuilder();
                generateClass(nested_sb, nested, itemVar, depth + 1);
                sb.append(nested_sb);
                sb.append(listVar).append(".add(").append(itemVar).append(");\n");
            } else {
                String itemVar = uniqueVar(fi.getName() + "Item" + i);
                sb.append(elementType).append(" ").append(itemVar)
                        .append(" = new ").append(elementType).append("();\n");
                sb.append(listVar).append(".add(").append(itemVar).append(");\n");
            }
        }
        return listVar;
    }

    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Generate a simple literal mock value for primitive / wrapper / String types.
     */
    private String simpleMockValue(String type, String fieldName) {
        int num = config.isRandomNumbers() ? (int) (Math.random() * 1000) : ++counter;
        return switch (type) {
            case "String" -> "\"mock" + capitalize(fieldName) + "\"";
            case "int", "Integer" -> String.valueOf(num);
            case "long", "Long" -> num + "L";
            case "double", "Double" -> num + ".0";
            case "float", "Float" -> num + ".0f";
            case "boolean", "Boolean" -> "true";
            case "byte", "Byte" -> "(byte) " + (num % 128);
            case "short", "Short" -> "(short) " + num;
            case "char", "Character" -> "'A'";
            case "BigDecimal" -> "new java.math.BigDecimal(\"" + num + ".00\")";
            case "BigInteger" -> "new java.math.BigInteger(\"" + num + "\")";
            case "Date" -> "new java.util.Date()";
            case "LocalDate" -> "java.time.LocalDate.now()";
            case "LocalDateTime" -> "java.time.LocalDateTime.now()";
            case "LocalTime" -> "java.time.LocalTime.now()";
            case "Instant" -> "java.time.Instant.now()";
            default -> "null";
        };
    }

    private String toSetterName(String fieldName) {
        return "set" + capitalize(fieldName);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty())
            return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private String uniqueVar(String hint) {
        return hint + (++counter);
    }
}
