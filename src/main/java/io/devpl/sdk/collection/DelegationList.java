package io.devpl.sdk.collection;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

/**
 * the delegated list
 *
 * @param <E> the type of element
 */
public interface DelegationList<E> {

    /**
     * return the list to be delegated
     *
     * @return not null
     */
    List<E> delegator();

    default int size() {
        return delegator().size();
    }

    default boolean isEmpty() {
        return delegator().isEmpty();
    }

    default boolean contains(Object o) {
        return delegator().contains(o);
    }

    default Iterator<E> iterator() {
        return delegator().iterator();
    }

    default Object[] toArray() {
        return delegator().toArray();
    }

    default <T> T[] toArray(T[] a) {
        return delegator().toArray(a);
    }

    default boolean add(E e) {
        return delegator().add(e);
    }

    default boolean remove(Object o) {
        return delegator().remove(o);
    }

    default boolean containsAll(Collection<?> c) {
        return delegator().containsAll(c);
    }

    default boolean addAll(Collection<? extends E> c) {
        return delegator().addAll(c);
    }

    default boolean addAll(int index, Collection<? extends E> c) {
        return delegator().addAll(index, c);
    }

    default <T> boolean removeAll(Collection<T> c) {
        return delegator().removeAll(c);
    }

    default boolean retainAll(Collection<?> c) {
        return delegator().retainAll(c);
    }

    default void replaceAll(UnaryOperator<E> operator) {
        delegator().replaceAll(operator);
    }

    default void sort(Comparator<? super E> c) {
        delegator().sort(c);
    }

    default void clear() {
        delegator().clear();
    }

    default E get(int index) {
        return delegator().get(index);
    }

    default E set(int index, E element) {
        return delegator().set(index, element);
    }

    default void add(int index, E element) {
        delegator().add(index, element);
    }

    default E remove(int index) {
        return delegator().remove(index);
    }

    default int indexOf(Object o) {
        return delegator().indexOf(o);
    }

    default int lastIndexOf(Object o) {
        return delegator().lastIndexOf(o);
    }

    default ListIterator<E> listIterator() {
        return delegator().listIterator();
    }

    default ListIterator<E> listIterator(int index) {
        return delegator().listIterator(index);
    }

    default List<E> subList(int fromIndex, int toIndex) {
        return delegator().subList(fromIndex, toIndex);
    }

    default Spliterator<E> spliterator() {
        return delegator().spliterator();
    }

    default <T> T[] toArray(IntFunction<T[]> generator) {
        return delegator().toArray(generator);
    }

    default boolean removeIf(Predicate<? super E> filter) {
        return delegator().removeIf(filter);
    }

    default Stream<E> stream() {
        return delegator().stream();
    }

    default Stream<E> parallelStream() {
        return delegator().parallelStream();
    }

    default void forEach(Consumer<? super E> action) {
        delegator().forEach(action);
    }
}
