package org.workassistant.util.collection;

import java.util.ConcurrentModificationException;
import java.util.Map;
import java.util.Objects;

/**
 * NOTE: copied from android.os.util.AbstractArrayMap
 * Base implementation of {@link ArrayMap} that doesn't include any standard Java
 * container API interoperability.(不兼容Java容器API，即不继承自Map)  These features are
 * generally heavier-weight ways to interact with the container, so discouraged,
 * but they can be useful to make it easier to use as a drop-in replacement for HashMap.
 * If you don't need them, this class can be preferable since it doesn't bring in any of
 * the implementation of those APIs, allowing that code to be stripped by ProGuard.
 */
public abstract class AbstractArrayMap<K, V> {

    static final int[] EMPTY_INTS = new int[0];
    // static final long[] EMPTY_LONGS = new long[0];
    static final Object[] EMPTY_OBJECTS = new Object[0];
    /**
     * Attempt to spot concurrent modifications to this data structure.
     * <p>
     * It's best-effort, but any time we can throw something more diagnostic than an
     * ArrayIndexOutOfBoundsException deep in the ArrayMap internals it's going to
     * save a lot of development time.
     * <p>
     * Good times to look for CME include after any allocArrays() call and at the end of
     * functions that change mSize (put/remove/clear).
     */
    private static final boolean CONCURRENT_MODIFICATION_EXCEPTIONS = true;
    /**
     * The minimum amount by which the capacity of a ArrayMap will increase.
     * This is tuned to be relatively space-efficient.
     */
    private static final int BASE_SIZE = 4;
    /**
     * Maximum number of entries to have in array caches.
     */
    private static final int CACHE_SIZE = 10;
    /**
     * Caches of small array objects to avoid spamming garbage.  The cache
     * Object[] variable is a pointer to a linked list of array objects.
     * The first entry in the array is a pointer to the next array in the
     * list; the second entry is a pointer to the int[] hash code array for it.
     */
    static Object[] mBaseCache;
    static int mBaseCacheSize;
    static Object[] mTwiceBaseCache;
    static int mTwiceBaseCacheSize;
    transient int[] mHashes;
    transient int mSize;
    Object[] mArray;

    /**
     * Create a new empty ArrayMap.  The default capacity of an array map is 0, and
     * will grow once items are added to it.
     */
    public AbstractArrayMap() {
        mHashes = EMPTY_INTS;
        mArray = EMPTY_OBJECTS;
        mSize = 0;
    }

    /**
     * Create a new ArrayMap with a given initial capacity.
     */
    @SuppressWarnings("NullAway") // allocArrays initializes mHashes and mArray.
    public AbstractArrayMap(int capacity) {
        if (capacity == 0) {
            mHashes = EMPTY_INTS;
            mArray = EMPTY_OBJECTS;
        } else {
            allocArrays(capacity);
        }
        mSize = 0;
    }

    /**
     * Create a new ArrayMap with the mappings from the given ArrayMap.
     */
    public AbstractArrayMap(AbstractArrayMap<K, V> map) {
        this();
        if (map != null) {
            putAll(map);
        }
    }

    private static int binarySearchHashes(int[] hashes, int N, int hash) {
        try {
            return binarySearch(hashes, N, hash);
        } catch (ArrayIndexOutOfBoundsException e) {
            if (CONCURRENT_MODIFICATION_EXCEPTIONS) {
                throw new ConcurrentModificationException();
            } else {
                throw e; // the cache is poisoned at this point, there's not much we can do
            }
        }
    }

    // This is Arrays.binarySearch(), but doesn't do any argument validation.
    static int binarySearch(int[] array, int size, int value) {
        int lo = 0;
        int hi = size - 1;
        while (lo <= hi) {
            int mid = (lo + hi) >>> 1;
            int midVal = array[mid];
            if (midVal < value) {
                lo = mid + 1;
            } else if (midVal > value) {
                hi = mid - 1;
            } else {
                return mid;  // value found
            }
        }
        return ~lo;  // value not present
    }

    @SuppressWarnings("ArrayToString")
    private static void freeArrays(final int[] hashes, final Object[] array, final int size) {
        if (hashes.length == (BASE_SIZE * 2)) {
            synchronized (AbstractArrayMap.class) {
                if (mTwiceBaseCacheSize < CACHE_SIZE) {
                    array[0] = mTwiceBaseCache;
                    array[1] = hashes;
                    for (int i = (size << 1) - 1; i >= 2; i--) {
                        array[i] = null;
                    }
                    mTwiceBaseCache = array;
                    mTwiceBaseCacheSize++;
                }
            }
        } else if (hashes.length == BASE_SIZE) {
            synchronized (AbstractArrayMap.class) {
                if (mBaseCacheSize < CACHE_SIZE) {
                    array[0] = mBaseCache;
                    array[1] = hashes;
                    for (int i = (size << 1) - 1; i >= 2; i--) {
                        array[i] = null;
                    }
                    mBaseCache = array;
                    mBaseCacheSize++;
                }
            }
        }
    }

    /**
     * 查找key及其hashcode是否存在
     *
     * @param key  key
     * @param hash key的hashcode
     * @return key是否存在
     */
    int indexOf(Object key, int hash) {
        final int N = mSize;
        // Important fast case: if nothing is in here, nothing to look for.
        if (N == 0) return ~0;
        // 二分查找
        int index = binarySearchHashes(mHashes, N, hash);
        // 如果没有找到hashcode，则不存在该key对应的Entry
        if (index < 0) {
            return index;
        }
        // 存在该key对应的Entry
        if (key.equals(mArray[index << 1])) {
            return index;
        }
        // hash相等但是equals不相等
        // 1.在该索引之后继续查找匹配的key。
        int end;
        for (end = index + 1; end < N && mHashes[end] == hash; end++) {
            if (key.equals(mArray[end << 1])) return end;
        }
        // 2.在该索引之前继续查找匹配的key。 Search for a matching key before the index.
        for (int i = index - 1; i >= 0 && mHashes[i] == hash; i--) {
            if (key.equals(mArray[i << 1])) return i;
        }
        // Key not found -- return negative value indicating where a
        // new entry for this key should go.  We use the end of the
        // hash chain to reduce the number of array entries that will
        // need to be copied when inserting.
        return ~end;
    }

    int indexOfNull() {
        final int N = mSize;
        // Important fast case: if nothing is in here, nothing to look for.
        if (N == 0) {
            return ~0;
        }
        int index = binarySearchHashes(mHashes, N, 0);

        // If the hash code wasn't found, then we have no entry for this key.
        if (index < 0) {
            return index;
        }

        // If the key at the returned index matches, that's what we want.
        if (null == mArray[index << 1]) {
            return index;
        }

        // Search for a matching key after the index.
        int end;
        for (end = index + 1; end < N && mHashes[end] == 0; end++) {
            if (null == mArray[end << 1]) return end;
        }

        // Search for a matching key before the index.
        for (int i = index - 1; i >= 0 && mHashes[i] == 0; i--) {
            if (null == mArray[i << 1]) return i;
        }
        // Key not found -- return negative value indicating where a
        // new entry for this key should go.  We use the end of the
        // hash chain to reduce the number of array entries that will
        // need to be copied when inserting.
        return ~end;
    }

    @SuppressWarnings("ArrayToString")
    private void allocArrays(final int size) {
        if (size == (BASE_SIZE * 2)) {
            synchronized (AbstractArrayMap.class) {
                if (mTwiceBaseCache != null) {
                    final Object[] array = mTwiceBaseCache;
                    mArray = array;
                    mTwiceBaseCache = (Object[]) array[0];
                    mHashes = (int[]) array[1];
                    array[0] = array[1] = null;
                    mTwiceBaseCacheSize--;
                    return;
                }
            }
        } else if (size == BASE_SIZE) {
            synchronized (AbstractArrayMap.class) {
                if (mBaseCache != null) {
                    final Object[] array = mBaseCache;
                    mArray = array;
                    mBaseCache = (Object[]) array[0];
                    mHashes = (int[]) array[1];
                    array[0] = array[1] = null;
                    mBaseCacheSize--;
                    return;
                }
            }
        }
        mHashes = new int[size];
        mArray = new Object[size << 1];
    }

    /**
     * Make the array map empty.  All storage is released.
     */
    public void clear() {
        if (mSize > 0) {
            final int[] ohashes = mHashes;
            final Object[] oarray = mArray;
            final int osize = mSize;
            mHashes = EMPTY_INTS;
            mArray = EMPTY_OBJECTS;
            mSize = 0;
            freeArrays(ohashes, oarray, osize);
        }
        if (CONCURRENT_MODIFICATION_EXCEPTIONS && mSize > 0) {
            throw new ConcurrentModificationException();
        }
    }

    /**
     * Ensure the array map can hold at least <var>minimumCapacity</var>
     * items.
     */
    public void ensureCapacity(int minimumCapacity) {
        final int osize = mSize;
        if (mHashes.length < minimumCapacity) {
            final int[] ohashes = mHashes;
            final Object[] oarray = mArray;
            allocArrays(minimumCapacity);
            if (mSize > 0) {
                System.arraycopy(ohashes, 0, mHashes, 0, osize);
                System.arraycopy(oarray, 0, mArray, 0, osize << 1);
            }
            freeArrays(ohashes, oarray, osize);
        }
        if (CONCURRENT_MODIFICATION_EXCEPTIONS && mSize != osize) {
            throw new ConcurrentModificationException();
        }
    }

    /**
     * Check whether a key exists in the array.
     *
     * @param key The key to search for.
     * @return Returns true if the key exists, else false.
     */
    public boolean containsKey(Object key) {
        return indexOfKey(key) >= 0;
    }

    /**
     * Returns the index of a key in the set.
     *
     * @param key The key to search for.
     * @return Returns the index of the key if it exists, else a negative integer.
     */
    public int indexOfKey(Object key) {
        return key == null ? indexOfNull() : indexOf(key, key.hashCode());
    }

    int indexOfValue(Object value) {
        final int N = mSize * 2;
        final Object[] array = mArray;
        if (value == null) {
            for (int i = 1; i < N; i += 2) {
                if (array[i] == null) {
                    return i >> 1;
                }
            }
        } else {
            for (int i = 1; i < N; i += 2) {
                if (value.equals(array[i])) {
                    return i >> 1;
                }
            }
        }
        return -1;
    }

    /**
     * Check whether a value exists in the array.  This requires a linear search
     * through the entire array.
     *
     * @param value The value to search for.
     * @return Returns true if the value exists, else false.
     */
    public boolean containsValue(Object value) {
        return indexOfValue(value) >= 0;
    }

    /**
     * Retrieve a value from the array.
     *
     * @param key The key of the value to retrieve.
     * @return Returns the value associated with the given key,
     * or null if there is no such key.
     */
    @SuppressWarnings("NullAway") // See inline comment.
    public V get(Object key) {
        // We pass null as the default to a function which isn't explicitly annotated as nullable.
        // Not marking the function as nullable should allow us to eventually propagate the generic
        // parameter's nullability to the caller. If we were to mark it as nullable now, we would
        // also be forced to mark the return type of that method as nullable which harms the case
        // where you are passing in a non-null default value.
        return getOrDefault(key, null);
    }

    /**
     * Retrieve a value from the array, or {@code defaultValue} if there is no mapping for the key.
     *
     * @param key          The key of the value to retrieve.
     * @param defaultValue The default mapping of the key
     * @return Returns the value associated with the given key,
     * or {@code defaultValue} if there is no mapping for the key.
     */
    @SuppressWarnings("unchecked")
    public V getOrDefault(Object key, V defaultValue) {
        final int index = indexOfKey(key);
        return index >= 0 ? (V) mArray[(index << 1) + 1] : defaultValue;
    }

    /**
     * Return the key at the given index in the array.
     *
     * @param index The desired index, must be between 0 and {@link #size()}-1.
     * @return Returns the key stored at the given index.
     */
    @SuppressWarnings("unchecked")
    public K keyAt(int index) {
        return (K) mArray[index << 1];
    }

    /**
     * Return the value at the given index in the array.
     *
     * @param index The desired index, must be between 0 and {@link #size()}-1.
     * @return Returns the value stored at the given index.
     */
    @SuppressWarnings("unchecked")
    public V valueAt(int index) {
        return (V) mArray[(index << 1) + 1];
    }

    /**
     * Set the value at a given index in the array.
     *
     * @param index The desired index, must be between 0 and {@link #size()}-1.
     * @param value The new value to store at this index.
     * @return Returns the previous value at the given index.
     */
    public V setValueAt(int index, V value) {
        index = (index << 1) + 1;
        @SuppressWarnings("unchecked")
        V old = (V) mArray[index];
        mArray[index] = value;
        return old;
    }

    /**
     * Return true if the array map contains no items.
     */
    public boolean isEmpty() {
        return mSize <= 0;
    }

    /**
     * Add a new value to the array map.
     *
     * @param key   The key under which to store the value.  <b>Must not be null.</b>  If
     *              this key already exists in the array, its value will be replaced.
     * @param value The value to store for the given key.
     * @return Returns the old value that was stored for the given key, or null if there
     * was no such key.
     */
    public V put(K key, V value) {
        final int oldSize = mSize;
        final int hash;
        int index;
        if (key == null) {
            hash = 0;
            index = indexOfNull();
        } else {
            hash = key.hashCode();
            index = indexOf(key, hash);
        }
        if (index >= 0) {
            // 存在该key，直接赋值即可
            index = (index << 1) + 1;
            @SuppressWarnings("unchecked") final V old = (V) mArray[index];
            mArray[index] = value;
            return old;
        }
        // 不存在该key
        index = ~index;
        if (oldSize >= mHashes.length) {
            // 超过8个
            final int n = oldSize >= (BASE_SIZE * 2) ? (oldSize + (oldSize >> 1)) : (oldSize >= BASE_SIZE ? (BASE_SIZE * 2) : BASE_SIZE);
            final int[] ohashes = mHashes;
            final Object[] oarray = mArray;
            allocArrays(n);
            if (CONCURRENT_MODIFICATION_EXCEPTIONS && oldSize != mSize) {
                throw new ConcurrentModificationException();
            }
            if (mHashes.length > 0) {
                System.arraycopy(ohashes, 0, mHashes, 0, ohashes.length);
                System.arraycopy(oarray, 0, mArray, 0, oarray.length);
            }
            freeArrays(ohashes, oarray, oldSize);
        }
        if (index < oldSize) {
            System.arraycopy(mHashes, index, mHashes, index + 1, oldSize - index);
            System.arraycopy(mArray, index << 1, mArray, (index + 1) << 1, (mSize - index) << 1);
        }
        if (CONCURRENT_MODIFICATION_EXCEPTIONS) {
            if (oldSize != mSize || index >= mHashes.length) {
                throw new ConcurrentModificationException();
            }
        }
        mHashes[index] = hash;
        mArray[index << 1] = key;
        mArray[(index << 1) + 1] = value;
        mSize++;
        return null;
    }

    /**
     * Perform a {@link #put(Object, Object)} of all key/value pairs in <var>array</var>
     *
     * @param array The array whose contents are to be retrieved.
     */
    public void putAll(AbstractArrayMap<? extends K, ? extends V> array) {
        final int N = array.mSize;
        ensureCapacity(mSize + N);
        if (mSize == 0) {
            if (N > 0) {
                System.arraycopy(array.mHashes, 0, mHashes, 0, N);
                System.arraycopy(array.mArray, 0, mArray, 0, N << 1);
                mSize = N;
            }
        } else {
            for (int i = 0; i < N; i++) {
                put(array.keyAt(i), array.valueAt(i));
            }
        }
    }

    /**
     * Add a new value to the array map only if the key does not already have a value, or it is
     * mapped to {@code null}.
     *
     * @param key   The key under which to store the value.
     * @param value The value to store for the given key.
     * @return Returns the value that was stored for the given key, or null if there
     * was no such key.
     */
    public V putIfAbsent(K key, V value) {
        V mapValue = get(key);
        if (mapValue == null) {
            mapValue = put(key, value);
        }
        return mapValue;
    }

    /**
     * Remove an existing key from the array map.
     *
     * @param key The key of the mapping to remove.
     * @return Returns the value that was stored under the key, or null if there
     * was no such key.
     */
    public V remove(Object key) {
        final int index = indexOfKey(key);
        if (index >= 0) {
            return removeAt(index);
        }
        return null;
    }

    /**
     * Remove an existing key from the array map only if it is currently mapped to {@code value}.
     *
     * @param key   The key of the mapping to remove.
     * @param value The value expected to be mapped to the key.
     * @return Returns true if the mapping was removed.
     */
    public boolean remove(Object key, Object value) {
        int index = indexOfKey(key);
        if (index >= 0) {
            V mapValue = valueAt(index);
            if (Objects.equals(value, mapValue)) {
                removeAt(index);
                return true;
            }
        }
        return false;
    }

    /**
     * Remove the key/value mapping at the given index.
     *
     * @param index The desired index, must be between 0 and {@link #size()}-1.
     * @return Returns the value that was stored at this index.
     */
    public V removeAt(int index) {
        final Object old = mArray[(index << 1) + 1];
        final int osize = mSize;
        final int nsize;
        if (osize <= 1) {
            // Now empty.
            freeArrays(mHashes, mArray, osize);
            mHashes = EMPTY_INTS;
            mArray = EMPTY_OBJECTS;
            nsize = 0;
        } else {
            nsize = osize - 1;
            if (mHashes.length > (BASE_SIZE * 2) && mSize < mHashes.length / 3) {
                // Shrunk enough to reduce size of arrays.  We don't allow it to
                // shrink smaller than (BASE_SIZE*2) to avoid flapping between
                // that and BASE_SIZE.
                final int n = osize > (BASE_SIZE * 2) ? (osize + (osize >> 1)) : (BASE_SIZE * 2);
                final int[] ohashes = mHashes;
                final Object[] oarray = mArray;
                allocArrays(n);
                if (CONCURRENT_MODIFICATION_EXCEPTIONS && osize != mSize) {
                    throw new ConcurrentModificationException();
                }

                if (index > 0) {
                    System.arraycopy(ohashes, 0, mHashes, 0, index);
                    System.arraycopy(oarray, 0, mArray, 0, index << 1);
                }
                if (index < nsize) {
                    System.arraycopy(ohashes, index + 1, mHashes, index, nsize - index);
                    System.arraycopy(oarray, (index + 1) << 1, mArray, index << 1, (nsize - index) << 1);
                }
            } else {
                if (index < nsize) {
                    System.arraycopy(mHashes, index + 1, mHashes, index, nsize - index);
                    System.arraycopy(mArray, (index + 1) << 1, mArray, index << 1, (nsize - index) << 1);
                }
                mArray[nsize << 1] = null;
                mArray[(nsize << 1) + 1] = null;
            }
        }
        if (CONCURRENT_MODIFICATION_EXCEPTIONS && osize != mSize) {
            throw new ConcurrentModificationException();
        }
        mSize = nsize;
        @SuppressWarnings("unchecked") final V value = (V) old;
        return value;
    }

    /**
     * Replace the mapping for {@code key} only if it is already mapped to a value.
     *
     * @param key   The key of the mapping to replace.
     * @param value The value to store for the given key.
     * @return Returns the previous mapped value or null.
     */
    public V replace(K key, V value) {
        int index = indexOfKey(key);
        if (index >= 0) {
            return setValueAt(index, value);
        }
        return null;
    }

    /**
     * Replace the mapping for {@code key} only if it is already mapped to a value.
     *
     * @param key      The key of the mapping to replace.
     * @param oldValue The value expected to be mapped to the key.
     * @param newValue The value to store for the given key.
     * @return Returns true if the value was replaced.
     */
    public boolean replace(K key, V oldValue, V newValue) {
        int index = indexOfKey(key);
        if (index >= 0) {
            V mapValue = valueAt(index);
            if (Objects.equals(oldValue, mapValue)) {
                setValueAt(index, newValue);
                return true;
            }
        }
        return false;
    }

    /**
     * Return the number of items in this array map.
     */
    public int size() {
        return mSize;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation returns false if the object is not a Map or
     * SimpleArrayMap, or if the maps have different sizes. Otherwise, for each
     * key in this map, values of both maps are compared. If the values for any
     * key are not equal, the method returns false, otherwise it returns true.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof AbstractArrayMap) {
            AbstractArrayMap<?, ?> map = (AbstractArrayMap<?, ?>) object;
            if (size() != map.size()) {
                return false;
            }

            try {
                for (int i = 0; i < mSize; i++) {
                    K key = keyAt(i);
                    V mine = valueAt(i);
                    Object theirs = map.get(key);
                    if (mine == null) {
                        if (theirs != null || !map.containsKey(key)) {
                            return false;
                        }
                    } else if (!mine.equals(theirs)) {
                        return false;
                    }
                }
            } catch (NullPointerException | ClassCastException ignored) {
                return false;
            }
            return true;
        } else if (object instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) object;
            if (size() != map.size()) {
                return false;
            }

            try {
                for (int i = 0; i < mSize; i++) {
                    K key = keyAt(i);
                    V mine = valueAt(i);
                    Object theirs = map.get(key);
                    if (mine == null) {
                        if (theirs != null || !map.containsKey(key)) {
                            return false;
                        }
                    } else if (!mine.equals(theirs)) {
                        return false;
                    }
                }
            } catch (NullPointerException | ClassCastException ignored) {
                return false;
            }
            return true;
        }
        return false;
    }


    @Override
    public int hashCode() {
        final int[] hashes = mHashes;
        final Object[] array = mArray;
        int result = 0;
        for (int i = 0, v = 1, s = mSize; i < s; i++, v += 2) {
            Object value = array[v];
            result += hashes[i] ^ (value == null ? 0 : value.hashCode());
        }
        return result;
    }

    /**
     * {@inheritDoc}
     *
     * <p>This implementation composes a string by iterating over its mappings. If
     * this map contains itself as a key or a value, the string "(this Map)"
     * will appear in its place.
     */
    @Override
    public String toString() {
        if (isEmpty()) {
            return "{}";
        }
        StringBuilder buffer = new StringBuilder(mSize * 28);
        buffer.append('{');
        for (int i = 0; i < mSize; i++) {
            if (i > 0) {
                buffer.append(", ");
            }
            Object key = keyAt(i);
            if (key != this) {
                buffer.append(key);
            } else {
                buffer.append("(this Map)");
            }
            buffer.append('=');
            Object value = valueAt(i);
            if (value != this) {
                buffer.append(value);
            } else {
                buffer.append("(this Map)");
            }
        }
        buffer.append('}');
        return buffer.toString();
    }
}
