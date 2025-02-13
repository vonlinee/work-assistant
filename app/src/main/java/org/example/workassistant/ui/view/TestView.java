package org.example.workassistant.ui.view;

import io.fxtras.scene.TextRegion;
import io.fxtras.scene.mvvm.FxmlBinder;
import io.fxtras.scene.mvvm.FxmlView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

@FxmlBinder(location = "fxml/test.fxml")
public class TestView extends FxmlView {

    @FXML
    public TextRegion leftTextArea;
    @FXML
    public TextRegion rightTextArea;

    @FXML
    public void onButtonClick(ActionEvent actionEvent) {
        String text = leftTextArea.getText();
        if (text == null || text.isBlank()) {
            return;
        }
    }
}
