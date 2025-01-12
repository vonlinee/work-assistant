package io.devpl.fxui.tools.filestructure;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 字段类型
 */
public enum FieldType {

    PRIMITIVE_BOOLEAN("boolean", boolean.class),
    PRIMITIVE_CHAR("char", char.class),
    PRIMITIVE_BYTE("byte", byte.class),
    PRIMITIVE_SHORT("short", short.class),
    PRIMITIVE_INT("int", int.class),
    PRIMITIVE_FLOAT("float", float.class),
    PRIMITIVE_LONG("long", long.class),
    PRIMITIVE_DOUBLE("double", double.class),
    BOOLEAN("Boolean", Boolean.class),
    CHAR("Character", Character.class),
    BYTE("Byte", Byte.class),
    SHORT("Short", Short.class),
    INT("Integer", Integer.class),
    FLOAT("Float", Float.class),
    LONG("Long", Long.class),
    DOUBLE("Double", Double.class),
    STRING("String", String.class),
    DATE("Double", Double.class),
    LOCAL_DATE("LocalDate", LocalDate.class),
    LOCAL_DATE_TIME("LocalDateTime", LocalDateTime.class),
    BIG_INTEGER("BigInteger", BigInteger.class),
    BIG_DECIMAL("BigDecimal", BigDecimal.class);

    final String typeName;
    /**
     * 全限定类型名称
     */
    String qualifier;

    FieldType(String name) {
        this.typeName = name;
    }

    FieldType(String typeName, String qualifier) {
        this.typeName = typeName;
        this.qualifier = qualifier;
    }

    FieldType(String typeName, Class<?> typeClass) {
        this.typeName = typeName;
        this.qualifier = typeClass.getName();
    }

    public static List<String> getAllTypeNames() {
        return Arrays.stream(values()).map(FieldType::getTypeName).collect(Collectors.toList());
    }

    public String getTypeName() {
        return typeName;
    }

    public String getQualifier() {
        return qualifier;
    }
}
