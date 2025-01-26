package org.example.workassistant.ui.tools.filestructure;

import org.example.workassistant.ui.tools.IconKey;
import org.example.workassistant.ui.tools.IconMap;
import org.girod.javafx.svgimage.SVGImage;

/**
 * Java 可见性枚举
 */
public enum JavaVisibility {

    PUBLIC(IconKey.JAVA_PUBLIC),
    PRIVATE(IconKey.JAVA_PRIVATE),
    PROTECTED(IconKey.JAVA_PROTECTED),
    PACKAGE_VISIBLE(IconKey.JAVA_PLOCAL);

    /**
     * 对应展示的图标
     */
    private final String iconUrl;

    JavaVisibility(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public SVGImage getIconNode() {
        return IconMap.loadSVG(this.iconUrl);
    }
}
