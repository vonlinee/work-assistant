package io.devpl.common.utils;

/**
 * JSON和对象之间互相转换
 */
public interface JSONConverter {

    /**
     * 对象转为JSON字符串
     *
     * @param obj 对象
     * @return JSON字符串
     */
    String toJSONString(Object obj);

    /**
     * 对象转为JSON字符串
     *
     * @param obj            对象
     * @param prettyPrinting 是否格式化
     * @return JSON字符串
     */
    String toJSONString(Object obj, boolean prettyPrinting);

    /**
     * JSON字符串反序列化为对象
     *
     * @param jsonString JSON字符串
     * @param type       对象类型
     * @param <T>        对象类型
     * @return 对象实例
     */
    <T> T toObject(String jsonString, Class<T> type);
}
