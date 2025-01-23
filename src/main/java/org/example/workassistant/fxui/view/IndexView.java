package org.example.workassistant.fxui.view;

import io.fxtras.mvvm.FxmlBinder;
import io.fxtras.mvvm.FxmlView;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

@FxmlBinder(location = "fxml/index.fxml")
public class IndexView extends FxmlView {

    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}
