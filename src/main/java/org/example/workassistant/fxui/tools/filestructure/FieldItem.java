package org.example.workassistant.fxui.tools.filestructure;

import org.example.workassistant.fxui.tools.IconKey;
import org.example.workassistant.fxui.tools.IconMap;

/**
 * 字段
 */
public class FieldItem extends JavaElementItem {

    private String name;

    public FieldItem() {
        super(IconMap.loadSVG(IconKey.JAVA_FIELD));
    }
}
