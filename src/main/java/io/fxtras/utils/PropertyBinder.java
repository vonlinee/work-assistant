package io.fxtras.utils;

import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.util.function.BiConsumer;

public class PropertyBinder<B, T> implements ChangeListener<T> {

    private final B bean;
    private final BiConsumer<B, T> setter;

    /**
     * @param bean   对象
     * @param setter setter对应的lambda表达式
     */
    public PropertyBinder(B bean, BiConsumer<B, T> setter) {
        this.bean = bean;
        this.setter = setter;
    }

    @Override
    public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
        setter.accept(bean, newValue);
    }

    public static <T, B> void bind(Property<T> property, B bean, BiConsumer<B, T> setter) {
        property.addListener(new PropertyBinder<>(bean, setter));
    }
}
