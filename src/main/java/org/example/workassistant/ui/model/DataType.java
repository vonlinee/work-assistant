package org.example.workassistant.ui.model;

import org.jetbrains.annotations.Nullable;

/**
 * 所有数据类型的父接口
 */
public interface DataType {

    DataType UNKNOWN = new DataType() {
    };
    DataType ANY = new DataType() {
    };

    default String getCategory() {
        return "*";
    }

    /**
     * 数据类型ID
     *
     * @return 数据类型ID，全局唯一
     */
    default String id() {
        return getQualifier();
    }

    /**
     * 唯一限定类型名称
     *
     * @return 限定类型名称
     */
    default String getQualifier() {
        return toString();
    }

    /**
     * 获取该类型的泛型类型
     *
     * @return 类型的泛型类型
     */
    default DataType[] getGenericTypes() {
        return null;
    }

    /**
     * 是否是数组类型，同种数据的集合
     *
     * @return 是否是数组类型
     */
    default boolean isArray() {
        return false;
    }

    /**
     * 元素类型，用于集合类型(数组，列表等)
     *
     * @return 元素类型
     * @see DataType#isArray()
     */
    default DataType getComponentType() {
        return null;
    }

    /**
     * 设置元素类型
     *
     * @param componentType 元素数据类型
     */
    default void setComponentType(DataType componentType) {
    }

    /**
     * 校验字面值是否合法
     *
     * @param literalValue 字面值
     * @param error        存放错误信息
     * @return 字面值是否合法
     */
    default boolean matches(String literalValue, StringBuilder error) {
        return true;
    }

    /**
     * 将对象转换为字符串
     *
     * @param obj 对象
     * @return 字符串表示的对象
     */
    default String serialize(Object obj) {
        return String.valueOf(obj);
    }

    /**
     * 将字面值解析成对应的java对象
     *
     * @param literalValue 字面值
     * @param sb           记录错误信息，如果字面值合法，那么无错误信息
     * @return java对象
     */
    @Nullable
    default Object deserialize(String literalValue, StringBuilder sb) {
        return null;
    }

    /**
     * 将字面值
     *
     * @param literalValue 字面值
     * @return 处理过的字面值
     */
    default String normalize(String literalValue) {
        return literalValue;
    }

    /**
     * 决定字面值是否需要使用引号包裹
     *
     * @param value 字面值
     * @return 字面值
     */
    default String quote(String value) {
        return value;
    }
}
