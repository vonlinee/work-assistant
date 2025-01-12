package io.devpl.fxui.view;

import javafx.scene.Node;

/**
 * TODO 所有视图节点都继承此类
 */
public abstract class VNode implements LifeCycle {

    /**
     * 获取真实的JavaFX节点
     *
     * @return Node
     * @see Node
     */
    public abstract Node getNode();
}
