package io.devpl.fxui.tools.filestructure;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.HBox;
import org.girod.javafx.svgimage.SVGImage;

public class JavaElementItem extends TreeItem<String> {

    /**
     * 可见性：默认私有
     */
    private final ObjectProperty<JavaVisibility> visibility = new SimpleObjectProperty<>(JavaVisibility.PRIVATE);

    JavaElementItem(SVGImage typeIcon) {
        HBox graphicContainer = new HBox();
        graphicContainer.setSpacing(4.0);
        graphicContainer.setAlignment(Pos.CENTER);
        graphicContainer.getChildren().addAll(typeIcon, visibility.get().getIconNode());
        this.setGraphic(graphicContainer);

        // 更换可见性对应的图标
        visibility.addListener((observable, oldValue, newValue) -> {
            ObservableList<Node> children = graphicContainer.getChildren();
            // 更换图标
            if (children.size() == 2) {
                children.set(1, newValue.getIconNode());
            }
        });
    }

    public JavaVisibility getVisibility() {
        return visibility.get();
    }

    public ObjectProperty<JavaVisibility> visibilityProperty() {
        return visibility;
    }

    public void setVisibility(JavaVisibility visibility) {
        this.visibility.set(visibility);
    }
}
