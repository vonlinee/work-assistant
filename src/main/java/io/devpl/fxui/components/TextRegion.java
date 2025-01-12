package io.devpl.fxui.components;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;

/**
 * 文本区域
 */
public class TextRegion extends Region {

    TextArea textArea;

    public TextRegion() {
        textArea = new TextArea();
        // 禁止换行
        textArea.setWrapText(false);
        textArea.setFont(Font.font(12));

        getChildren().add(textArea);
    }

    @Override
    protected void layoutChildren() {
        layoutInArea(textArea, 0, 0, getWidth(), getHeight(), 0, HPos.CENTER, VPos.CENTER);
    }

    public String getText() {
        return textArea.getText();
    }

    public void setText(String text) {
        textArea.setText(text);
    }
}
