package io.devpl.fxui.tools.navigation.impl;

import io.devpl.fxui.model.ConnectionConfig;
import io.devpl.fxui.tools.IconKey;
import io.devpl.fxui.tools.IconMap;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * 数据库导航视图
 *
 * @see TreeItem 如果将泛型定义为String，则TreeItem不能携带除String以外的任何数据
 */
public final class DBTreeView extends TreeView<String> {

    public DBTreeView() {
        setRoot(new TreeItem<>());
        setShowRoot(false);
        setEditable(false);
        setCellFactory(param -> new DBTreeCell());
    }

    public void addConnection(ConnectionConfig connectionInfo) {
        TreeItem<String> connTreeItem = new TreeItem<>(connectionInfo.getConnectionName(), IconMap.loadSVG(IconKey.DB_MYSQL));
        getRoot().getChildren().add(connTreeItem);
    }
}
