package io.fxtras.utils;

import javafx.scene.Node;

public interface NodeBuilder<T extends Node> {

    T build();
}
