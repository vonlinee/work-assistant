package org.workassistant.util.util;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A pool of {@link Constant}s.
 *
 * @param <T> the type of the constant
 */
public abstract class ConstantPool<T extends Constant<T>> {

    /**
     * 保存所有常量实例
     */
    private final ConcurrentMap<String, T> constants = new ConcurrentHashMap<>();

    private final AtomicInteger nextId = new AtomicInteger(1);

    /**
     * Shortcut of {@link #valueOf(String) valueOf(firstNameComponent.getName() +
     * "#" + secondNameComponent)}.
     */
    public T valueOf(Class<?> firstNameComponent, String secondNameComponent) {
        return valueOf(Objects.requireNonNull(firstNameComponent, "firstNameComponent").getName()
                       + '#'
                       + Objects.requireNonNull(secondNameComponent, "secondNameComponent"));
    }

    /**
     * Returns the {@link Constant} which is assigned to the specified {@code name}.
     * If there's no such {@link Constant}, a new one will be created and returned.
     * Once created, the subsequent calls with the same {@code name} will always
     * return the previously created one (i.e. singleton.)
     *
     * @param name the name of the {@link Constant}
     */
    public T valueOf(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Param '" + name + "' must not be empty");
        }
        return getOrCreate(name);
    }

    /**
     * Get existing constant by name or creates new one if not exists. Thread safe
     *
     * @param name the name of the {@link Constant}
     */
    private T getOrCreate(String name) {
        T constant = constants.get(name);
        if (constant == null) {
            final T tempConstant = newConstant(nextId(), name);
            constant = constants.putIfAbsent(name, tempConstant);
            if (constant == null) {
                return tempConstant;
            }
        }
        return constant;
    }

    /**
     * Returns {@code true} if a {@link } exists for the given
     * {@code name}.
     */
    public boolean exists(String name) {
        if (name == null || name.isEmpty()) {
            return false;
        }
        return constants.containsKey(name);
    }

    /**
     * Creates a new {@link Constant} for the given {@code name} or fail with an
     * {@link IllegalArgumentException} if a {@link Constant} for the given
     * {@code name} exists.
     */
    public T newInstance(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("the param " + name + " must not be empty");
        }
        return createOrThrow(name);
    }

    /**
     * Creates constant by name or throws exception. Thread safe
     *
     * @param name the name of the {@link Constant}
     */
    private T createOrThrow(String name) {
        T constant = constants.get(name);
        if (constant == null) {
            final T tempConstant = newConstant(nextId(), name);
            constant = constants.putIfAbsent(name, tempConstant);
            if (constant == null) {
                return tempConstant;
            }
        }

        throw new IllegalArgumentException(String.format("'%s' is already in use", name));
    }

    protected abstract T newConstant(int id, String name);

    public final int nextId() {
        return nextId.getAndIncrement();
    }
}
