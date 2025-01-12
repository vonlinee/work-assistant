package io.devpl.fxui.utils;

import io.devpl.sdk.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 单个对象字段校验
 *
 * @param <T>
 */
public class Validator<T> {

    private final T bean;

    private final List<String> errorMessages;

    Validator(T bean) {
        this.bean = bean;
        this.errorMessages = new ArrayList<>();
    }

    public static <T> Validator<T> target(T target) {
        return new Validator<>(target);
    }

    public Validator<T> assertTrue(boolean expression, String message) {
        if (!expression) {
            errorMessages.add(message);
        }
        return this;
    }

    public <V> Validator<T> assertTrue(Function<T, V> column, Predicate<V> condition, String message) {
        if (!condition.test(column.apply(bean))) {
            errorMessages.add(message);
        }
        return this;
    }

    public Validator<T> hasText(Function<T, String> column, String message) {
        if (!StringUtils.hasText(column.apply(bean))) {
            errorMessages.add(message);
        }
        return this;
    }

    public String getErrorMessages() {
        return String.join(";\n", errorMessages);
    }
}
