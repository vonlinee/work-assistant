package org.example.workassistant.sdk.util;

/**
 * Interface representing a builder. Builders are objects that are used to
 * construct other objects.
 *
 * @param <T> the object instance to build
 */
@FunctionalInterface
public interface Builder<T> {
    /**
     * Builds and returns the object.
     *
     * @return T the object instance to build, mostly do not return null
     */
    T build();
}

