package io.devpl.fxui.editor;

import lombok.Getter;

/**
 * codemirror支持的主题
 */
@Getter
public enum Theme {

    XQ_LIGHT("xq-light");

    private final String name;

    Theme(String name) {
        this.name = name;
    }

    public static Theme valueOfName(String name) {
        for (Theme value : values()) {
            if (value.getName().equalsIgnoreCase(name)) {
                return value;
            }
        }
        return null;
    }
}
