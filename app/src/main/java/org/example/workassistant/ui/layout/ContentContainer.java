package org.example.workassistant.ui.layout;

import io.fxtras.scene.ContentRegion;
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
