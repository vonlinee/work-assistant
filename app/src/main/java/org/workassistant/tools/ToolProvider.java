package org.workassistant.tools;

import javafx.scene.Node;

public interface ToolProvider {

    String getLabel();

    Node getRoot();

    default int getOrder() {
        return Integer.MAX_VALUE;
    }
}
