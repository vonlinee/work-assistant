package io.devpl.sdk.lang;

public interface InterpolationEngine {
    String combine(String template, Bindings bindings);
}