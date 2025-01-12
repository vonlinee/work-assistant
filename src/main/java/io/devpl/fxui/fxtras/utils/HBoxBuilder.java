package io.devpl.fxui.fxtras.utils;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

public class HBoxBuilder implements NodeBuilder<HBox> {

    private final HBox hBox;

    private HBoxBuilder() {
        this(new HBox());
    }

    private HBoxBuilder(HBox hBox) {
        this.hBox = hBox;
    }

    public static HBoxBuilder builder() {
        return new HBoxBuilder();
    }

    public static HBoxBuilder builder(HBox hBox) {
        return new HBoxBuilder(hBox);
    }

    public HBoxBuilder spacing(double spacing) {
        hBox.setSpacing(spacing);
        return this;
    }

    public HBoxBuilder children(Node... children) {
        hBox.getChildren().addAll(children);
        return this;
    }

    public HBoxBuilder prefSize(double width, double height) {
        hBox.setPrefSize(width, height);
        return this;
    }

    public HBoxBuilder alignment(Pos pos) {
        hBox.setAlignment(pos);
        return this;
    }

    public HBoxBuilder maxSize(double width, double height) {
        hBox.setMaxSize(width, height);
        return this;
    }

    @Override
    public HBox build() {
        return hBox;
    }
}
