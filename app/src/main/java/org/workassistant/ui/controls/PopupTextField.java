package org.workassistant.ui.controls;

import io.github.palexdev.materialfx.controls.MFXTextField;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import org.workassistant.ui.tools.IconKey;

public class PopupTextField extends StackPane {

    MFXTextField textField;
    IconRegion expand;

    public PopupTextField() {
        textField = new MFXTextField();
        expand = new IconRegion(IconKey.EXPAND);

        getChildren().add(textField);
        getChildren().add(expand);

        StackPane.setAlignment(expand, Pos.CENTER_RIGHT);

        textField.prefWidthProperty().bind(this.widthProperty());
    }
}
