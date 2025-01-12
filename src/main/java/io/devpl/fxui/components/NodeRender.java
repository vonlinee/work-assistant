package io.devpl.fxui.components;

import javafx.scene.Node;

public abstract class NodeRender<T extends Node> {

    T node;

    public Node getNode() {
        if (node == null) {
            node = render();
        }
        return node;
    }

    /**
     * 初始化节点
     * @return Node
     */
    protected abstract T render();
}
