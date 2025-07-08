package org.workassistant.ui.tools.filestructure;

import org.workassistant.ui.tools.IconKey;
import org.workassistant.ui.tools.IconMap;

public class MethodItem extends JavaElementItem {

    public MethodItem() {
        super(IconMap.loadSVG(IconKey.JAVA_METHOD));
    }
}
