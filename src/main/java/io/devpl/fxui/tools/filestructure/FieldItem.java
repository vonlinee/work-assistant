package io.devpl.fxui.tools.filestructure;

import io.devpl.fxui.tools.IconKey;
import io.devpl.fxui.tools.IconMap;

/**
 * 字段
 */
public class FieldItem extends JavaElementItem {

    private String name;

    public FieldItem() {
        super(IconMap.loadSVG(IconKey.JAVA_FIELD));
    }
}
