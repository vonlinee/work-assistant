package org.example.workassistant.ui.controller.fields;

import org.example.workassistant.ui.model.FieldSpec;
import javafx.scene.control.TableView;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FieldImportEvent {

    TableView<FieldSpec> tableView;
    String text;
}
