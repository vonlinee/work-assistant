package org.workassistant.util.util;

/**
 * An attribute which allows to store a value reference. It may be updated atomically and so is thread-safe.
 *
 * @param <T> the type of the value it holds.
 */
public interface Attribute<T> {

    /**
     * Returns the key of this attribute.
     */
    AttributeKey<T> key();

    /**
     * Returns the current value, which may be {@code null}
     */
    T get();

    /**
     * Sets the value
     */
    void set(T value);

    /**
     * Atomically sets to the given value and returns the old value which may be {@code null} if non was set before.
     */
    T getAndSet(T value);

    /**
     * Atomically sets to the given value if this {@link Attribute}'s value is {@code null}.
     * If it was not possible to set the value as it contains a value it will just return the current value.
     */
    T setIfAbsent(T value);

    /**
     * Removes this attribute from the {@link AttributeMap} and returns the old value. Subsequent {@link #get()}
     * calls will return {@code null}.
     * <p>
     * If you only want to return the old value and clear the {@link Attribute} while still keep it in the
     * {@link AttributeMap} use {@link #getAndSet(Object)} with a value of {@code null}.
     *
     * <p>
     * Be aware that even if you call this method another thread that has obtained a reference to this {@link Attribute}
     * via {@link AttributeMap#attr(AttributeKey)} will still operate on the same instance. That said if now another
     * thread or even the same thread later will call {@link AttributeMap#attr(AttributeKey)} again, a new
     * {@link Attribute} instance is created and so is not the same as the previous one that was removed. Because of
     * this special caution should be taken when you call {@link #remove()} or {@link #getAndRemove()}.
     *
     * @deprecated please consider using {@link #getAndSet(Object)} (with value of {@code null}).
     */
    @Deprecated
    T getAndRemove();

    /**
     * Atomically sets the value to the given updated value if the current value == the expected value.
     * If it is the set was successful it returns {@code true} otherwise {@code false}.
     */
    boolean compareAndSet(T oldValue, T newValue);

    /**
     * Removes this attribute from the {@link AttributeMap}. Subsequent {@link #get()} calls will return @{code null}.
     * <p>
     * If you only want to remove the value and clear the {@link Attribute} while still keep it in
     * {@link AttributeMap} use {@link #set(Object)} with a value of {@code null}.
     *
     * <p>
     * Be aware that even if you call this method another thread that has obtained a reference to this {@link Attribute}
     * via {@link AttributeMap#attr(AttributeKey)} will still operate on the same instance. That said if now another
     * thread or even the same thread later will call {@link AttributeMap#attr(AttributeKey)} again, a new
     * {@link Attribute} instance is created and so is not the same as the previous one that was removed. Because of
     * this special caution should be taken when you call {@link #remove()} or {@link #getAndRemove()}.
     *
     * @deprecated please consider using {@link #set(Object)} (with value of {@code null}).
     */
    @Deprecated
    void remove();
}
