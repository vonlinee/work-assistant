package io.devpl.sdk.collection;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Set集合工具类
 */
public abstract class Sets {

    @SafeVarargs
    public static <E> Set<E> of(Collection<E> collection, E... elements) {
        if (collection == null) {
            return new HashSet<>(Arrays.asList(elements));
        }
        collection.addAll(Arrays.asList(elements));
        return new HashSet<>(collection);
    }

    @SafeVarargs
    public static <E> Set<E> newSet(Collection<E> collection, E... elements) {
        if (collection == null) {
            return new HashSet<>(Arrays.asList(elements));
        }
        HashSet<E> set = new HashSet<>(collection);
        set.addAll(Arrays.asList(elements));
        return set;
    }
}
