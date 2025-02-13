package org.example.workassistant.utils.lang;

public interface InterpolationEngine {
    String combine(String template, Bindings bindings);
}