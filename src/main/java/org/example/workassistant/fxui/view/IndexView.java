package org.example.workassistant.fxui.view;

import org.example.workassistant.fxui.fxtras.mvvm.FxmlBinder;
import org.example.workassistant.fxui.fxtras.mvvm.FxmlView;
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
