package org.workassistant.util.collection;

import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 大小写不敏感
 *
 * @param <V>
 * @apiNote key不能为null，也不能为空字符串，且首尾只能是英文字符
 * 不存储字符串的key内容
 */
public class StrictStringKeyMap<V> implements Map<String, V> {

    /**
     * key为字符串的hash值
     */
    HashMap<Integer, V> map = new HashMap<>();

    /**
     * 忽略大小写的hash算法
     *
     * @param s 作为key的字符串
     * @return hash值
     * @see Arrays#hashCode(char[])
     */
    protected int hash(String s) {
        if (s == null)
            return 0;
        int result = 1;
        char[] chars = s.toCharArray();
        for (int element : chars) {
            if (Character.isUpperCase(element)) {
                result = 31 * result + Character.toLowerCase(element);
            } else {
                result = 31 * result + element;
            }
        }
        return result;
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        if (key instanceof String _key) {
            return map.containsKey(hash(_key));
        }
        return false;
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        if (key instanceof String _key) {
            return map.get(hash(_key));
        }
        return null;
    }

    @Nullable
    @Override
    public V put(String key, V value) {
        return map.put(hash(key), value);
    }

    @Override
    public V remove(Object key) {
        if (key instanceof String _key) {
            return map.remove(hash(_key));
        }
        return null;
    }

    @Override
    public void putAll( Map<? extends String, ? extends V> m) {
        for (Entry<? extends String, ? extends V> entry : m.entrySet()) {
            map.put(hash(entry.getKey()), entry.getValue());
        }
    }

    @Override
    public void clear() {
        map.clear();
    }

    /**
     * 此Map不存储具体的key
     *
     * @return 始终返回空集合
     */
    
    @Override
    public Set<String> keySet() {
        throw new UnsupportedOperationException();
    }

    
    @Override
    public Collection<V> values() {
        return map.values();
    }

    /**
     * 此Map不存储具体的key
     *
     * @return 始终返回空集合
     */
    
    @Override
    public Set<Entry<String, V>> entrySet() {
        throw new UnsupportedOperationException();
    }
}
