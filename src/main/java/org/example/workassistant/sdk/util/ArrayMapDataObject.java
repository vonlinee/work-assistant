package org.example.workassistant.sdk.util;

import org.example.workassistant.sdk.collection.ArrayMap;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 采用ArrayMap结构实现，适用于小数据量
 */
final class ArrayMapDataObject implements DataObject, Cloneable {

    /**
     * 小驼峰命名:以字母开头
     */
    private static final Pattern NAME_PATTERN = Pattern.compile("[a-zA-z_][a-zA-z0-9_]*");

    /**
     * 存放实际的数据
     */
    private final ArrayMap<String, Object> data;

    /**
     * 严格模式，如果为true，针对每个数据项的key都要进行合法性校验
     */
    private final boolean strict;

    public ArrayMapDataObject(int initialCapacity) {
        this(true, initialCapacity);
    }

    public ArrayMapDataObject(boolean strict, int initialCapacity) {
        this.data = new ArrayMap<>(initialCapacity);
        this.strict = strict;
    }

    public ArrayMapDataObject(Map<String, Object> initialData) {
        this(true, initialData);
    }

    public ArrayMapDataObject(boolean strict, Map<String, Object> initialData) {
        if (initialData != null) {
            this.data = new ArrayMap<>(initialData.size());
            data.putAll(initialData);
        } else {
            this.data = new ArrayMap<>();
        }
        this.strict = strict;
    }

    /**
     * 校验name是否合法
     *
     * @param name 数据项名称
     */
    private void ensureValidName(String name) {
        if (strict && !isKeyAllowed(name)) {
            throw new IllegalArgumentException("illegal item name [" + name + "] of data!");
        }
    }

    @Override
    public boolean containsKey(String name) {
        return isKeyAllowed(name) && data.containsKey(name);
    }

    @Override
    public boolean containsValue(Object value) {
        return data.containsValue(value);
    }

    @Override
    public void set(String name, Object value) throws NoSuchElementException {
        if (!containsKey(name)) {
            throw new NoSuchElementException(name);
        }
        data.put(name, value);
    }

    /**
     * 数据项的名称需要字母或者数字
     *
     * @param name 数据项名称
     * @return 数据项名称是否符合规则
     */
    private boolean isKeyAllowed(String name) {
        return NAME_PATTERN.matcher(name).matches();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V getTypedValue(String name) {
        if (!isKeyAllowed(name)) {
            return null;
        }
        return (V) data.get(name);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V getTypedValue(String name, Class<V> valueType) {
        final Object typedValue = getTypedValue(name);
        if (typedValue == null || isAssignableFrom(typedValue.getClass(), valueType)) {
            return null;
        }
        return (V) typedValue;
    }

    @Override
    public <V> V getTypedValue(String name, V defaultValue) {
        final Object typedValue = getTypedValue(name);
        if (typedValue == null) {
            return defaultValue;
        }
        return null;
    }

    @Override
    public <V> V getTypedValue(String name, Class<V> valueType, V defaultValue) {
        final Object typedValue = getTypedValue(name);
        if (typedValue == null || isAssignableFrom(typedValue.getClass(), valueType)) {
            return defaultValue;
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V remove(String key) {
        return (V) data.remove(key);
    }

    @Override
    public void removeKeys(String... keys) {
        data.removeAll(Arrays.asList(keys));
    }

    @Override
    public boolean equals(DataObject obj) {
        return Objects.equals(this.asMap(), obj.asMap());
    }

    private boolean isAssignableFrom(Class<?> aClass, Class<?> bClass) {
        return false;
    }

    @Override
    public Set<String> keySet() {
        return asMap().keySet();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> Collection<V> values() {
        return (Collection<V>) asMap().values();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof DataObject) {
            return equals((DataObject) object);
        }
        return false;
    }

    @Override
    public String toString() {
        return "DataObject@" + Integer.toHexString(hashCode());
    }

    @Override
    public Map<String, Object> asMap() {
        return data;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
