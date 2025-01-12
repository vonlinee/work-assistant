package io.devpl.fxui.components;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

/**
 * @see javafx.stage.Popup
 */
public class Modal extends Stage {

    private final ContentRegion contentRegion;

    public Modal() {
        this(400.0, 500.0);
    }

    public Modal(double width, double height) {
        setScene(new Scene(contentRegion = new ContentRegion(), 400.0, 400.0));
    }

    public final void setContent(Node content) {
        this.contentRegion.setContent(content);
    }

    public static void show(String title, Parent node) {
        Modal modal = new Modal();
        modal.setTitle(title);
        modal.setContent(node);
        modal.show();
    }

    public static Modal of(String title, Region root, double w, double h) {
        Modal modal = new Modal();
        modal.setTitle(title);
        modal.setWidth(w == Region.USE_COMPUTED_SIZE ? root.getWidth() : w);
        modal.setHeight(h == Region.USE_COMPUTED_SIZE ? root.getHeight() : h);
        modal.setHeight(h);
        modal.setContent(root);
        return modal;
    }

    public static void show(String title, Region root, double w, double h) {
        of(title, root, w, h).show();
    }

    public static void show(String title, Region region) {
        show(title, region, Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
    }
}
