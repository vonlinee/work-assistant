package org.workassistant.util.util;

public interface Visitable<T> {

    void accept(Visitor<T> visitor);
}
