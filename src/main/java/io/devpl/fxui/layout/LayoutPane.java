package io.devpl.fxui.layout;

import io.devpl.fxui.layout.menu.NavigationMenu;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeCell;

/**
 * 导航面板
 */
public class LayoutPane extends SplitPane {

    MenuContainer menuPane;
    ContentContainer contentPane;

    public LayoutPane() {
        contentPane = new ContentContainer();

        // 初始化菜单容器
        menuPane = new MenuContainer();
        // 点击菜单切换
        menuPane.setOnMenuClicked(event -> {
            TreeCell<?> cell = (TreeCell<?>) event.getSource();
            NavigationMenu clickedMenu = (NavigationMenu) cell.getTreeItem();
            if (clickedMenu.hasChildren()) {
                clickedMenu.setExpanded(!clickedMenu.isExpanded());
            } else {
                contentPane.switchTo(clickedMenu.getContent());
            }
        });

        // 初始化内容区域
        contentPane.setStyle("-fx-background-color: #c48c8c");

        this.setDividerPosition(0, 0.3);
        this.getItems().addAll(menuPane, contentPane);

        // 宽度自适应
        this.getDividers().get(0).positionProperty()
            .addListener((observable, oldValue, newValue) -> {
                menuPane.setPrefWidth(LayoutPane.this.getWidth() * newValue.doubleValue());
                contentPane.setPrefWidth(LayoutPane.this.getWidth() * (1.0 - newValue.doubleValue()));
            });
        menuPane.prefHeightProperty().bind(this.heightProperty());
        contentPane.prefHeightProperty().bind(this.heightProperty());
    }

    public final void addNavigationMenu(NavigationMenu... menuItems) {
        menuPane.addNavigationMenu(menuItems);
    }

    /**
     * 展开所有菜单
     */
    public final void expandAllMenu() {
        menuPane.expandAll();
    }
}
