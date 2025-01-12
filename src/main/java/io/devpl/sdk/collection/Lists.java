package io.devpl.sdk.collection;

import io.devpl.sdk.util.ArrayUtils;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * List工具类
 */
public final class Lists {

    private Lists() {
    }

    public static <E> List<E> empty() {
        return Collections.emptyList();
    }

    @SafeVarargs
    public static <E> List<E> of(E... elements) {
        return linkOf(elements);
    }

    @SafeVarargs
    public static <E> ArrayList<E> arrayOf(E... elements) {
        return new ArrayList<>(ArrayUtils.asArrayList(elements));
    }

    /**
     * 从Map构造一个List
     *
     * @param map         数据Map
     * @param constructor 构造函数，接受Map的Key和Value对象
     * @param <K>         Map的key类型
     * @param <V>         Map的value类型
     * @param <E>         List元素类型
     * @return List
     */
    public static <K, V, E> List<E> arrayOf(Map<K, V> map, BiFunction<K, V, E> constructor) {
        if (map == null || map.isEmpty()) {
            return new ArrayList<>();
        }
        List<E> list = new ArrayList<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            list.add(constructor.apply(entry.getKey(), entry.getValue()));
        }
        return list;
    }

    public static <E> ArrayList<E> arrayOf(Iterator<E> iterator) {
        if (iterator == null) return new ArrayList<>();
        ArrayList<E> list = new ArrayList<>();
        while (iterator.hasNext()) {
            list.add(iterator.next());
        }
        return list;
    }

    @SafeVarargs
    public static <E> LinkedList<E> linkOf(E... elements) {
        if (elements == null) return new LinkedList<>();
        return new LinkedList<>(ArrayUtils.asArrayList(elements));
    }

    /**
     * 不可变的List
     *
     * @param elements 初始元素列表
     * @param <E>      元素类型
     * @return 不可变的list
     */
    @SafeVarargs
    public static <E> List<E> immutableOf(E... elements) {
        if (elements == null || elements.length == 0) {
            return Collections.emptyList();
        }
        return List.of(elements);
    }

    public static ArrayList<Integer> arrayOf(int[] arr) {
        ArrayList<Integer> intList = new ArrayList<>(arr.length);
        for (int i = 0; i < arr.length; i++) {
            intList.add(i, arr[i]);
        }
        return intList;
    }

    public static ArrayList<Long> arrayOf(long[] arr) {
        ArrayList<Long> intList = new ArrayList<>(arr.length);
        for (int i = 0; i < arr.length; i++) {
            intList.add(i, arr[i]);
        }
        return intList;
    }

    public static ArrayList<Double> arrayOf(double[] arr) {
        ArrayList<Double> intList = new ArrayList<>(arr.length);
        for (int i = 0; i < arr.length; i++) {
            intList.add(i, arr[i]);
        }
        return intList;
    }

    public static ArrayList<Short> arrayOf(short[] arr) {
        ArrayList<Short> intList = new ArrayList<>(arr.length);
        for (int i = 0; i < arr.length; i++) {
            intList.add(i, arr[i]);
        }
        return intList;
    }

    public static <T> boolean isEmpty(List<T> list) {
        return list == null || list.isEmpty();
    }

    /**
     * 取第一个满足条件的元素
     *
     * @param list      list
     * @param condition 条件
     * @param <E>       元素类型
     * @return 第一个元素
     */
    public static <E> E first(List<E> list, Predicate<E> condition) {
        if (isEmpty(list)) {
            return null;
        }
        for (E element : list) {
            if (condition.test(element)) {
                return element;
            }
        }
        return null;
    }
}
