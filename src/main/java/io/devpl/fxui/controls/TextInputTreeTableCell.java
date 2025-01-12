package io.devpl.fxui.controls;

import javafx.geometry.Insets;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.util.StringConverter;

public class TextInputTreeTableCell<S, T> extends TextFieldTreeTableCell<S, T> {

    public TextInputTreeTableCell(StringConverter<T> converter) {
        super(converter);
        this.setPadding(new Insets(0));
    }
}
