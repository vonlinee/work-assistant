package io.devpl.fxui.controls;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.Region;

public class DisclosureNode extends Region {

    private final Node node = new Button("+");
    private boolean showing = true;

    public DisclosureNode() {
        getChildren().add(node);
    }

    @Override
    protected void layoutChildren() {
        if (showing) {
            layoutInArea(node, 0, 0, getWidth(), getHeight(), 0, HPos.CENTER, VPos.CENTER);
        }
    }

    public final void setShowing(boolean showing) {
        this.showing = showing;
    }
}
