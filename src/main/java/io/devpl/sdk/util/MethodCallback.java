package io.devpl.sdk.util;

import java.lang.reflect.Method;

/**
 * Action to take on each method.
 */
@FunctionalInterface
public interface MethodCallback {

    /**
     * Perform an operation using the given method.
     *
     * @param method the method to operate on
     */
    void doWith(Method method) throws IllegalArgumentException, IllegalAccessException;
}
