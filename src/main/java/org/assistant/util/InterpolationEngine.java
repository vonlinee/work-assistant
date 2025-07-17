package org.assistant.util;

public interface InterpolationEngine {
    String combine(String template, Bindings bindings);
}