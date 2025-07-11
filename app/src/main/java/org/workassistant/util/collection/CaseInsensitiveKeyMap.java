package org.workassistant.util.collection;

import java.util.*;

/**
 * A Map implementation that uses case-insensitive (using {@link
 * Locale#ENGLISH}) strings as keys.
 * <p>
 * Keys must be instances of {@link String}. Note that this means that
 * <code>null</code> keys are not permitted.
 * <p>
 * This implementation is not thread-safe.
 *
 * @param <V> Type of values placed in this Map.
 */
public class CaseInsensitiveKeyMap<V> extends AbstractMap<String, V> {

    private final Map<Key, V> map = new HashMap<>();

    @Override
    public V get(Object key) {
        return map.get(Key.getInstance(key));
    }

    @Override
    public V put(String key, V value) {
        Key caseInsensitiveKey = Objects.requireNonNull(Key.getInstance(key), "Null keys are not permitted");
        return map.put(caseInsensitiveKey, value);
    }

    /**
     * <b>Use this method with caution</b>. If the input Map contains duplicate
     * keys when the keys are compared in a case-insensitive manner then some
     * values will be lost when inserting via this method.
     */
    @Override
    public void putAll(Map<? extends String, ? extends V> m) {
        super.putAll(m);
    }

    @Override
    public boolean containsKey(Object key) {
        return map.containsKey(Key.getInstance(key));
    }

    @Override
    public V remove(Object key) {
        return map.remove(Key.getInstance(key));
    }

    @Override
    public  Set<Entry<String, V>> entrySet() {
        return new EntrySet<>(map.entrySet());
    }

    private static class EntrySet<V> extends AbstractSet<Entry<String, V>> {

        private final Set<Entry<Key, V>> entrySet;

        EntrySet(Set<Entry<Key, V>> entrySet) {
            this.entrySet = entrySet;
        }

        @Override
        public  Iterator<Entry<String, V>> iterator() {
            return new EntryIterator<>(entrySet.iterator());
        }

        @Override
        public int size() {
            return entrySet.size();
        }
    }

    private record EntryIterator<V>(Iterator<Entry<Key, V>> iterator) implements Iterator<Entry<String, V>> {

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public Entry<String, V> next() {
            Entry<Key, V> entry = iterator.next();
            return new EntryImpl<>(entry.getKey().getKey(), entry.getValue());
        }

        @Override
        public void remove() {
            iterator.remove();
        }
    }

    private record EntryImpl<V>(String key, V value) implements Entry<String, V> {

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }
    }

    private static class Key {

        private final String key;
        private final String lcKey;

        private Key(String key) {
            this.key = key;
            this.lcKey = key.toLowerCase(Locale.ENGLISH);
        }

        public static Key getInstance(Object o) {
            if (o instanceof String) {
                return new Key((String) o);
            }
            return null;
        }

        public String getKey() {
            return key;
        }

        @Override
        public int hashCode() {
            return lcKey.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Key other = (Key) obj;
            return lcKey.equals(other.lcKey);
        }
    }
}
