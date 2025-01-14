package org.example.workassistant.fxui.tools.filestructure;

import org.example.workassistant.fxui.tools.IconKey;
import org.example.workassistant.fxui.tools.IconMap;

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
