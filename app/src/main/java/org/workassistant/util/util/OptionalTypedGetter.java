package org.workassistant.util.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 提供默认值，不对默认值进行检查，由调用方保证其是否符合要求
 *
 * @param <K> key类型
 */
public interface OptionalTypedGetter<K> extends TypedGetter<K> {

    default Object getObject(K key, Object optional) {
        Object val = getObject(key);
        return val == null ? optional : val;
    }

    default String getString(K key, String optional) {
        String val = getString(key);
        return val == null ? optional : val;
    }

    default Integer getInteger(K key, Integer optional) {
        Integer val = getInteger(key);
        return val == null ? optional : val;
    }

    default Short getShort(K key, Short optional) {
        Short val = getShort(key);
        return val == null ? optional : val;
    }

    default Boolean getBoolean(K key, Boolean optional) {
        Boolean val = getBoolean(key);
        return val == null ? optional : val;
    }

    default Long getLong(K key, Long optional) {
        Long val = getLong(key);
        return val == null ? optional : val;
    }

    default Character getCharacter(K key, Character optional) {
        Character val = getCharacter(key);
        return val == null ? optional : val;
    }

    default Float getFloat(K key, Float optional) {
        Long val = getLong(key);
        return val == null ? optional : val;
    }

    default Double getDouble(K key, Double optional) {
        Long val = getLong(key);
        return val == null ? optional : val;
    }

    default Byte getByte(K key, Byte optional) {
        Byte val = getByte(key);
        return val == null ? optional : val;
    }

    default BigDecimal getBigDecimal(K key, BigDecimal optional) {
        BigDecimal val = getBigDecimal(key);
        return val == null ? optional : val;
    }

    default Number getNumber(K key, Number optional) {
        Number val = getNumber(key);
        return val == null ? optional : val;
    }

    default BigInteger getBigInteger(K key, BigInteger optional) {
        BigInteger val = getBigInteger(key);
        return val == null ? optional : val;
    }

    default <E extends Enum<E>> E getEnum(Class<E> clazz, K key, E optional) {
        E val = getEnum(clazz, key);
        return val == null ? optional : val;
    }

    default Date getDate(K key, Date optional) {
        Date val = getDate(key);
        return val == null ? optional : val;
    }

    default LocalDateTime getLocalDateTime(K key, LocalDateTime optional) {
        LocalDateTime val = getLocalDateTime(key);
        return val == null ? optional : val;
    }

    default <V> V get(K key, Class<V> type, V optional) {
        V val = get(key, type);
        return val == null ? optional : val;
    }

    default <MK, V> Map<MK, V> getMap(K key, Map<MK, V> optional) {
        Map<MK, V> map = getMap(key);
        return map == null ? optional : map;
    }

    default <MK, V> Map<MK, V> getMap(K key, Class<MK> keyType, Class<V> valueType, Map<MK, V> optional) {
        Map<MK, V> map = getMap(key, keyType, valueType);
        return map == null ? optional : map;
    }

    default <V> Map<String, V> getMap(K key, Class<V> valueType, Map<String, V> optional) {
        Map<String, V> map = getMap(key, valueType);
        return map == null ? optional : map;
    }

    default <E> List<E> getList(K key, List<E> optional) {
        List<E> list = getList(key);
        return list == null ? optional : list;
    }

    default <E> List<E> getList(K key, Class<E> elementType, List<E> optional) {
        List<E> list = getList(key, elementType);
        return list == null ? optional : list;
    }

    default <E> Set<E> getSet(K key, Set<E> optional) {
        Set<E> list = getSet(key);
        return list == null ? optional : list;
    }

    default <E> Set<E> getSet(K key, Class<E> elementType, Set<E> optional) {
        Set<E> list = getSet(key, elementType);
        return list == null ? optional : list;
    }
}
