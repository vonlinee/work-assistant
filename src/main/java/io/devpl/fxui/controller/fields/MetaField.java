package io.devpl.fxui.controller.fields;

import lombok.Getter;
import lombok.Setter;

/**
 * 字段信息: 和Java、数据库都无关的字段
 * 包含字段名称，数据类型
 */
@Getter
@Setter
public class MetaField {

    /**
     * 字段标识符
     */
    private String identifier;

    /**
     * 名称
     */
    private String name;

    /**
     * 含义及描述信息
     */
    private String description;

    /**
     * 数据类型名称
     * java class name
     * 使用字符串来表示任意值，增加灵活性，使用时需解析此值得到相应的类型
     *
     * @see java.sql.Types
     */
    private String dataType;
}
