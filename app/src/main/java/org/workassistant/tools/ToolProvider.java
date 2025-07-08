package org.workassistant.tools;

import javafx.scene.Node;

public interface ToolProvider {

    String getLabel();

    Node getRoot();
}
