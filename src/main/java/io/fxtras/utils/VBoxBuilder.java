package io.fxtras.utils;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class VBoxBuilder implements NodeBuilder<VBox> {

    private final VBox vBox;

    public VBoxBuilder spacing(double spacing) {
        vBox.setSpacing(spacing);
        return this;
    }

    public VBoxBuilder children(Node... children) {
        vBox.getChildren().addAll(children);
        return this;
    }

    public VBoxBuilder alignment(Pos pos) {
        vBox.setAlignment(pos);
        return this;
    }

    public VBoxBuilder prefSize(double width, double height) {
        vBox.setPrefSize(width, height);
        return this;
    }

    public VBoxBuilder maxSize(double width, double height) {
        vBox.setMaxSize(width, height);
        return this;
    }

    /**
     * private constructor
     */
    private VBoxBuilder() {
        this(new VBox());
    }

    /**
     * private constructor
     */
    private VBoxBuilder(VBox vBox) {
        this.vBox = vBox;
    }

    public static VBoxBuilder builder() {
        return builder(new VBox());
    }

    public static VBoxBuilder builder(VBox vBox) {
        return vBox == null ? new VBoxBuilder() : new VBoxBuilder(vBox);
    }

    @Override
    public VBox build() {
        return vBox;
    }
}
