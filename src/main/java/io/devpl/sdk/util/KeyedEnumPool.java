package io.devpl.sdk.util;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通过key进行访问的常量池
 * 和ConstantPool相比不需要定义泛型
 *
 * @param <K> key类型
 * @param <E> 常量类型
 */
public abstract class KeyedEnumPool<K, E> {

    public final ConcurrentHashMap<K, E> enumerations = new ConcurrentHashMap<>();

    public E get(Object key) throws NoSuchElementException {
        if (key == null) {
            throw new NoSuchElementException();
        }
        final E element = enumerations.get(key);
        if (element == null) {
            throw new NoSuchElementException();
        }
        return element;
    }

    /**
     * 新增枚举实例
     *
     * @param key      枚举key
     * @param instance 枚举实例
     * @return 旧元素
     */
    private E putEnum(K key, E instance) {
        return enumerations.put(key, instance);
    }

    public E put(K key, E instance) {
        return putEnum(key, instance);
    }

    public final boolean containsKey(K key) {
        return enumerations.containsKey(key);
    }

    public List<E> values() {
        return new ArrayList<>(enumerations.values());
    }
}
