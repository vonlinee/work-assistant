package org.workassistant.util.lang;

import java.util.Map;

/**
 * 插值字符串
 * JDK21语言内置字符串插值已实现
 */
public class Interpolations {
    private static final InterpolationEngine INDEXED_ENGINE = new IndexedInterpolationEngine();
    private static final InterpolationEngine NAMED_ENGINE = new NamedInterpolationEngine();

    public static String indexed(String template, Object... bindings) {
        return INDEXED_ENGINE.combine(template, IndexedInterpolationEngine.createBindings(bindings));
    }

    public static String named(String template, Object... bindings) {
        return NAMED_ENGINE.combine(template, NamedInterpolationEngine.createBindings(bindings));
    }

    public static String named(String template, Map<String, ?> bindings) {
        return named(template, bindings, null);
    }

    public static String named(String template, Map<String, ?> bindings, Object defaultValue) {
        return NAMED_ENGINE.combine(template, NamedInterpolationEngine.createBindings(bindings, defaultValue));
    }
}
