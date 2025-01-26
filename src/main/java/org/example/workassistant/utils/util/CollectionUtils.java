package org.example.workassistant.utils.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.PageUtil;
import cn.hutool.core.util.TypeUtil;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * 集合操作工具类
 * 简化stream操作
 *
 * @since 17
 */
public abstract class CollectionUtils {

    /**
     * Return {@code true} if the supplied Collection is {@code null} or empty.
     * Otherwise, return {@code false}.
     *
     * @param collection the Collection to check
     * @return whether the given Collection is empty
     */
    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 所有都为空时返回true
     *
     * @param collections 多个集合
     * @return 所有都为空时返回true
     */
    public static boolean isEmpty(Collection<?>... collections) {
        if (collections == null) {
            return true;
        }
        if (collections.length == 1) {
            return isEmpty(collections[0]);
        }
        for (Collection<?> collection : collections) {
            if (!isEmpty(collection)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return {@code true} if the supplied Collection is {@code null} or empty.
     * Otherwise, return {@code false}.
     *
     * @param map the Collection to check
     * @return whether the given Collection is empty
     */
    public static boolean isEmpty(Map<?, ?> map) {
        return (map == null || map.isEmpty());
    }

    /**
     * 分组
     *
     * @param list       列表
     * @param classifier 分组的key
     * @param <K>        分组Key类型
     * @param <E>        列表元素
     * @return 分组Map
     */
    public static <K, E> Map<K, List<E>> groupingBy(Collection<E> list, Function<? super E, ? extends K> classifier) {
        return list.stream().collect(Collectors.groupingBy(classifier));
    }

    /**
     * 对list进行排序
     *
     * @param list       list集合
     * @param comparator 元素比较器
     * @param <E>        元素类型
     * @return 排序，集合为null则返回空集合
     */
    
    public static <E> List<E> sort(List<E> list, Comparator<E> comparator) {
        if (list == null) {
            return Collections.emptyList();
        }
        list.sort(comparator);
        return list;
    }

    public static <E, T extends Comparable<T>> List<E> sortBy(Collection<E> list, Function<E, T> keyExtractor) {
        return sortBy(list, keyExtractor, true);
    }

    public static <E, T extends Comparable<T>> List<E> sortBy(Collection<E> list, Function<E, T> keyExtractor, boolean asc) {
        return list.stream().sorted(asc ? Comparator.comparing(keyExtractor) : Comparator.comparing(keyExtractor).reversed()).collect(Collectors.toList());
    }

    public static <E> List<E> filter(Collection<E> list, Predicate<? super E> filter) {
        return list.stream().filter(filter).collect(Collectors.toList());
    }

    public static <E> int sumInt(Collection<E> list, ToIntFunction<? super E> mapper) {
        return sumInt(list, true, mapper);
    }

    /**
     * 将集合元素映射为 list
     * 仅仅支持单次映射
     *
     * @param collection 原集合
     * @param mapper     映射
     * @param <E>        原集合元素类型
     * @param <T>        映射后的集合元素类型
     * @return 映射后的集合
     */
    public static <E, T> List<T> toList(Collection<E> collection, Function<E, T> mapper) {
        return collection.stream().map(mapper).collect(Collectors.toList());
    }

    /**
     * 将集合元素映射为 list
     * 仅仅支持2次映射
     *
     * @param collection 原集合
     * @param mapper1    映射1
     * @param mapper2    映射2
     * @param <E>        原集合元素类型
     * @param <T>        映射后的集合元素类型
     * @return 映射后的集合
     */
    public static <E, T, F> List<F> toList(Collection<E> collection, Function<E, T> mapper1, Function<T, F> mapper2) {
        return collection.stream().map(mapper1).map(mapper2).collect(Collectors.toList());
    }

    /**
     * 先经过一层map，再进行flatMap操作
     *
     * @param collection 愿集合
     * @param mapper     映射逻辑
     * @param <E>        原集合数据类型
     * @param <T>        映射后的集合元素类型
     * @param <C>        映射后的集合类型
     * @return 映射后的list集合
     */
    public static <E, T, C extends Collection<T>> List<T> toFlatList(Collection<E> collection, Function<E, C> mapper) {
        if (isEmpty(collection)) {
            return Collections.emptyList();
        }
        return collection.stream().map(mapper).flatMap(Collection::stream).toList();
    }

    public static <E, T> Set<T> toSet(Collection<E> list, Function<E, T> mapper) {
        if (isEmpty(list)) {
            return Collections.emptySet();
        }
        return list.stream().map(mapper).collect(Collectors.toSet());
    }

    public static <E, T> Set<T> toSet(Collection<E> list, Function<E, T> mapper, Predicate<? super T> condition) {
        return list.stream().map(mapper).filter(condition).collect(Collectors.toSet());
    }

    public static <E, K, V> Map<K, V> toMap(Collection<E> list, Function<? super E, ? extends K> keyMapper, Function<? super E, ? extends V> valueMapper) {
        return list.stream().collect(Collectors.toMap(keyMapper, valueMapper));
    }

    /**
     * 对枚举类进行映射
     *
     * @param enumClass   枚举类类型
     * @param keyMapper   key
     * @param valueMapper value
     * @param <E>         枚举类类型
     * @param <K>         key类型
     * @param <V>         value类型
     * @return Map
     */
    
    public static <E extends Enum<E>, K, V> Map<K, V> toMap(Class<E> enumClass, Function<E, ? extends K> keyMapper, Function<E, ? extends V> valueMapper) {
        return toMap(EnumSet.allOf(enumClass), keyMapper, valueMapper);
    }

    /**
     * 将单列集合映射为map
     *
     * @param list      列表
     * @param keyMapper 键映射器
     * @return {@link Map}<{@link K}, {@link E}>
     */
    
    public static <E, K> Map<K, E> toMap(Collection<E> list, Function<? super E, ? extends K> keyMapper) {
        if (isEmpty(list)) {
            return Collections.emptyMap();
        }
        return list.stream().collect(Collectors.toMap(keyMapper, Function.identity()));
    }

    /**
     * 求和
     *
     * @param collection    集合
     * @param filterNull    是否过滤null元素，即null元素不参与求和
     * @param toIntFunction int function
     * @param <E>           元素类型
     * @return 求和
     */
    public static <E> int sumInt(Collection<E> collection, boolean filterNull, ToIntFunction<? super E> toIntFunction) {
        if (isEmpty(collection)) {
            return 0;
        }
        int sum = 0;
        for (E element : collection) {
            if (filterNull && element == null) {
                continue;
            }
            sum += toIntFunction.applyAsInt(element);
        }
        return sum;
    }

    public static <E> long sumLong(Collection<E> list, ToLongFunction<? super E> toIntFunction) {
        if (isEmpty(list)) {
            return 0;
        }
        long sum = 0;
        for (E element : list) {
            sum += toIntFunction.applyAsLong(element);
        }
        return sum;
    }

    public static <E, V> String join(List<E> list, Function<E, V> mapper) {
        return join(list, mapper, String::valueOf);
    }

    public static <E, V> String join(List<E> list, Function<E, V> mapper, Function<V, String> toStringMapper) {
        return join(list, mapper, ",");
    }

    public static <E, V> String join(List<E> list, Function<E, V> mapper, CharSequence delimiter) {
        return join(list, mapper, delimiter, String::valueOf);
    }

    public static <E, V> String join(List<E> list, Function<E, V> mapper, CharSequence delimiter, Function<V, String> toStringMapper) {
        return list.stream().map(mapper).map(toStringMapper).collect(Collectors.joining(delimiter));
    }

    public static <K, E, T> T treeify(Collection<E> collection, TreeBuilder<K, E, T> builder) {
        return builder.apply(collection);
    }

    public static <E, T, U extends Comparable<? super U>> T min(Collection<E> collection, Function<E, T> key, Function<? super T, ? extends U> keyExtractor) {
        if (collection == null) {
            return null;
        }
        return collection.stream().map(key).min(Comparator.comparing(keyExtractor)).orElse(null);
    }

    public static <E, T, U extends Comparable<? super U>> T min(Collection<E> collection, Function<E, T> key, Function<? super T, ? extends U> keyExtractor, T defaults) {
        if (collection == null) {
            return defaults;
        }
        return collection.stream().map(key).min(Comparator.comparing(keyExtractor)).orElse(defaults);
    }

    public static <E, T, U extends Comparable<? super U>> T max(Collection<E> collection, Function<E, T> key, Function<? super T, ? extends U> keyExtractor) {
        if (collection == null) {
            return null;
        }
        return collection.stream().map(key).max(Comparator.comparing(keyExtractor)).orElse(null);
    }

    public static <E, T, U extends Comparable<? super U>> T max(Collection<E> collection, Function<E, T> key, Function<? super T, ? extends U> keyExtractor, T defaults) {
        if (collection == null) {
            return defaults;
        }
        return collection.stream().map(key).max(Comparator.comparing(keyExtractor)).orElse(defaults);
    }

    /**
     * 集合添加多个元素，支持数组和可变参数
     *
     * @param coll 集合
     * @param arr  新加的元素
     * @param <E>  元素类型
     */
    public static <E> void addAll(Collection<E> coll, E[] arr) {
        if (coll == null || arr == null || arr.length == 0) {
            return;
        }
        coll.addAll(ArrayUtils.asList(arr));
    }

    /**
     * 将指定对象全部加入到集合中<br>
     * 提供的对象如果为集合类型，会自动转换为目标元素类型<br>
     *
     * @param <T>        元素类型
     * @param collection 被加入的集合
     * @param value      对象，可能为Iterator、Iterable、Enumeration、Array
     * @return 被加入集合
     */
    public static <T> Collection<T> addAll1(Collection<T> collection, Object value) {
        return CollUtil.addAll(collection, value, TypeUtil.getTypeArgument(collection.getClass()));
    }

    /**
     * 集合添加另外一个集合的所有元素
     *
     * @param coll              集合
     * @param anotherCollection 新加的元素集合
     * @param <E>               元素类型
     */
    public static <E> void addAll(Collection<E> coll, Collection<? extends E> anotherCollection) {
        if (coll == null || isEmpty(anotherCollection)) {
            return;
        }
        coll.addAll(anotherCollection);
    }

    /**
     * 集合添加多个元素，支持数组和可变参数
     *
     * @param intColl int集合
     * @param nums    新加的元素
     */
    public static void addAll(Collection<Integer> intColl, int[] nums) {
        if (intColl == null || nums == null || nums.length == 0) {
            return;
        }
        addAll(intColl, ArrayUtils.toIntegerArray(nums));
    }

    /**
     * 将集合先做一层映射，然后按条件统计映射后的集合元素
     *
     * @param collection 集合
     * @param condition  条件
     * @param <E>        集合元素类型
     * @return 统计数量，返回Int
     */
    public static <E, T> long count(Collection<E> collection, Function<E, T> key, Predicate<T> condition) {
        return collection.stream().map(key).filter(condition).count();
    }

    /**
     * 按条件统计某个字段
     *
     * @param collection 集合
     * @param condition  条件
     * @param <E>        集合元素类型
     * @return 统计数量，返回Int
     */
    public static <E> int countInt(Collection<E> collection, Predicate<E> condition) {
        if (isEmpty(collection)) {
            return 0;
        }
        return (int) collection.stream().filter(condition).count();
    }

    /**
     * 按条件统计元素个数
     *
     * @param collection 集合
     * @param condition  条件
     * @param <E>        集合元素类型
     * @return 统计数量，返回long
     */
    public static <E> long countLong(Collection<E> collection, Predicate<E> condition) {
        if (isEmpty(collection)) {
            return 0;
        }
        return collection.stream().filter(condition).count();
    }

    /**
     * 将集合平铺
     *
     * @param collection 集合，元素也为集合
     * @param collector  Collector, 平铺后的集合类型, {@link Collectors#toList()} {@link Collectors#toSet()}
     * @param <E>        集合元素类型
     * @param <R>        平铺后的集合类型
     * @return 平铺后的集合
     */
    public static <E, R extends Collection<E>> R flatten(Collection<? extends Collection<E>> collection, Collector<E, ?, R> collector) {
        return collection.stream().flatMap(Collection::stream).collect(collector);
    }

    /**
     * 是否有任何一个元素能匹配指定的条件
     *
     * @param collection 巡逻队
     * @param condition  条件
     * @return boolean
     */
    public static <E> boolean anyMatch(Collection<E> collection, Predicate<E> condition) {
        if (isEmpty(collection)) {
            return false;
        }
        for (E team : collection) {
            if (condition.test(team)) {
                return true;
            }
        }
        return false;
    }

    /**
     * nullsafe版本removeAll
     * 注意：直接在参数的集合上进行修改元素
     *
     * @param coll1 集合1
     * @param coll2 集合2
     * @param <E>   集合元素类型
     * @return 如果参数为空，则返回原参数
     */
    @Nullable
    public static <C extends Collection<E>, E> C removeAll(C coll1, Collection<E> coll2) {
        if (isEmpty(coll1) || isEmpty(coll2)) {
            return coll1;
        }
        coll1.removeAll(coll2);
        return coll1;
    }

    /**
     * 求差集
     *
     * @param coll1 集合1
     * @param coll2 集合2
     * @param <E>   集合元素类型
     * @return 返回新的list，不影响作为参数的两个集合
     */
    public static <C extends Collection<E>, E> List<E> differ(C coll1, C coll2) {
        if (isEmpty(coll1)) {
            return coll2 == null ? new ArrayList<>() : new ArrayList<>(coll2);
        }
        if (isEmpty(coll2)) {
            return new ArrayList<>(coll1);
        }
        // 创建两个集合
        List<E> list = new ArrayList<>();
        for (E e : coll1) {
            if (!coll2.contains(e)) {
                list.add(e);
            }
        }
        return list;
    }

    /**
     * 使用集合覆盖指定的集合元素
     *
     * @param set        待覆盖的集合元素
     * @param collection 集合
     * @param <T>        元素类型
     * @return 覆盖后的集合，最终返回不为空，集合元素以参数collection为准
     */
    public static <S extends Collection<T>, T> S setAll(S set, Collection<T> collection, Supplier<S> empty) {
        if (!isEmpty(set)) {
            set.clear();
        } else if (empty != null) {
            set = empty.get();
        }
        set.addAll(collection);
        return set;
    }

    /**
     * 集合映射字段为数组
     *
     * @param collection 集合
     * @param mapper     字段映射
     * @param array      指定数组构造
     * @param <E>        集合类型
     * @param <T>        数组类型
     * @return 数组
     */
    public static <E, T> T[] toArray(Collection<E> collection, Function<E, T> mapper, IntFunction<T[]> array) {
        return collection.stream().map(mapper).toArray(array);
    }

    /**
     * 合并两个Map对象
     *
     * @param target map1
     * @param source map2
     * @param <K>    key类型
     * @param <V>    value类型
     */
    public static <K, V> void merge(Map<K, V> target, Map<K, V> source) {
        if (target == null || isEmpty(source)) {
            return;
        }
        // 合并为null的值
        for (Map.Entry<K, V> entry : target.entrySet()) {
            if (entry.getValue() == null) {
                V val = source.get(entry.getKey());
                if (val != null) {
                    entry.setValue(val);
                }
            }
        }
        for (Map.Entry<K, V> entry : source.entrySet()) {
            if (!target.containsKey(entry.getKey())) {
                target.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * @param target             general an empty collection
     * @param anotherCollections another collections to be moved into the collection.
     * @param <C>
     * @param <E>
     * @param <NE>
     * @return the target
     */
    @SafeVarargs
    public static <C extends Collection<E>, E, NE extends Collection<? extends E>> C addAll( C target, NE... anotherCollections) {
        for (NE anotherCollection : anotherCollections) {
            target.addAll(anotherCollection);
        }
        return target;
    }

    public static <K, V> Collection<V> values(Map<K, V> map, Collection<K> keys) {
        List<V> list = new ArrayList<>();
        for (K key : keys) {
            list.add(map.get(key));
        }
        return list;
    }

    public static <T> boolean isNotEmpty(Collection<T> collection) {
        return !isEmpty(collection);
    }

    public static <T> boolean isNotEmpty(Map<?, ?> map) {
        return !isEmpty(map);
    }

    public static <C extends List<T>, T> C reverse(C collection) {
        Collections.reverse(collection);
        return collection;
    }

    /**
     * 新建一个List<br>
     * 提供的参数为null时返回空{@link ArrayList}
     *
     * @param <T>           集合元素类型
     * @param useLinkedList 是否新建LinkedList
     * @param iter          {@link Iterator}
     * @return ArrayList对象
     */
    public static <T> List<T> asList(Iterator<T> iter, boolean useLinkedList) {
        final List<T> list = useLinkedList ? new LinkedList<>() : new ArrayList<>();
        if (null != iter) {
            while (iter.hasNext()) {
                list.add(iter.next());
            }
        }
        return list;
    }

    public static <T> List<T> asList(Iterator<T> iter) {
        return asList(iter, false);
    }

    public static <K, V> void removeAny(Map<K, V> map, K[] keys) {
        if (ArrayUtils.isEmpty(keys)) {
            return;
        }
        for (K key : keys) {
            map.remove(key);
        }
    }

    public static <T> boolean containsAny(Collection<T> collection, List<T> targets) {
        return CollUtil.containsAny(collection, targets);
    }

    /**
     * 对指定List分页取值
     *
     * @param <T>      集合元素类型
     * @param pageNo   页码，第一页的页码取决于{@link PageUtil#getFirstPageNo()}，默认0
     * @param pageSize 每页的条目数
     * @param list     列表
     * @return 分页后的段落内容
     * @since 4.1.20
     */
    public static <T> List<T> page(int pageNo, int pageSize, List<T> list) {
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>(0);
        }

        int resultSize = list.size();
        // 每页条目数大于总数直接返回所有
        if (resultSize <= pageSize) {
            if (pageNo < (PageUtil.getFirstPageNo() + 1)) {
                return new ArrayList<>(list);
            } else {
                // 越界直接返回空
                return new ArrayList<>(0);
            }
        }
        // 相乘可能会导致越界 临时用long
        if (((long) (pageNo - PageUtil.getFirstPageNo()) * pageSize) > resultSize) {
            // 越界直接返回空
            return new ArrayList<>(0);
        }

        final int[] startEnd = PageUtil.transToStartEnd(pageNo, pageSize);
        if (startEnd[1] > resultSize) {
            startEnd[1] = resultSize;
            if (startEnd[0] > startEnd[1]) {
                return new ArrayList<>(0);
            }
        }

        return sub(list, startEnd[0], startEnd[1], 1);
    }

    /**
     * 截取集合的部分<br>
     * 此方法与{@link List#subList(int, int)} 不同在于子列表是新的副本，操作子列表不会影响原列表。
     *
     * @param <T>   集合元素类型
     * @param list  被截取的数组
     * @param start 开始位置（包含）
     * @param end   结束位置（不包含）
     * @param step  步进
     * @return 截取后的数组，当开始位置超过最大时，返回空的List
     * @since 4.0.6
     */
    public static <T> List<T> sub(List<T> list, int start, int end, int step) {
        if (list == null) {
            return null;
        }

        if (list.isEmpty()) {
            return new ArrayList<>(0);
        }

        final int size = list.size();
        if (start < 0) {
            start += size;
        }
        if (end < 0) {
            end += size;
        }
        if (start == size) {
            return new ArrayList<>(0);
        }
        if (start > end) {
            int tmp = start;
            start = end;
            end = tmp;
        }
        if (end > size) {
            if (start >= size) {
                return new ArrayList<>(0);
            }
            end = size;
        }

        if (step < 1) {
            step = 1;
        }

        final List<T> result = new ArrayList<>();
        for (int i = start; i < end; i += step) {
            result.add(list.get(i));
        }
        return result;
    }
}
