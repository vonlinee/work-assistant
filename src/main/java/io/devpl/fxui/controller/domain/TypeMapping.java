package io.devpl.fxui.controller.domain;

import io.devpl.fxui.model.CommonJavaType;
import io.devpl.fxui.model.JavaType;
import lombok.Getter;
import lombok.Setter;

import java.sql.JDBCType;
import java.sql.SQLType;
import java.util.ArrayList;
import java.util.List;

/**
 * 类型映射
 */
@Getter
@Setter
public class TypeMapping {

    public static List<TypeMapping> mapppings = defaultMapping();

    /**
     * SQL类型
     */
    private SQLType sqlType;

    /**
     * Java数据类型
     */
    private JavaType javaDataType;

    /**
     * SQL类型的长度
     */
    private String sqlTypeLength;

    public TypeMapping(SQLType sqlType, JavaType javaDataType) {
        this.sqlType = sqlType;
        this.javaDataType = javaDataType;
    }

    public TypeMapping(JavaType javaDataType, SQLType sqlType) {
        this.sqlType = sqlType;
        this.javaDataType = javaDataType;
    }

    public TypeMapping(JavaType javaDataType, SQLType sqlType, String sqlTypeLength) {
        this.sqlType = sqlType;
        this.javaDataType = javaDataType;
        this.sqlTypeLength = sqlTypeLength;
    }

    /**
     * 默认的类型映射表
     * @return
     */
    public static List<TypeMapping> defaultMapping() {
        List<TypeMapping> typeMappingList = new ArrayList<>();
        typeMappingList.add(new TypeMapping(CommonJavaType.LONG, JDBCType.BIGINT, "(20)"));
        typeMappingList.add(new TypeMapping(CommonJavaType.INTEGER, JDBCType.INTEGER, "(11)"));
        typeMappingList.add(new TypeMapping(CommonJavaType.STRING, JDBCType.VARCHAR, "(50)"));
        typeMappingList.add(new TypeMapping(CommonJavaType.BOOLEAN, JDBCType.TINYINT, "(1)"));
        typeMappingList.add(new TypeMapping(CommonJavaType.FLOAT, JDBCType.DECIMAL, "(5, 2)"));
        typeMappingList.add(new TypeMapping(CommonJavaType.DOUBLE, JDBCType.DECIMAL, "(5, 2)"));
        typeMappingList.add(new TypeMapping(CommonJavaType.BIG_DECIMAL, JDBCType.DECIMAL, "(5, 2)"));
        typeMappingList.add(new TypeMapping(CommonJavaType.BIG_INTEGER, JDBCType.INTEGER, "(11)"));
        typeMappingList.add(new TypeMapping(CommonJavaType.LOCAL_DATE, JDBCType.DATE));
        typeMappingList.add(new TypeMapping(CommonJavaType.LOCAL_DATE_TIME, JDBCType.TIMESTAMP));
        return typeMappingList;
    }

    @Override
    public String toString() {
        return "TypeMapping{" +
                "sqlType=" + sqlType +
                ", javaDataType=" + javaDataType +
                ", sqlTypeLength='" + sqlTypeLength + '\'' +
                '}';
    }
}
