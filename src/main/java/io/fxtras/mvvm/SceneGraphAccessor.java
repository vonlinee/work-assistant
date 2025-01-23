package io.fxtras.mvvm;

import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * @since 17
 */
public interface SceneGraphAccessor {

    /**
     * 从事件获取当前事件源所在的舞台对象
     * When accessing a Stage, timing is important, as the Stage is not created
     * until the very end of a View-creation process.
     * <a href="https://edencoding.com/stage-controller/">...</a>
     * @param event JavaFX event
     * @return 当前事件源所在的舞台对象
     * @throws RuntimeException 如果事件源不是Node
     */
    default Stage getStage(Event event) {
        final Object source = event.getSource();
        if (source instanceof Node node) {
            return getStage(node);
        }
        throw new RuntimeException("event source is [" + source.getClass() + "] instead of a [Node]");
    }

    /**
     * 从节点获取Stage
     * @param node 节点，需要已被绑定到场景图中
     * @return 返回节点所在Stage
     */
    default Stage getStage(Node node) {
        final Scene scene = node.getScene();
        if (scene == null) {
            throw new RuntimeException("node [" + node + "] has not been bind to a scene!");
        }
        final Window window = scene.getWindow();
        if (window instanceof Stage) {
            return (Stage) window;
        }
        throw new RuntimeException("the window [" + window + "] is not a stage");
    }
}
