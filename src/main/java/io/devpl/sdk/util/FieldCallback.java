package io.devpl.sdk.util;

import java.lang.reflect.Field;

/**
 * Callback interface invoked on each field in the hierarchy.
 */
@FunctionalInterface
public interface FieldCallback {

    /**
     * Perform an operation using the given field.
     *
     * @param field the field to operate on
     */
    void doWith(Field field) throws IllegalArgumentException, IllegalAccessException;
}
