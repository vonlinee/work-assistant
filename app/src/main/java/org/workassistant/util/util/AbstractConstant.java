package org.workassistant.util.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Base implementation of {@link Constant}.
 * 常量池的基本实现
 */
public abstract class AbstractConstant<T extends AbstractConstant<T>> implements Constant<T> {

    private static final AtomicLong uniqueIdGenerator = new AtomicLong();
    private final int id;
    private final String name;
    private final long uniquifier;

    /**
     * Creates a new instance.
     */
    protected AbstractConstant(int id, String name) {
        this.id = id;
        this.name = name;
        this.uniquifier = uniqueIdGenerator.getAndIncrement();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public int id() {
        return id;
    }

    @Override
    public final String toString() {
        return name();
    }

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    public final boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public final int compareTo(T o) {
        if (this == o) {
            return 0;
        }
        AbstractConstant<T> other = o;
        int returnCode;
        returnCode = hashCode() - other.hashCode();
        if (returnCode != 0) {
            return returnCode;
        }
        if (uniquifier < other.uniquifier) {
            return -1;
        }
        if (uniquifier > other.uniquifier) {
            return 1;
        }
        throw new Error("failed to compare two different constants");
    }
}
