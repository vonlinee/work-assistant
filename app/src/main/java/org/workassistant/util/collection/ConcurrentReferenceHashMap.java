package org.workassistant.util.collection;

import org.workassistant.util.Assert;
import org.workassistant.util.util.ObjectUtils;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A {@link ConcurrentHashMap} that uses {@link ReferenceType#SOFT soft} or
 * {@linkplain ReferenceType#WEAK weak} references for both {@code keys} and {@code values}.
 *
 * <p>This class can be used as an alternative to
 * {@code Collections.synchronizedMap(new WeakHashMap<K, Reference<V>>())} in order to
 * support better performance when accessed concurrently. This implementation follows the
 * same design constraints as {@link ConcurrentHashMap} with the exception that
 * {@code null} values and {@code null} keys are supported.
 *
 * <p><b>NOTE:</b> The use of references means that there is no guarantee that items
 * placed into the map will be subsequently available. The garbage collector may discard
 * references at any time, so it may appear that an unknown thread is silently removing
 * entries.
 *
 * <p>If not explicitly specified, this implementation will use
 * {@linkplain SoftReference soft entry references}.
 *
 * @param <K> the key type
 * @param <V> the value type
 * @author Phillip Webb
 * @author Juergen Hoeller
 * @since 17
 */
public class ConcurrentReferenceHashMap<K, V> extends AbstractMap<K, V> implements ConcurrentMap<K, V> {

    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    private static final float DEFAULT_LOAD_FACTOR = 0.75f;

    private static final int DEFAULT_CONCURRENCY_LEVEL = 16;

    private static final ReferenceType DEFAULT_REFERENCE_TYPE = ReferenceType.SOFT;

    private static final int MAXIMUM_CONCURRENCY_LEVEL = 1 << 16;

    private static final int MAXIMUM_SEGMENT_SIZE = 1 << 30;

    /**
     * Array of segments indexed using the high order bits from the hash.
     */
    private final Segment[] segments;

    /**
     * When the average number of references per table exceeds this value resize will be attempted.
     */
    private final float loadFactor;

    /**
     * The reference type: SOFT or WEAK.
     */
    private final ReferenceType referenceType;

    /**
     * The shift value used to calculate the size of the segments array and an index from the hash.
     */
    private final int shift;

    /**
     * Late binding entry set.
     */
    private volatile Set<Map.Entry<K, V>> entrySet;

    /**
     * Create a new {@code ConcurrentReferenceHashMap} instance.
     */
    public ConcurrentReferenceHashMap() {
        this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL, DEFAULT_REFERENCE_TYPE);
    }

    /**
     * Create a new {@code ConcurrentReferenceHashMap} instance.
     *
     * @param initialCapacity the initial capacity of the map
     */
    public ConcurrentReferenceHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL, DEFAULT_REFERENCE_TYPE);
    }

    /**
     * Create a new {@code ConcurrentReferenceHashMap} instance.
     *
     * @param initialCapacity the initial capacity of the map
     * @param loadFactor      the load factor. When the average number of references per table
     *                        exceeds this value resize will be attempted
     */
    public ConcurrentReferenceHashMap(int initialCapacity, float loadFactor) {
        this(initialCapacity, loadFactor, DEFAULT_CONCURRENCY_LEVEL, DEFAULT_REFERENCE_TYPE);
    }

    /**
     * Create a new {@code ConcurrentReferenceHashMap} instance.
     *
     * @param initialCapacity  the initial capacity of the map
     * @param concurrencyLevel the expected number of threads that will concurrently
     *                         write to the map
     */
    public ConcurrentReferenceHashMap(int initialCapacity, int concurrencyLevel) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR, concurrencyLevel, DEFAULT_REFERENCE_TYPE);
    }

    /**
     * Create a new {@code ConcurrentReferenceHashMap} instance.
     *
     * @param initialCapacity the initial capacity of the map
     * @param referenceType   the reference type used for entries (soft or weak)
     */
    public ConcurrentReferenceHashMap(int initialCapacity, ReferenceType referenceType) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL, referenceType);
    }

    /**
     * Create a new {@code ConcurrentReferenceHashMap} instance.
     *
     * @param initialCapacity  the initial capacity of the map
     * @param loadFactor       the load factor. When the average number of references per
     *                         table exceeds this value, resize will be attempted.
     * @param concurrencyLevel the expected number of threads that will concurrently
     *                         write to the map
     */
    public ConcurrentReferenceHashMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
        this(initialCapacity, loadFactor, concurrencyLevel, DEFAULT_REFERENCE_TYPE);
    }

    /**
     * Create a new {@code ConcurrentReferenceHashMap} instance.
     *
     * @param initialCapacity  the initial capacity of the map
     * @param loadFactor       the load factor. When the average number of references per
     *                         table exceeds this value, resize will be attempted.
     * @param concurrencyLevel the expected number of threads that will concurrently
     *                         write to the map
     * @param referenceType    the reference type used for entries (soft or weak)
     */
    @SuppressWarnings("unchecked")
    public ConcurrentReferenceHashMap(
        int initialCapacity, float loadFactor, int concurrencyLevel, ReferenceType referenceType) {
        Assert.isTrue(initialCapacity >= 0, "Initial capacity must not be negative");
        Assert.isTrue(loadFactor > 0f, "Load factor must be positive");
        Assert.isTrue(concurrencyLevel > 0, "Concurrency level must be positive");
        Assert.notNull(referenceType, "Reference type must not be null");
        this.loadFactor = loadFactor;
        this.shift = calculateShift(concurrencyLevel, MAXIMUM_CONCURRENCY_LEVEL);
        int size = 1 << this.shift;
        this.referenceType = referenceType;
        int roundedUpSegmentCapacity = (int) ((initialCapacity + size - 1L) / size);
        int initialSize = 1 << calculateShift(roundedUpSegmentCapacity, MAXIMUM_SEGMENT_SIZE);
        Segment[] segments = (Segment[]) Array.newInstance(Segment.class, size);
        int resizeThreshold = (int) (initialSize * getLoadFactor());
        for (int i = 0; i < segments.length; i++) {
            segments[i] = new Segment(initialSize, resizeThreshold);
        }
        this.segments = segments;
    }

    /**
     * Calculate a shift value that can be used to create a power-of-two value between
     * the specified maximum and minimum values.
     *
     * @param minimumValue the minimum value
     * @param maximumValue the maximum value
     * @return the calculated shift (use {@code 1 << shift} to obtain a value)
     */
    protected static int calculateShift(int minimumValue, int maximumValue) {
        int shift = 0;
        int value = 1;
        while (value < minimumValue && value < maximumValue) {
            value <<= 1;
            shift++;
        }
        return shift;
    }

    protected final float getLoadFactor() {
        return this.loadFactor;
    }

    protected final int getSegmentsSize() {
        return this.segments.length;
    }

    protected final Segment getSegment(int index) {
        return this.segments[index];
    }

    /**
     * Factory method that returns the {@link ReferenceManager}.
     * This method will be called once for each {@link Segment}.
     *
     * @return a new reference manager
     */
    protected ReferenceManager createReferenceManager() {
        return new ReferenceManager();
    }

    /**
     * Get the hash for a given object, apply an additional hash function to reduce
     * collisions. This implementation uses the same Wang/Jenkins algorithm as
     * {@link ConcurrentHashMap}. Subclasses can override to provide alternative hashing.
     *
     * @param o the object to hash (maybe null)
     * @return the resulting hash code
     */
    protected int getHash(Object o) {
        int hash = (o != null ? o.hashCode() : 0);
        hash += (hash << 15) ^ 0xffffcd7d;
        hash ^= (hash >>> 10);
        hash += (hash << 3);
        hash ^= (hash >>> 6);
        hash += (hash << 2) + (hash << 14);
        hash ^= (hash >>> 16);
        return hash;
    }

    @Override
    public V get(Object key) {
        Reference<K, V> ref = getReference(key, Restructure.WHEN_NECESSARY);
        Entry<K, V> entry = (ref != null ? ref.get() : null);
        return (entry != null ? entry.getValue() : null);
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        Reference<K, V> ref = getReference(key, Restructure.WHEN_NECESSARY);
        Entry<K, V> entry = (ref != null ? ref.get() : null);
        return (entry != null ? entry.getValue() : defaultValue);
    }

    @Override
    public boolean containsKey(Object key) {
        Reference<K, V> ref = getReference(key, Restructure.WHEN_NECESSARY);
        Entry<K, V> entry = (ref != null ? ref.get() : null);
        return (entry != null && ObjectUtils.nullSafeEquals(entry.getKey(), key));
    }

    /**
     * Return a {@link Reference} to the {@link Entry} for the specified {@code key},
     * or {@code null} if not found.
     *
     * @param key         the key (can be {@code null})
     * @param restructure types of restructure allowed during this call
     * @return the reference, or {@code null} if not found
     */
    protected final Reference<K, V> getReference(Object key, Restructure restructure) {
        int hash = getHash(key);
        return getSegmentForHash(hash).getReference(key, hash, restructure);
    }

    @Override
    public V put(K key, V value) {
        return put(key, value, true);
    }

    @Override
    public V putIfAbsent( K key, V value) {
        return put(key, value, false);
    }

    private V put(final K key, final V value, final boolean overwriteExisting) {
        return doTask(key, new Task<V>(TaskOption.RESTRUCTURE_BEFORE, TaskOption.RESIZE) {
            @Override
            protected V execute(Reference<K, V> ref, Entry<K, V> entry, Entries<V> entries) {
                if (entry != null) {
                    V oldValue = entry.getValue();
                    if (overwriteExisting) {
                        entry.setValue(value);
                    }
                    return oldValue;
                }
                Assert.state(entries != null, "No entries segment");
                entries.add(value);
                return null;
            }
        });
    }

    @Override
    public V remove(Object key) {
        return doTask(key, new Task<V>(TaskOption.RESTRUCTURE_AFTER, TaskOption.SKIP_IF_EMPTY) {
            @Override
            protected V execute(Reference<K, V> ref, Entry<K, V> entry) {
                if (entry != null) {
                    if (ref != null) {
                        ref.release();
                    }
                    return entry.value;
                }
                return null;
            }
        });
    }

    @Override
    public boolean remove( Object key, final Object value) {
        Boolean result = doTask(key, new Task<Boolean>(TaskOption.RESTRUCTURE_AFTER, TaskOption.SKIP_IF_EMPTY) {
            @Override
            protected Boolean execute(Reference<K, V> ref, Entry<K, V> entry) {
                if (entry != null && ObjectUtils.nullSafeEquals(entry.getValue(), value)) {
                    if (ref != null) {
                        ref.release();
                    }
                    return true;
                }
                return false;
            }
        });
        return (Boolean.TRUE.equals(result));
    }

    @Override
    public boolean replace( K key, final  V oldValue, final  V newValue) {
        Boolean result = doTask(key, new Task<Boolean>(TaskOption.RESTRUCTURE_BEFORE, TaskOption.SKIP_IF_EMPTY) {
            @Override
            protected Boolean execute(Reference<K, V> ref, Entry<K, V> entry) {
                if (entry != null && ObjectUtils.nullSafeEquals(entry.getValue(), oldValue)) {
                    entry.setValue(newValue);
                    return true;
                }
                return false;
            }
        });
        return (Boolean.TRUE.equals(result));
    }

    @Override
    public V replace( K key, final  V value) {
        return doTask(key, new Task<V>(TaskOption.RESTRUCTURE_BEFORE, TaskOption.SKIP_IF_EMPTY) {
            @Override
            protected V execute(Reference<K, V> ref, Entry<K, V> entry) {
                if (entry != null) {
                    V oldValue = entry.getValue();
                    entry.setValue(value);
                    return oldValue;
                }
                return null;
            }
        });
    }

    @Override
    public void clear() {
        for (Segment segment : this.segments) {
            segment.clear();
        }
    }

    /**
     * Remove any entries that have been garbage collected and are no longer referenced.
     * Under normal circumstances garbage collected entries are automatically purged as
     * items are added or removed from the Map. This method can be used to force a purge,
     * and is useful when the Map is read frequently but updated less often.
     */
    public void purgeUnreferencedEntries() {
        for (Segment segment : this.segments) {
            segment.restructureIfNecessary(false);
        }
    }

    @Override
    public int size() {
        int size = 0;
        for (Segment segment : this.segments) {
            size += segment.getCount();
        }
        return size;
    }

    @Override
    public boolean isEmpty() {
        for (Segment segment : this.segments) {
            if (segment.getCount() > 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public  Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> entrySet = this.entrySet;
        if (entrySet == null) {
            entrySet = new EntrySet();
            this.entrySet = entrySet;
        }
        return entrySet;
    }

    private <T> T doTask(Object key, Task<T> task) {
        int hash = getHash(key);
        return getSegmentForHash(hash).doTask(hash, key, task);
    }

    private Segment getSegmentForHash(int hash) {
        return this.segments[(hash >>> (32 - this.shift)) & (this.segments.length - 1)];
    }

    /**
     * Various reference types supported by this map.
     */
    public enum ReferenceType {

        /**
         * Use {@link SoftReference SoftReferences}.
         */
        SOFT,

        /**
         * Use {@link WeakReference WeakReferences}.
         */
        WEAK
    }

    /**
     * Various options supported by a {@code Task}.
     */
    private enum TaskOption {
        RESTRUCTURE_BEFORE,
        RESTRUCTURE_AFTER,
        SKIP_IF_EMPTY,
        RESIZE
    }

    /**
     * The types of restructuring that can be performed.
     */
    protected enum Restructure {
        WHEN_NECESSARY,
        NEVER
    }

    /**
     * A reference to an {@link Entry} contained in the map. Implementations are usually
     * wrappers around specific Java reference implementations (e.g., {@link SoftReference}).
     *
     * @param <K> the key type
     * @param <V> the value type
     */
    protected interface Reference<K, V> {

        /**
         * Return the referenced entry, or {@code null} if the entry is no longer available.
         */
        Entry<K, V> get();

        /**
         * Return the hash for the reference.
         */
        int getHash();

        /**
         * Return the next reference in the chain, or {@code null} if none.
         */
        Reference<K, V> getNext();

        /**
         * Release this entry and ensure that it will be returned from
         * {@code ReferenceManager#pollForPurge()}.
         */
        void release();
    }

    /**
     * Allows a task access to Segment entries.
     */
    private interface Entries<V> {

        /**
         * Add a new entry with the specified value.
         *
         * @param value the value to add
         */
        void add(V value);
    }

    /**
     * A single map entry.
     *
     * @param <K> the key type
     * @param <V> the value type
     */
    protected static final class Entry<K, V> implements Map.Entry<K, V> {

        private final K key;

        private volatile V value;

        public Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return this.key;
        }

        @Override
        public V getValue() {
            return this.value;
        }

        @Override
        public V setValue(V value) {
            V previous = this.value;
            this.value = value;
            return previous;
        }

        @Override
        public String toString() {
            return (this.key + "=" + this.value);
        }

        @Override
        @SuppressWarnings("rawtypes")
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof Map.Entry otherEntry)) {
                return false;
            }
            return (ObjectUtils.nullSafeEquals(getKey(), otherEntry.getKey()) &&
                    ObjectUtils.nullSafeEquals(getValue(), otherEntry.getValue()));
        }

        @Override
        public int hashCode() {
            return (ObjectUtils.nullSafeHashCode(this.key) ^ ObjectUtils.nullSafeHashCode(this.value));
        }
    }

    /**
     * Internal {@link Reference} implementation for {@link SoftReference SoftReferences}.
     */
    private static final class SoftEntryReference<K, V> extends SoftReference<Entry<K, V>> implements Reference<K, V> {

        private final int hash;

        private final Reference<K, V> nextReference;

        public SoftEntryReference(Entry<K, V> entry, int hash, Reference<K, V> next,
                                  ReferenceQueue<Entry<K, V>> queue) {

            super(entry, queue);
            this.hash = hash;
            this.nextReference = next;
        }

        @Override
        public int getHash() {
            return this.hash;
        }

        @Override

        public Reference<K, V> getNext() {
            return this.nextReference;
        }

        @Override
        public void release() {
            enqueue();
            clear();
        }
    }

    /**
     * Internal {@link Reference} implementation for {@link WeakReference WeakReferences}.
     */
    private static final class WeakEntryReference<K, V> extends WeakReference<Entry<K, V>> implements Reference<K, V> {

        private final int hash;

        private final Reference<K, V> nextReference;

        public WeakEntryReference(Entry<K, V> entry, int hash, Reference<K, V> next,
                                  ReferenceQueue<Entry<K, V>> queue) {
            super(entry, queue);
            this.hash = hash;
            this.nextReference = next;
        }

        @Override
        public int getHash() {
            return this.hash;
        }

        @Override

        public Reference<K, V> getNext() {
            return this.nextReference;
        }

        @Override
        public void release() {
            enqueue();
            clear();
        }
    }

    /**
     * A single segment used to divide the map to allow better concurrent performance.
     */
    protected final class Segment extends ReentrantLock {

        private final ReferenceManager referenceManager;

        private final int initialSize;
        /**
         * The total number of references contained in this segment. This includes chained
         * references and references that have been garbage collected but not purged.
         */
        private final AtomicInteger count = new AtomicInteger();
        /**
         * Array of references indexed using the low order bits from the hash.
         * This property should only be set along with {@code resizeThreshold}.
         */
        private volatile Reference<K, V>[] references;
        /**
         * The threshold when resizing of the references should occur. When {@code count}
         * exceeds this value references will be resized.
         */
        private int resizeThreshold;

        public Segment(int initialSize, int resizeThreshold) {
            this.referenceManager = createReferenceManager();
            this.initialSize = initialSize;
            this.references = createReferenceArray(initialSize);
            this.resizeThreshold = resizeThreshold;
        }

        public Reference<K, V> getReference(Object key, int hash, Restructure restructure) {
            if (restructure == Restructure.WHEN_NECESSARY) {
                restructureIfNecessary(false);
            }
            if (this.count.get() == 0) {
                return null;
            }
            // Use a local copy to protect against other threads writing
            Reference<K, V>[] references = this.references;
            int index = getIndex(hash, references);
            Reference<K, V> head = references[index];
            return findInChain(head, key, hash);
        }

        /**
         * Apply an update operation to this segment.
         * The segment will be locked during the update.
         *
         * @param hash the hash of the key
         * @param key  the key
         * @param task the update operation
         * @return the result of the operation
         */
        private <T> T doTask(final int hash, final Object key, final Task<T> task) {
            boolean resize = task.hasOption(TaskOption.RESIZE);
            if (task.hasOption(TaskOption.RESTRUCTURE_BEFORE)) {
                restructureIfNecessary(resize);
            }
            if (task.hasOption(TaskOption.SKIP_IF_EMPTY) && this.count.get() == 0) {
                return task.execute(null, null, null);
            }
            lock();
            try {
                final int index = getIndex(hash, this.references);
                final Reference<K, V> head = this.references[index];
                Reference<K, V> ref = findInChain(head, key, hash);
                Entry<K, V> entry = (ref != null ? ref.get() : null);
                Entries<V> entries = value -> {
                    @SuppressWarnings("unchecked")
                    Entry<K, V> newEntry = new Entry<>((K) key, value);
                    Reference<K, V> newReference = Segment.this.referenceManager.createReference(newEntry, hash, head);
                    Segment.this.references[index] = newReference;
                    Segment.this.count.incrementAndGet();
                };
                return task.execute(ref, entry, entries);
            } finally {
                unlock();
                if (task.hasOption(TaskOption.RESTRUCTURE_AFTER)) {
                    restructureIfNecessary(resize);
                }
            }
        }

        /**
         * Clear all items from this segment.
         */
        public void clear() {
            if (this.count.get() == 0) {
                return;
            }
            lock();
            try {
                this.references = createReferenceArray(this.initialSize);
                this.resizeThreshold = (int) (this.references.length * getLoadFactor());
                this.count.set(0);
            } finally {
                unlock();
            }
        }

        /**
         * Restructure the underlying data structure when it becomes necessary. This
         * method can increase the size of the references table as well as purge any
         * references that have been garbage collected.
         *
         * @param allowResize if resizing is permitted
         */
        private void restructureIfNecessary(boolean allowResize) {
            int currCount = this.count.get();
            boolean needsResize = allowResize && (currCount > 0 && currCount >= this.resizeThreshold);
            Reference<K, V> ref = this.referenceManager.pollForPurge();
            if (ref != null || (needsResize)) {
                restructure(allowResize, ref);
            }
        }

        private void restructure(boolean allowResize, Reference<K, V> ref) {
            boolean needsResize;
            lock();
            try {
                int countAfterRestructure = this.count.get();
                Set<Reference<K, V>> toPurge = Collections.emptySet();
                if (ref != null) {
                    toPurge = new HashSet<>();
                    while (ref != null) {
                        toPurge.add(ref);
                        ref = this.referenceManager.pollForPurge();
                    }
                }
                countAfterRestructure -= toPurge.size();

                // Recalculate taking into account count inside lock and items that
                // will be purged
                needsResize = (countAfterRestructure > 0 && countAfterRestructure >= this.resizeThreshold);
                boolean resizing = false;
                int restructureSize = this.references.length;
                if (allowResize && needsResize && restructureSize < MAXIMUM_SEGMENT_SIZE) {
                    restructureSize <<= 1;
                    resizing = true;
                }

                // Either create a new table or reuse the existing one
                Reference<K, V>[] restructured =
                    (resizing ? createReferenceArray(restructureSize) : this.references);

                // Restructure
                for (int i = 0; i < this.references.length; i++) {
                    ref = this.references[i];
                    if (!resizing) {
                        restructured[i] = null;
                    }
                    while (ref != null) {
                        if (!toPurge.contains(ref)) {
                            Entry<K, V> entry = ref.get();
                            if (entry != null) {
                                int index = getIndex(ref.getHash(), restructured);
                                restructured[index] = this.referenceManager.createReference(
                                    entry, ref.getHash(), restructured[index]);
                            }
                        }
                        ref = ref.getNext();
                    }
                }

                // Replace volatile members
                if (resizing) {
                    this.references = restructured;
                    this.resizeThreshold = (int) (this.references.length * getLoadFactor());
                }
                this.count.set(Math.max(countAfterRestructure, 0));
            } finally {
                unlock();
            }
        }

        private Reference<K, V> findInChain(Reference<K, V> ref, Object key, int hash) {
            Reference<K, V> currRef = ref;
            while (currRef != null) {
                if (currRef.getHash() == hash) {
                    Entry<K, V> entry = currRef.get();
                    if (entry != null) {
                        K entryKey = entry.getKey();
                        if (ObjectUtils.nullSafeEquals(entryKey, key)) {
                            return currRef;
                        }
                    }
                }
                currRef = currRef.getNext();
            }
            return null;
        }

        @SuppressWarnings({"unchecked"})
        private Reference<K, V>[] createReferenceArray(int size) {
            return new Reference[size];
        }

        private int getIndex(int hash, Reference<K, V>[] references) {
            return (hash & (references.length - 1));
        }

        /**
         * Return the size of the current references array.
         */
        public int getSize() {
            return this.references.length;
        }

        /**
         * Return the total number of references in this segment.
         */
        public int getCount() {
            return this.count.get();
        }
    }

    /**
     * A task that can be {@link Segment#doTask run} against a {@link Segment}.
     */
    private abstract class Task<T> {

        private final EnumSet<TaskOption> options;

        public Task(TaskOption... options) {
            this.options = (options.length == 0 ? EnumSet.noneOf(TaskOption.class) : EnumSet.of(options[0], options));
        }

        public boolean hasOption(TaskOption option) {
            return this.options.contains(option);
        }

        /**
         * Execute the task.
         *
         * @param ref     the found reference (or {@code null})
         * @param entry   the found entry (or {@code null})
         * @param entries access to the underlying entries
         * @return the result of the task
         * @see #execute(Reference, Entry)
         */
        protected T execute(Reference<K, V> ref, Entry<K, V> entry, Entries<V> entries) {
            return execute(ref, entry);
        }

        /**
         * Convenience method that can be used for tasks that do not need access to {@link Entries}.
         *
         * @param ref   the found reference (or {@code null})
         * @param entry the found entry (or {@code null})
         * @return the result of the task
         * @see #execute(Reference, Entry, Entries)
         */
        protected T execute(Reference<K, V> ref, Entry<K, V> entry) {
            return null;
        }
    }

    /**
     * Internal entry-set implementation.
     */
    private class EntrySet extends AbstractSet<Map.Entry<K, V>> {

        @Override
        public  Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        @Override
        public boolean contains(Object o) {
            if (o instanceof Map.Entry<?, ?> entry) {
                Reference<K, V> ref = ConcurrentReferenceHashMap.this.getReference(entry.getKey(), Restructure.NEVER);
                Entry<K, V> otherEntry = (ref != null ? ref.get() : null);
                if (otherEntry != null) {
                    return ObjectUtils.nullSafeEquals(entry.getValue(), otherEntry.getValue());
                }
            }
            return false;
        }

        @Override
        public boolean remove(Object o) {
            if (o instanceof Map.Entry<?, ?> entry) {
                return ConcurrentReferenceHashMap.this.remove(entry.getKey(), entry.getValue());
            }
            return false;
        }

        @Override
        public int size() {
            return ConcurrentReferenceHashMap.this.size();
        }

        @Override
        public void clear() {
            ConcurrentReferenceHashMap.this.clear();
        }
    }

    /**
     * Internal entry iterator implementation.
     */
    private class EntryIterator implements Iterator<Map.Entry<K, V>> {

        private int segmentIndex;
        private int referenceIndex;
        private Reference<K, V>[] references;
        private Reference<K, V> reference;
        private Entry<K, V> next;
        private Entry<K, V> last;

        public EntryIterator() {
            moveToNextSegment();
        }

        @Override
        public boolean hasNext() {
            getNextIfNecessary();
            return (this.next != null);
        }

        @Override
        public Entry<K, V> next() {
            getNextIfNecessary();
            if (this.next == null) {
                throw new NoSuchElementException();
            }
            this.last = this.next;
            this.next = null;
            return this.last;
        }

        private void getNextIfNecessary() {
            while (this.next == null) {
                moveToNextReference();
                if (this.reference == null) {
                    return;
                }
                this.next = this.reference.get();
            }
        }

        private void moveToNextReference() {
            if (this.reference != null) {
                this.reference = this.reference.getNext();
            }
            while (this.reference == null && this.references != null) {
                if (this.referenceIndex >= this.references.length) {
                    moveToNextSegment();
                    this.referenceIndex = 0;
                } else {
                    this.reference = this.references[this.referenceIndex];
                    this.referenceIndex++;
                }
            }
        }

        private void moveToNextSegment() {
            this.reference = null;
            this.references = null;
            if (this.segmentIndex < ConcurrentReferenceHashMap.this.segments.length) {
                this.references = ConcurrentReferenceHashMap.this.segments[this.segmentIndex].references;
                this.segmentIndex++;
            }
        }

        @Override
        public void remove() {
            Assert.state(this.last != null, "No element to remove");
            ConcurrentReferenceHashMap.this.remove(this.last.getKey());
            this.last = null;
        }
    }

    /**
     * Strategy class used to manage {@link Reference References}.
     * This class can be overridden if alternative reference types need to be supported.
     */
    protected class ReferenceManager {

        private final ReferenceQueue<Entry<K, V>> queue = new ReferenceQueue<>();

        /**
         * Factory method used to create a new {@link Reference}.
         *
         * @param entry the entry contained in the reference
         * @param hash  the hash
         * @param next  the next reference in the chain, or {@code null} if none
         * @return a new {@link Reference}
         */
        public Reference<K, V> createReference(Entry<K, V> entry, int hash, Reference<K, V> next) {
            if (ConcurrentReferenceHashMap.this.referenceType == ReferenceType.WEAK) {
                return new WeakEntryReference<>(entry, hash, next, this.queue);
            }
            return new SoftEntryReference<>(entry, hash, next, this.queue);
        }

        /**
         * Return any reference that has been garbage collected and can be purged from the
         * underlying structure or {@code null} if no references need purging. This
         * method must be thread safe and ideally should not block when returning
         * {@code null}. References should be returned once and only once.
         *
         * @return a reference to purge or {@code null}
         */
        @SuppressWarnings("unchecked")
        public Reference<K, V> pollForPurge() {
            return (Reference<K, V>) this.queue.poll();
        }
    }
}
