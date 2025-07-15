package org.workassistant.ui.view;

import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;

public class AboutMe extends BorderPane {

    TextField textField;

    public AboutMe() {
        textField = new TextField();
        textField.setPrefSize(400, 500);

        setCenter(textField);
    }
}
