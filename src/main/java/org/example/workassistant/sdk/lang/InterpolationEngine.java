package org.example.workassistant.sdk.lang;

public interface InterpolationEngine {
    String combine(String template, Bindings bindings);
}