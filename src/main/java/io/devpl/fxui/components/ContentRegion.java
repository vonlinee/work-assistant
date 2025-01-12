package io.devpl.fxui.components;

import io.devpl.fxui.utils.FXUtils;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;

/**
 * 内容区域
 */
public class ContentRegion extends Region {

    private final ScrollPane scrollPane;

    public ContentRegion() {
        this.scrollPane = new ScrollPane();
        this.scrollPane.setFitToHeight(true);
        this.scrollPane.setFitToWidth(true);
        this.scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        getChildren().add(scrollPane);
    }

    @Override
    protected void layoutChildren() {
        FXUtils.layoutInRegion(this, scrollPane);
    }

    public final void setContent(Node content) {
        this.scrollPane.setContent(content);
    }
}
