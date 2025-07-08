package org.workassistant.ui.layout;

import io.fxtras.scene.RouterView;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import lombok.Getter;

/**
 * @see javafx.scene.control.MenuItem
 */
@Getter
public final class NavigationMenu extends TreeItem<String> {

    private Node content;

    public NavigationMenu(String title, Node content) {
        super(title);
        this.content = content;
    }

    public NavigationMenu(String title) {
        super(title);
        this.content = null;
    }

    public Node setContent(Node newContent) {
        Node old = this.content;
        this.content = newContent;
        return old;
    }

    public boolean hasChildren() {
        return !getChildren().isEmpty();
    }

    public void addChild(String text, RouterView view) {
        this.getChildren().add(new NavigationMenu(text, view.getNode()));
    }
}
