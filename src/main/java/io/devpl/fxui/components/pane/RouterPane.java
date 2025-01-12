package io.devpl.fxui.components.pane;

import io.devpl.fxui.components.NodeRender;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;

import java.util.HashMap;
import java.util.Map;

/**
 * 路由面板
 */
public class RouterPane extends ScrollPane {

    private final ObjectProperty<Object> currentKey = new SimpleObjectProperty<>();

    private final Map<Object, Object> routeTable = new HashMap<>();

    public RouterPane() {
        this.setHbarPolicy(ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollBarPolicy.AS_NEEDED);
        // 自适应宽度
        setFitToWidth(true);
        setFitToHeight(true);

        currentKey.addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                Object target = routeTable.get(newValue);
                if (target instanceof Node node) {
                    setContent(node);
                } else if (target instanceof NodeRender<?> renderFunction) {
                    Node node = renderFunction.getNode();
                    if (node != null) {
                        setContent(node);
                    }
                }
            }
        });
    }

    public void addRouteMapping(Object key, Node routeNode) {
        routeTable.put(key, routeNode);
    }

    public void addRouteMapping(Object key, NodeRender<?> routeNodeSupplier) {
        routeTable.put(key, routeNodeSupplier);
    }

    public void setCurrentRoute(Object key) {
        currentKey.set(key);
    }
}
