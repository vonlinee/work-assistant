package org.workassistant.ui.controller.fields;

import org.workassistant.ui.model.FieldSpec;
import javafx.scene.control.TableView;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FieldImportEvent {

    TableView<FieldSpec> tableView;
    String text;
}
