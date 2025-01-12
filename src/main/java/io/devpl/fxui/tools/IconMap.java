package io.devpl.fxui.tools;

import javafx.scene.Node;
import org.girod.javafx.svgimage.SVGImage;
import org.girod.javafx.svgimage.SVGLoader;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign2.MaterialDesignW;

import java.net.URL;

/**
 * 图标映射关系：决定哪些节点展示哪种图标
 * Ikonli提供的图标
 * 本地SVG图标
 */
public final class IconMap {

    public static SVGImage loadSVG(String key) {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(key);
        return SVGLoader.load(resource);
    }

    public static FontIcon fontIcon(Ikon ikon) {
        return FontIcon.of(ikon);
    }

    public static Node windowClose() {
        return new FontIcon(MaterialDesignW.WINDOW_CLOSE);
    }
}
