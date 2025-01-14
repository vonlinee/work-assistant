package org.example.workassistant.sdk.util;

import java.lang.reflect.Field;

/**
 * Callback optionally used to filter fields to be operated on by a field callback.
 */
@FunctionalInterface
public interface FieldFilter {

    /**
     * Determine whether the given field matches.
     *
     * @param field the field to check
     */
    boolean matches(Field field);

    /**
     * Create a composite filter based on this filter <em>and</em> the provided filter.
     * <p>If this filter does not match, the next filter will not be applied.
     *
     * @param next the next {@code FieldFilter}
     * @return a composite {@code FieldFilter}
     * @throws IllegalArgumentException if the FieldFilter argument is {@code null}
     * @since 5.3.2
     */
    default FieldFilter and(FieldFilter next) {
        Assert.notNull(next, "Next FieldFilter must not be null");
        return field -> matches(field) && next.matches(field);
    }
}
