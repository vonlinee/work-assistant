package io.devpl.sdk.lang;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 这两种方式都可以使用正则表达式实现。基本的正则表达式插值代码
 */
public class PatternInterpolationEngine implements InterpolationEngine {

    private final Pattern pattern;

    public PatternInterpolationEngine(Pattern pattern) {
        this.pattern = pattern;
    }

    @Override
    public String combine(String template, Bindings bindings) {
        StringBuilder buffer = new StringBuilder(template.length());
        Matcher matcher = pattern.matcher(template);
        while (matcher.find()) {
            String name = matcher.group(1);
            Object value = bindings.get(name);
            matcher.appendReplacement(buffer, String.valueOf(value));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
}