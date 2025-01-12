package io.devpl.fxui.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 常见的Java实体类字段类型
 * <a href="https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html">...</a>
 */
public enum CommonJavaType implements JavaType {
    // 基本类型
    BASE_BYTE("byte", "byte"),
    BASE_SHORT("short", "short"),
    BASE_CHAR("char", "char"),
    BASE_INT("int", "int"),
    BASE_LONG("long", "long"),
    BASE_FLOAT("float", "float"),
    BASE_DOUBLE("double", "double"),
    BASE_BOOLEAN("boolean", "boolean"),

    // 包装类型
    BYTE("Byte", "java.lang.Byte"),
    SHORT("Short", "java.lang.Short"),
    CHARACTER("Character", "java.lang.Character"),
    INTEGER("Integer", "java.lang.Integer"),
    LONG("Long", "java.lang.Long"),
    FLOAT("Float", "java.lang.Float"),
    DOUBLE("Double", "java.lang.Double"),
    BOOLEAN("Boolean", "java.lang.Boolean"),
    NUMBER("Number", "java.lang.Number"),
    STRING("String", "java.lang.String"),

    // sql 包下数据类型
    DATE_SQL("Date", "java.sql.Date"),
    TIME("Time", "java.sql.Time"),
    TIMESTAMP("Timestamp", "java.sql.Timestamp"),
    BLOB("Blob", "java.sql.Blob"),
    CLOB("Clob", "java.sql.Clob"),

    // java8 新时间类型
    LOCAL_DATE("LocalDate", "java.time.LocalDate"),
    LOCAL_TIME("LocalTime", "java.time.LocalTime"),
    YEAR("Year", "java.time.Year"),
    YEAR_MONTH("YearMonth", "java.time.YearMonth"),
    LOCAL_DATE_TIME("LocalDateTime", "java.time.LocalDateTime"),
    INSTANT("Instant", "java.time.Instant"),

    // 其他杂类

    OBJECT("Object", "java.lang.Object"),
    DATE("Date", "java.util.Date"),
    BIG_INTEGER("BigInteger", "java.math.BigInteger"),
    BIG_DECIMAL("BigDecimal", "java.math.BigDecimal"),

    // 数组类型
    OBJECT_ARRAY("Object[]", "java.lang.Object[]"),
    BYTE_ARRAY("byte[]", "byte[]"),
    ANY_ARRAY("*[]", "*[]"),

    // 常见集合类型 无法知道元素类型
    LIST("List", "java.util.List"),
    SET("Set", "java.util.List"),
    MAP("Map", "java.util.Map"),

    // 未知类型，使用Object进行兼容
    UNKNOWN(OBJECT),
    ;

    /**
     * 类型
     */
    private final String type;

    /**
     * 限定符
     */
    private final String qualifier;

    CommonJavaType(CommonJavaType type) {
        this.type = type.type;
        this.qualifier = type.qualifier;
    }

    CommonJavaType(final String type, final String qulifiedName) {
        this.type = type;
        this.qualifier = qulifiedName;
    }

    public static Map<String, CommonJavaType> typeMap() {
        HashMap<String, CommonJavaType> map = new HashMap<>();
        for (CommonJavaType type : values()) {
            if (type.getName() != null) {
                map.put(type.getName(), type);
            }
            if (type.getQualifier() != null) {
                map.put(type.getQualifier(), type);
            }
        }
        return map;
    }

    public static CommonJavaType valueOfQulifiedName(String qualifiedName) {
        for (CommonJavaType value : values()) {
            if (value.getQualifier().equals(qualifiedName)) {
                return value;
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return type;
    }

    @Override
    public String getQualifier() {
        return qualifier;
    }

    /**
     * 判断是否是基本数据类型
     *
     * @return 是否是基本数据类型
     */
    public boolean isPrimitive() {
        return this.getName().startsWith("BASE");
    }
}
