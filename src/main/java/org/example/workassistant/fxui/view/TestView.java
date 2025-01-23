package org.example.workassistant.fxui.view;

import org.example.workassistant.fxui.components.TextRegion;
import io.fxtras.mvvm.FxmlBinder;
import io.fxtras.mvvm.FxmlView;
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
