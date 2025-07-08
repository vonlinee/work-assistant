package org.workassistant.util.collection;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * NOTE: copied from android.os.util.ArrayMap.
 * ArrayMap是一个通用的key->value映射类型的数据结构，相比传统的java.util.HashMap，内存利用效率更高
 * <p>
 * 本实现基于Android平台的{@code android.util.ArrayMap}
 * 它将其映射保存在数组数据结构中，一个int数组，存放有所有key的hash码，一个存放key/value对像
 * 这个设计使得不用为每个放进map中的项目都创建额外的一个对象
 * 它还试图更积极地控制这些数组的规模增长（因为增长它们只需要复制数组中的条目，而不是重建整个hash map）
 * 而java.util.HashMap扩容时先对底层的数组进行扩容，然后需要在扩容后的新数组上重建整个hash map
 *
 * <p>如果您不需要这里（指此ArrayMap）提供的标准Java容器API（比如迭代器等），可考虑改用｛@link AbstractArrayMap｝.
 * 需要自己手动实现AbstractArrayMap，AbstractArrayMap不属于Map分支</p>
 *
 * <p>Note that this implementation is not intended to be appropriate for data structures
 * that may contain large numbers of items.  It is generally slower than a traditional
 * HashMap, since lookups require a binary search and adds and removes require inserting
 * and deleting entries in the array.  For containers holding up to hundreds of items,
 * the performance difference is not significant, less than 50%.</p>
 *
 * <p>Because this container is intended to better balance memory use, unlike most other
 * standard Java containers it will shrink its array as items are removed from it.  Currently
 * you have no control over this shrinking -- if you set a capacity and then remove an
 * item, it may reduce the capacity to better match the current size.  In the future an
 * explicit call to set the capacity should turn off this aggressive shrinking behavior.</p>
 * <p>
 * 数据量比较小，并且需要频繁的使用Map存储数据的时候，推荐使用ArrayMap，而且两者查询速度也不会相差很多
 * 数据量比较大的时候，则推荐使用HashMap
 * 具体的标准没有测过
 */
public class ArrayMap<K, V> extends AbstractArrayMap<K, V> implements Map<K, V> {

    IndexedMap<K, V> mCollections;

    public ArrayMap() {
        super();
    }

    /**
     * Create a new ArrayMap with a given initial capacity.
     */
    public ArrayMap(int capacity) {
        super(capacity);
    }

    /**
     * Create a new ArrayMap with the mappings from the given ArrayMap.
     */
    public ArrayMap(AbstractArrayMap<K, V> map) {
        super(map);
    }

    /**
     * writing standard Java collection interfaces to a data structure like {@link ArrayMap}
     *
     * @return IndexedMap
     */
    private IndexedMap<K, V> getCollection() {
        if (mCollections == null) {
            mCollections = new IndexedMap<>() {
                @Override
                protected int colGetSize() {
                    return mSize;
                }

                @Override
                protected Object colGetEntry(int index, int offset) {
                    return mArray[(index << 1) + offset];
                }

                @Override
                protected int colIndexOfKey(Object key) {
                    return indexOfKey(key);
                }

                @Override
                protected int colIndexOfValue(Object value) {
                    return indexOfValue(value);
                }

                @Override
                protected Map<K, V> colGetMap() {
                    return ArrayMap.this;
                }

                @Override
                protected void colPut(K key, V value) {
                    put(key, value);
                }

                @Override
                protected V colSetValue(int index, V value) {
                    return setValueAt(index, value);
                }

                @Override
                protected void colRemoveAt(int index) {
                    removeAt(index);
                }

                @Override
                protected void colClear() {
                    ArrayMap.this.clear();
                }
            };
        }
        return mCollections;
    }

    /**
     * Determine if the array map contains all the keys in the given collection.
     *
     * @param collection The collection whose contents are to be checked against.
     * @return Returns true if this array map contains a key for every entry
     * in <var>collection</var>, else returns false.
     */
    public boolean containsAll(Collection<?> collection) {
        return IndexedMap.containsAllHelper(this, collection);
    }

    /**
     * Perform a {@link #put(Object, Object)} of all key/value pairs in <var>map</var>
     *
     * @param map The map whose contents are to be retrieved.
     */
    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        ensureCapacity(mSize + map.size());
        for (Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Remove all keys in the array map that exist in the given collection.
     *
     * @param collection The collection whose contents are to be used to remove keys.
     * @return Returns true if any keys were removed from the array map, else false.
     */
    public boolean removeAll(Collection<?> collection) {
        return IndexedMap.removeAllKeys(this, collection);
    }

    /**
     * Remove all keys in the array map that do <b>not</b> exist in the given collection.
     *
     * @param collection The collection whose contents are to be used to determine which
     *                   keys to keep.
     * @return Returns true if any keys were removed from the array map, else false.
     */
    public boolean retainAll(Collection<?> collection) {
        return IndexedMap.retainAllKeys(this, collection);
    }

    /**
     * Return a {@link Set} for iterating over and interacting with all mappings
     * in the array map.
     *
     * <p><b>Note:</b> this is a very inefficient way to access the array contents, it
     * requires generating a number of temporary objects.</p>
     *
     * <p><b>Note:</b></p> the semantics of this
     * Set are subtly different from that of a {@link java.util.HashMap}: most important,
     * the {@link Entry Map.Entry} object returned by its iterator is a single
     * object that exists for the entire iterator, so you can <b>not</b> hold on to it
     * after calling {@link java.util.Iterator#next() Iterator.next}.</p>
     */
    @Override
    public Set<Entry<K, V>> entrySet() {
        return getCollection().getEntrySet();
    }

    /**
     * Return a {@link Set} for iterating over and interacting with all keys
     * in the array map.
     *
     * <p><b>Note:</b> this is a fairly inefficient way to access the array contents, it
     * requires generating a number of temporary objects.</p>
     */
    @Override
    public Set<K> keySet() {
        return getCollection().getKeySet();
    }

    /**
     * Return a {@link Collection} for iterating over and interacting with all values
     * in the array map.
     *
     * <p><b>Note:</b> this is a fairly inefficient way to access the array contents, it
     * requires generating a number of temporary objects.</p>
     */
    @Override
    public Collection<V> values() {
        return getCollection().getValues();
    }
}
