package io.devpl.fxui.layout;

import io.devpl.fxui.components.ContentRegion;
import javafx.scene.Node;
import javafx.scene.layout.Region;

/**
 * 内容区域
 */
public class ContentContainer extends ContentRegion {

    public final void switchTo(Node node) {
        if (node instanceof Region region) {
            region.setPrefWidth(getPrefWidth());
            region.setPrefHeight(getPrefHeight());
        }
        setContent(node);
    }
}
