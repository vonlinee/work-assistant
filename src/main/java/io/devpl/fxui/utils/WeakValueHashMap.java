package io.devpl.fxui.utils;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * Note: this class if copied from <a href="https://github.com/sialcasa/mvvmFX">MvvmFX</a>
 * The desired behaviour of an in-memory cache is to keep a weak reference to the cached object,
 * this will allow the garbage collector to remove an object from memory once it isn't needed
 * anymore.
 * <p>
 * A {@link HashMap} doesn't help here since it will keep hard references for key and
 * value objects. A {@link WeakHashMap} doesn't either, because it keeps weak references to the
 * key objects, but we want to track the value objects.
 * <p>
 * This implementation of a Map uses a {@link WeakReference} to the value objects. Once the
 * garbage collector decides it wants to finalize a value object, it will be removed from the
 * map automatically.
 * @param <K> - the type of the key object
 * @param <V> - the type of the value object
 */
public class WeakValueHashMap<K, V> extends AbstractMap<K, V> {

    /**
     * the internal hash map to the weak references of the actual value objects
     */
    private final HashMap<K, WeakValue> references;

    /**
     * the garbage collector's removal queue
     */
    private final ReferenceQueue<V> gcQueue;

    /**
     * Creates a WeakValueHashMap with a desired initial capacity
     * @param capacity - the initial capacity
     */
    public WeakValueHashMap(int capacity) {
        references = new HashMap<>(capacity);
        gcQueue = new ReferenceQueue<>();
    }

    /**
     * Creates a WeakValueHashMap with an initial capacity of 1
     */
    public WeakValueHashMap() {
        this(1);
    }

    /**
     * Creates a WeakValueHashMap and copies the content from an existing map
     * @param map - the map to copy from
     */
    public WeakValueHashMap(Map<? extends K, ? extends V> map) {
        this(map.size());
        putAll(map);
    }

    @Override
    public V put(K key, V value) {
        removeScheduledWeakEntries();
        return getReferenceValue(references.put(key, new WeakValue(key, value, gcQueue)));
    }

    @Override
    public V get(Object key) {
        removeScheduledWeakEntries();
        return getReferenceValue(references.get(key));
    }

    @Override
    public V remove(Object key) {
        return getReferenceValue(references.get(key));
    }

    @Override
    public void clear() {
        references.clear();
    }

    @Override
    public boolean containsKey(Object key) {
        removeScheduledWeakEntries();
        return references.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        removeScheduledWeakEntries();
        for (Entry<K, WeakValue> entry : references.entrySet()) {
            if (value == getReferenceValue(entry.getValue())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<K> keySet() {
        removeScheduledWeakEntries();
        return references.keySet();
    }

    @Override
    public int size() {
        removeScheduledWeakEntries();
        return references.size();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        removeScheduledWeakEntries();

        Set<Entry<K, V>> entries = new LinkedHashSet<>();
        for (Entry<K, WeakValue> entry : references.entrySet()) {
            entries.add(new SimpleEntry<K, V>(entry.getKey(), getReferenceValue(entry.getValue())));
        }
        return entries;
    }

    public Collection<V> values() {
        removeScheduledWeakEntries();

        Collection<V> values = new ArrayList<V>();
        for (WeakValue valueRef : references.values()) {
            values.add(getReferenceValue(valueRef));
        }
        return values;
    }

    private V getReferenceValue(WeakValue valueRef) {
        return valueRef == null ? null : valueRef.get();
    }

    /**
     * remove entries once their value is scheduled for removal by the garbage collector
     */
    @SuppressWarnings("unchecked")
    void removeScheduledWeakEntries() {
        WeakValue valueRef;
        while ((valueRef = (WeakValue) gcQueue.poll()) != null) {
            references.remove(valueRef.getKey());
        }
    }

    /**
     * for faster removal in {@link #removeScheduledWeakEntries()} we need to keep track of the key for a value
     */
    class WeakValue extends WeakReference<V> {

        private final K key;

        WeakValue(K key, V value, ReferenceQueue<V> queue) {
            super(value, queue);
            this.key = key;
        }

        public K getKey() {
            return key;
        }
    }
}