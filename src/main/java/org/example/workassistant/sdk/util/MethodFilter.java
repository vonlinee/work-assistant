package org.example.workassistant.sdk.util;

import java.lang.reflect.Method;

/**
 * Callback optionally used to filter methods to be operated on by a method callback.
 */
@FunctionalInterface
public interface MethodFilter {

    /**
     * Determine whether the given method matches.
     *
     * @param method the method to check
     */
    boolean matches(Method method);

    /**
     * Create a composite filter based on this filter <em>and</em> the provided filter.
     * <p>If this filter does not match, the next filter will not be applied.
     *
     * @param next the next {@code MethodFilter}
     * @return a composite {@code MethodFilter}
     * @throws IllegalArgumentException if the MethodFilter argument is {@code null}
     * @since 5.3.2
     */
    default MethodFilter and(MethodFilter next) {
        Assert.notNull(next, "Next MethodFilter must not be null");
        return method -> matches(method) && next.matches(method);
    }
}
