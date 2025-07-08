package org.workassistant.tools.excel;

import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import org.workassistant.tools.ToolProvider;

public class ExcelTool implements ToolProvider {

    BorderPane root;

    @Override
    public String getLabel() {
        return "Excel";
    }

    @Override
    public Node getRoot() {
        if (root == null) {
            root = new BorderPane();
            ExcelJoiner joiner = new ExcelJoiner();
            root.setCenter(joiner);
        }
        return root;
    }
}
