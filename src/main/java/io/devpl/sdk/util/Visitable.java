package io.devpl.sdk.util;

public interface Visitable<T> {

    void accept(Visitor<T> visitor);
}
