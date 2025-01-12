package io.devpl.fxui.layout;

import io.devpl.fxui.layout.menu.NavigationMenu;
import io.devpl.fxui.utils.FXUtils;
import javafx.event.EventHandler;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

/**
 * 菜单容器
 */
public class MenuContainer extends Region {

    private final TreeView<String> menuTreeView;

    private EventHandler<MouseEvent> menuClickedHandler;

    public MenuContainer() {
        this.menuTreeView = new TreeView<>();
        this.menuTreeView.setRoot(new TreeItem<>());
        this.menuTreeView.setShowRoot(false);

        getChildren().add(menuTreeView);
        menuTreeView.setCellFactory(param -> {
            TextFieldTreeCell<String> cell = new TextFieldTreeCell<>();
            cell.setOnMouseClicked(event -> {
                Object source = event.getSource();
                if (source instanceof TreeCell<?> treeCell) {
                    TreeItem<?> treeItem = treeCell.getTreeItem();
                    if (treeItem instanceof NavigationMenu menu && this.menuClickedHandler != null) {
                        this.menuClickedHandler.handle(event);
                    }
                }
            });
            return cell;
        });
    }

    public final void setOnMenuClicked(EventHandler<MouseEvent> menuClickedHandler) {
        this.menuClickedHandler = menuClickedHandler;
    }

    @Override
    protected void layoutChildren() {
        FXUtils.layoutInRegion(this, menuTreeView);
    }

    public final void addNavigationMenu(NavigationMenu ... menuItem) {
        this.menuTreeView.getRoot().getChildren().addAll(menuItem);
    }

    public void expandAll() {
        expand(menuTreeView.getRoot());
    }

    public void expand(TreeItem<?> treeItem) {
        if (treeItem == null) {
            return;
        }
        if (!treeItem.isExpanded()) {
            treeItem.setExpanded(true);
        }
        if (!treeItem.getChildren().isEmpty()) {
            for (TreeItem<?> child : treeItem.getChildren()) {
                expand(child);
            }
        }
    }
}
