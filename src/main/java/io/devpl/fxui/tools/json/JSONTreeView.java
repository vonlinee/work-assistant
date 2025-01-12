package io.devpl.fxui.tools.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.*;

import java.util.Map;

/**
 * JSON树结构视图 - 可视化
 */
public class JSONTreeView extends TreeTableView<JSONNode> {

    public JSONTreeView() {
        initialize();
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        setRowFactory(param -> {
            TreeTableRow<JSONNode> row = new TreeTableRow<>();
            row.setOnMouseClicked(event -> {
                @SuppressWarnings("unchecked")
                TreeTableRow<JSONNode> clickedRow = (TreeTableRow<JSONNode>) event.getSource();
                TreeItem<JSONNode> treeItem = clickedRow.getTreeItem();
                System.out.println(treeItem.getValue());
            });
            return row;
        });
    }

    private void initialize() {
        setColumnResizePolicy(TreeTableView.CONSTRAINED_RESIZE_POLICY);
        TreeItem<JSONNode> root = new TreeItem<>();
        setRoot(root);
        setShowRoot(false);
        // 第一列
        TreeTableColumn<JSONNode, String> keyColumn = new TreeTableColumn<>("Key");
        keyColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getKey()));
        getColumns().add(keyColumn);
        // 第二列
        TreeTableColumn<JSONNode, String> valueColumn = new TreeTableColumn<>("Value");
        valueColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getValue().getValue()));
        getColumns().add(valueColumn);
    }

    public void addRootJson(JsonElement jsonElement) {
        addJsonFile(getRoot(), "Root", jsonElement);
    }

    /**
     * 添加JSON树根节点
     * 从根节点开始
     * @param key               父JSON key
     * @param parentTreeItem    父TreeItem
     * @param parentJsonElement 父JSON Element
     */
    private void addJsonFile(TreeItem<JSONNode> parentTreeItem, String key, JsonElement parentJsonElement) {
        if (parentJsonElement.isJsonPrimitive()) {
            JSONNode value = new JSONNode(key, parentJsonElement.getAsJsonPrimitive());
            parentTreeItem.getChildren().add(new TreeItem<>(value));
        } else if (parentJsonElement.isJsonNull()) {
            JSONNode value = new JSONNode(key, parentJsonElement.getAsJsonPrimitive());
            parentTreeItem.getChildren().add(new TreeItem<>(value));
        } else if (parentJsonElement.isJsonObject()) {
            // 递归添加子节点
            JsonObject jsonObject = parentJsonElement.getAsJsonObject();
            JSONNode value = new JSONNode(key, jsonObject);
            TreeItem<JSONNode> item = new TreeItem<>(value);
            parentTreeItem.getChildren().add(item);
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                addJsonFile(item, entry.getKey(), entry.getValue());
            }
        } else if (parentJsonElement.isJsonArray()) {
            // 递归添加子节点
            JsonArray jsonArray = parentJsonElement.getAsJsonArray();
            JSONNode jsonArrayItem = new JSONNode(key, jsonArray);
            TreeItem<JSONNode> root = new TreeItem<>(jsonArrayItem);
            parentTreeItem.getChildren().add(root);
            int index = 0;
            for (JsonElement jsonElement : jsonArray) {
                addJsonFile(root, "[" + index++ + "]", jsonElement);
            }
        }
    }
}
