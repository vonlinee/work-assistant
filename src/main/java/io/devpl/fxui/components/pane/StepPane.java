package io.devpl.fxui.components.pane;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.layout.BorderPane;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 步骤面板
 */
public class StepPane extends BorderPane {

    List<Node> list;
    ButtonBar buttonBar;

    private int index;

    public StepPane() {
        this.list = new ArrayList<>();

        this.buttonBar = new ButtonBar();

        Button btnNext = new Button("下一步");
        Button btnPrev = new Button("上一步");

        buttonBar.getButtons().addAll(btnPrev, btnNext);

        btnPrev.setOnAction(event -> switchPane(index - 1));
        btnNext.setOnAction(event -> switchPane(index + 1));

        setBottom(this.buttonBar);

        // 第一次显示时默认第一步
        sceneProperty().addListener((observable, oldValue, newValue) -> {
            if (!list.isEmpty()) {
                setCenter(list.get(index));
            }
        });
    }

    private void switchPane(int index) {
        if (index < 0) {
            index = list.size() - 1;
        } else if (index >= list.size()) {
            index = 0;
        }
        if (this.index != index) {
            setCenter(list.get(index));
            this.index = index;
        }
    }

    public final void addStepView(int index, Node view) {
        if (index < 0 || index > list.size() - 1) {
            list.add(view);
        } else {
            list.add(index, view);
        }
    }

    public final void addStepView(Node... nodes) {
        this.list.addAll(Arrays.asList(nodes));
    }
}
