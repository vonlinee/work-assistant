package org.example.workassistant.utils.util;

public interface Visitable<T> {

    void accept(Visitor<T> visitor);
}
