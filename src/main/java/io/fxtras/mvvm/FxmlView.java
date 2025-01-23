package io.fxtras.mvvm;

import javafx.fxml.Initializable;
import javafx.scene.Node;

import java.net.URL;
import java.util.ResourceBundle;

public abstract class FxmlView extends ViewBase implements Initializable {

    @Override
    public final void setRoot(Node root) {
        super.setRoot(root);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}
