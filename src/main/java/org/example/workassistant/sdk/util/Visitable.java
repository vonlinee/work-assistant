package org.example.workassistant.sdk.util;

public interface Visitable<T> {

    void accept(Visitor<T> visitor);
}
