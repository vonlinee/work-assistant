package org.workassistant.util.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * 用于方法间的数据传输，可以动态地存放数据
 */
public interface DataObject extends Serializable {

    /**
     * 是否存在某个名称的数据项
     *
     * @param key 数据项名称
     * @return 是否存在某个名称的数据项
     */
    boolean containsKey(String key);

    /**
     * 是否存在某个数据值
     *
     * @param value 数据值
     * @return 是否存在某个数据值
     */
    boolean containsValue(Object value);

    /**
     * 设置name的值，如果name不存在，则抛出异常
     *
     * @param name  数据项的名称
     * @param value 数据项的新值
     * @throws NoSuchElementException 如果name指示的元素不存在，抛出此异常
     */
    void set(String name, Object value) throws NoSuchElementException;

    /**
     * 根据名称获取值
     *
     * @param key 数据项名称
     * @param <V> 期望的值类型
     * @return key对应的值，返回任意泛型，避免强制转换，但需要注意使用预期的类型
     * @throws ClassCastException 如果实际的类型和期望的值类型不兼容，会抛出ClassCastException
     */
    <V> V getTypedValue(String key);

    /**
     * 获取和指定Class兼容的类型，如果没有则返回null
     *
     * @param name      key
     * @param valueType 期望的值类型，必须相等才认为是同类型，不允许向上造型
     * @param <V>       期望的值类型
     * @return 期望的值，如果实际的类型和期望的值类型不兼容，返回null
     * @throws ClassCastException 如果实际的类型和期望的值类型不兼容，会抛出ClassCastException
     */
    <V> V getTypedValue(String name, Class<V> valueType);

    /**
     * 根据名称获取值
     *
     * @param name 数据项名称
     * @param <V>  期望的值类型
     * @return 期望的值，如果实际的类型和期望的值类型不兼容，会抛出ClassCastException
     * @throws ClassCastException 如果实际的类型和期望的值类型不兼容，会抛出ClassCastException
     */
    <V> V getTypedValue(String name, V defaultValue);

    /**
     * 根据名称获取值
     *
     * @param name         数据项名称
     * @param valueType    期望的值类型Class
     * @param defaultValue 默认值
     * @param <V>          期望的值类型
     * @return 期望的值，如果实际的类型和期望的值类型valueType不同，返回默认值
     * @throws ClassCastException 如果实际的类型和期望的值类型不兼容，会抛出ClassCastException
     */
    <V> V getTypedValue(String name, Class<V> valueType, V defaultValue);

    /**
     * 删除某个名称的值
     *
     * @param key 名称
     * @return 旧值
     * @throws ClassCastException 如果实际的类型和期望的值类型不兼容，会抛出ClassCastException
     */
    <V> V remove(String key);

    /**
     * 删除某些名称的值
     *
     * @param keys 名称
     */
    void removeKeys(String... keys);

    /**
     * 返回数据的key集合
     *
     * @return 数据的key集合
     */
    Set<String> keySet();

    /**
     * 返回数据的value集合
     *
     * @return 数据的key集合
     */
    <V> Collection<V> values();

    /**
     * 判断两个DataObject是否相等，只需要比较DataObject所包含的数据，和DataObject本身无关系
     *
     * @param obj 另一个DataObject
     * @return boolean
     */
    boolean equals(DataObject obj);

    /**
     * 返回DataObject自身的hashCode，一般用不到此方法
     *
     * @return hashcode
     */
    @Override
    int hashCode();

    /**
     * 如果要显示数据内容，需要通过asMap()方法，而不是通过此方法
     * 此方法应该对DataObject自身进行toString()
     *
     * @return String
     */
    @Override
    String toString();

    /**
     * 将存放的数据转换成Map
     *
     * @return 包含所有数据项的map，不能为空
     */
    Map<String, Object> asMap();
}
