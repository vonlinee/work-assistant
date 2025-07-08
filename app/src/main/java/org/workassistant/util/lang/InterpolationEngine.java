package org.workassistant.util.lang;

public interface InterpolationEngine {
    String combine(String template, Bindings bindings);
}