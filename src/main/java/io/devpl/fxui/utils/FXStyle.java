package io.devpl.fxui.utils;

import javafx.scene.paint.Color;

/**
 * 通过字符串的形式构建 javafx css
 * javafx 默认样式文件所在目录 javafx-controls-11-win.jar  -->com.sun.javafx.scene.control.skin.caspian
 */
public class FXStyle {

    private final StringBuilder style;

    private FXStyle() {
        style = new StringBuilder();
    }

    public static FXStyle of() {
        return new FXStyle();
    }

    public FXStyle backgroundColor(Color color) {
        style.append("-fx-background-color: #").append(color.toString().substring(4));
        return this;
    }

    public String build() {
        return style.toString();
    }

    public static String color(Color color) {
        return color.toString();
    }
}
