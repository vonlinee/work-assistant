package org.example.workassistant.ui.tools.filestructure;

import org.example.workassistant.ui.tools.IconKey;
import org.example.workassistant.ui.tools.IconMap;

/**
 * 字段
 */
public class FieldItem extends JavaElementItem {

    private String name;

    public FieldItem() {
        super(IconMap.loadSVG(IconKey.JAVA_FIELD));
    }
}
