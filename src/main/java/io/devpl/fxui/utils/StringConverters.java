package io.devpl.fxui.utils;

import javafx.util.StringConverter;

import java.util.function.Function;

public class StringConverters {

    public static <T> StringConverter<T> forType(Class<T> type, Function<T, String> to, Function<String, T> from) {
        return new StringConverter<T>() {
            @Override
            public String toString(T object) {
                if (object == null) {
                    return null;
                }
                try {
                    return to.apply(object);
                } catch (Exception exception) {
                    // ignore
                }
                return null;
            }

            @Override
            public T fromString(String string) {
                if (string == null || string.isEmpty()) {
                    return null;
                }
                try {
                    return from.apply(string);
                } catch (Exception exception) {
                    // ignore
                }
                return null;
            }
        };
    }
}
