package org.example.workassistant.fxui.layout.menu;

import lombok.Getter;
import org.example.workassistant.fxui.components.RouterView;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;

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
