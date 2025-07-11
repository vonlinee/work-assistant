package org.workassistant.util.collection;

import org.workassistant.util.util.ArrayUtils;

import java.lang.reflect.Array;
import java.util.*;

/**
 * The thread safe set implementation, which uses array to hold its elements. This set could be used, for cases, which
 * require minimum set modifications.
 *
 * @author Alexey Stashok
 */
public class SynchronizedArraySet<T> implements Set<T> {

    private final Object sync = new Object();
    private final Class<T> clazz;
    private final boolean replaceElementIfEquals;
    private volatile T[] array;

    public SynchronizedArraySet(final Class<T> clazz) {
        this(clazz, true);
    }

    public SynchronizedArraySet(final Class<T> clazz, final boolean replaceElementIfEquals) {
        this.clazz = clazz;
        this.replaceElementIfEquals = replaceElementIfEquals;
    }

    /**
     * Add the element(s) to the set.
     *
     * @param elements the elements to add.
     * @return <tt>true</tt>, if at least one element was added to the set and, as result, the size of the set was
     * increased, or <tt>false</tt>, all element(s) was/were present in the set and, as the result, the set values were just
     * reset.
     */
    @SuppressWarnings("unchecked")
    public final boolean addAll(final T... elements) {
        if (elements == null || elements.length == 0) {
            return false;
        }
        synchronized (sync) {
            int startIdx = 0;
            if (array == null) {
                array = (T[]) Array.newInstance(clazz, 1);
                array[0] = elements[0];
                startIdx = 1;
            }

            boolean result = false;

            for (int i = startIdx; i < elements.length; i++) {
                final T element = elements[i];

                final T[] oldArray = array;
                array = ArrayUtils.addUnique(array, element, replaceElementIfEquals);
                result |= oldArray != array;
            }
            return result;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean addAll(Collection<? extends T> collection) {
        if (collection.isEmpty()) {
            return false;
        }

        synchronized (sync) {
            boolean initArray = array == null;
            if (initArray) {
                array = (T[]) Array.newInstance(clazz, 1);
            }
            boolean result = false;

            for (T element : collection) {
                if (initArray) {
                    initArray = false;
                    array[0] = element;
                    continue;
                }
                final T[] oldArray = array;
                array = ArrayUtils.addUnique(array, element, replaceElementIfEquals);

                result |= oldArray != array;
            }

            return result;
        }
    }

    /**
     * Add all the elements from the source <tt>ArraySet</tt>.
     *
     * @param source the elements to add.
     * @return <tt>true</tt>, if at least one element was added to the set and, as result, the size of the set was
     * increased, or <tt>false</tt>, all element(s) was/were present in the set and, as the result, the set values were just
     * reset.
     */
    public final boolean add(SynchronizedArraySet<T> source) {
        final T[] sourceArraySet = source.getArray();

        if (sourceArraySet == null) {
            return false;
        }

        synchronized (sync) {
            if (array == null) {
                array = Arrays.copyOf(sourceArraySet, sourceArraySet.length);
                return true;
            }

            boolean result = false;

            for (int i = 0; i < sourceArraySet.length; i++) {
                final T element = sourceArraySet[i];

                final T[] oldArray = array;
                array = ArrayUtils.addUnique(array, element, replaceElementIfEquals);

                result |= oldArray != array;
            }

            return result;
        }
    }

    /**
     * Remove element(s) from the set.
     *
     * @param elements the element(s) to remove.
     * @return <tt>true</tt>, if at least one element was found and removed, or <tt>false</tt> otherwise.
     */
    public final boolean removeAll(Object... elements) {
        if (elements.length == 0) {
            return false;
        }

        synchronized (sync) {
            if (array == null) {
                return false;
            }

            boolean result = false;
            for (Object element : elements) {
                final T[] oldArray = array;

                array = ArrayUtils.remove(array, element);

                result |= oldArray != array;
            }

            return result;
        }
    }

    /**
     * Get the underlying array. Please note, it's not appropriate to modify the returned array's content. Please use
     * {@link #add(Object)} and {@link #remove(Object)} instead.
     *
     * @return the array.
     */
    public final T[] getArray() {
        return array;
    }

    /**
     * Get the copy of the underlying array. If the underlying array is <tt>null</tt> - then <tt>null</tt> will be returned.
     *
     * @return the copy of the underlying array. If the underlying array is <tt>null</tt> - then <tt>null</tt> will be
     * returned.
     */
    public final T[] getArrayCopy() {
        final T[] localArray = array;
        if (localArray == null) {
            return null;
        }

        return Arrays.copyOf(localArray, localArray.length);
    }

    /**
     * Get the copy of the underlying array. If the underlying array is <tt>null</tt> - then empty array will be returned.
     *
     * @return the copy of the underlying array. If the underlying array is <tt>null</tt> - then empty array will be
     * returned.
     */
    @SuppressWarnings("unchecked")
    public final T[] obtainArrayCopy() {
        final T[] localArray = array;
        if (localArray == null) {
            return (T[]) Array.newInstance(clazz, 0);
        }

        return Arrays.copyOf(localArray, localArray.length);
    }

    /**
     * Remove all the set elements.
     */
    @Override
    public void clear() {
        array = null;
    }

    // ===================== java.util.Set =================================

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        final T[] localArray = array;
        return localArray != null ? localArray.length : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean add(final T element) {

        if (element == null) {
            return false;
        }

        synchronized (sync) {
            if (array == null) {
                array = (T[]) Array.newInstance(clazz, 1);
                array[0] = element;
                return true;
            }

            final T[] oldArray = array;
            array = ArrayUtils.addUnique(array, element, replaceElementIfEquals);

            return oldArray != array;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Object o) {
        final Object[] localArray = array;

        if (localArray == null) {
            return false;
        }

        for (int i = 0; i < localArray.length; i++) {
            if (localArray[i].equals(o)) {
                return true;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] toArray() {
        final Object[] localArray = array;

        return Arrays.copyOf(localArray, localArray.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <K> K[] toArray(K[] a) {
        final Object[] localArray = array;

        if (localArray == null) {
            return a;
        }

        final int size = localArray.length;

        if (a.length < size) { // Make a new array of a's runtime type, but my contents:
            return (K[]) Arrays.copyOf(localArray, size, a.getClass());
        }
        System.arraycopy(localArray, 0, a, 0, localArray.length);
        if (a.length > size) {
            a[size] = null;
        }
        return a;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(Object o) {
        return removeAll(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsAll(Collection<?> collection) {
        if (collection.isEmpty()) {
            return true;
        }

        final Object[] localArray = array;

        for (Object element : collection) {
            if (ArrayUtils.indexOf(localArray, element) == -1) {
                return false;
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean retainAll(final Collection<?> collection) {
        final T[] localArray = array;
        if (localArray == null) {
            return false;
        }

        final T[] newArray = (T[]) Array.newInstance(clazz, Math.min(localArray.length, collection.size()));
        int newSize = 0;
        for (int i = 0; i < localArray.length; i++) {
            final T elem = localArray[i];
            if (collection.contains(elem)) {
                newArray[newSize++] = elem;
            }
        }

        if (newSize == localArray.length) {
            return false;
        }

        array = Arrays.copyOf(newArray, newSize);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean removeAll(Collection<?> collection) {
        final T[] localArray = array;
        if (localArray == null) {
            return false;
        }

        final T[] newArray = (T[]) Array.newInstance(clazz, localArray.length);
        int newSize = 0;
        for (int i = 0; i < localArray.length; i++) {
            final T elem = localArray[i];
            if (!collection.contains(elem)) {
                newArray[newSize++] = elem;
            }
        }

        if (newSize == localArray.length) {
            return false;
        }

        array = Arrays.copyOf(newArray, newSize);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<T> iterator() {
        return new Itr();
    }

    /**
     * Iterator
     */
    private class Itr implements Iterator<T> {

        int cursor; // index of next element to return
        T lastRet; // last returned element
        T nextElem;

        public Itr() {
            advance();
        }

        @Override
        public boolean hasNext() {
            return nextElem != null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T next() {
            if (nextElem == null) {
                throw new NoSuchElementException();
            }

            lastRet = nextElem;
            advance();

            return lastRet;
        }

        @Override
        public void remove() {
            if (lastRet == null) {
                throw new IllegalStateException();
            }

            SynchronizedArraySet.this.remove(lastRet);
            cursor--;
            lastRet = null;
        }

        @SuppressWarnings("unchecked")
        private void advance() {
            final Object[] localArray = array;
            if (localArray == null || cursor >= localArray.length) {
                nextElem = null;

                return;
            }

            nextElem = (T) localArray[cursor++];
        }
    }
}
