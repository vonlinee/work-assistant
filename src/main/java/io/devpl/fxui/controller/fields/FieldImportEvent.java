package io.devpl.fxui.controller.fields;

import io.devpl.fxui.model.FieldSpec;
import javafx.scene.control.TableView;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FieldImportEvent {

    TableView<FieldSpec> tableView;
    String text;
}
