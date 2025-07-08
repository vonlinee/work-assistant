package org.workassistant.ui.tools.filestructure;

import org.workassistant.ui.tools.IconKey;
import org.workassistant.ui.tools.IconMap;

/**
 * 字段
 */
public class FieldItem extends JavaElementItem {

    private String name;

    public FieldItem() {
        super(IconMap.loadSVG(IconKey.JAVA_FIELD));
    }
}
