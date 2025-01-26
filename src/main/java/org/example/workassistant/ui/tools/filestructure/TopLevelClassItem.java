package org.example.workassistant.ui.tools.filestructure;

import org.example.workassistant.ui.tools.IconKey;
import org.example.workassistant.ui.tools.IconMap;

/**
 * 外部类
 */
public class TopLevelClassItem extends JavaElementItem {

    public TopLevelClassItem() {
        super(IconMap.loadSVG(IconKey.JAVA_TOP_CLASS));
    }

    public void addMethod(MethodItem methodItem) {
        getChildren().add(methodItem);
    }

    public void addField(FieldItem fieldItem) {
        getChildren().add(fieldItem);
    }
}
