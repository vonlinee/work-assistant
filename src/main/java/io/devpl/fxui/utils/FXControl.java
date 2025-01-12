package io.devpl.fxui.utils;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;

public class FXControl {

    public static Button button(String text, EventHandler<ActionEvent> actionEventEventHandler) {
        Button button = new Button(text);
        button.setOnAction(actionEventEventHandler);
        return button;
    }

    public static Button button(String text, boolean fixText, EventHandler<ActionEvent> actionEventEventHandler) {
        Button button = new Button();
        button.setOnAction(actionEventEventHandler);
        if (fixText) {
            FXUtils.setText(button, text);
        } else {
            button.setText(text);
        }
        return button;
    }

    public static Label label(String text) {
        Label label = new Label(text);
        label.setAlignment(Pos.CENTER_LEFT);
        return label;
    }

    public static MenuItem menuItem(String text, EventHandler<ActionEvent> actionEventHandler) {
        MenuItem menuItem = new MenuItem(text);
        menuItem.setOnAction(actionEventHandler);
        return menuItem;
    }
}
