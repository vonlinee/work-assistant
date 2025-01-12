package io.devpl.fxui.components;

import javafx.scene.Node;

/**
 * 路由视图
 */
public final class RouterView {

    private final Node node;
    private boolean lazyInit;
    private boolean keepAlive = false;

    public RouterView(Node node) {
        this.node = node;
    }

    public Node getNode() {
        return node;
    }

    public static RouterView of(Node node) {
        return new RouterView(node);
    }

    public boolean isEmpty() {
        return node == null;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public boolean isLazyInit() {
        return lazyInit;
    }

    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }
}
