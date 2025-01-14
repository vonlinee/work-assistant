package org.example.workassistant.fxui.fxtras.utils;

import javafx.scene.Node;

public interface NodeBuilder<T extends Node> {

    T build();
}
