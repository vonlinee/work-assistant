package io.devpl.fxui.components.form;

import com.dlsc.formsfx.model.structure.StringField;
import com.dlsc.formsfx.view.controls.SimpleControl;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

/**
 * @see com.dlsc.formsfx.view.controls.SimpleBooleanControl
 */
public class TextAreaFormItem extends SimpleControl<StringField> {

    TextArea textArea;
    protected Label fieldLabel;

    public TextAreaFormItem() {
        textArea = new TextArea();
        textArea.setPrefWidth(500.0);
        fieldLabel = new Label();
    }

    @Override
    public void initializeParts() {
        super.initializeParts();
    }

    @Override
    public void layoutParts() {
        super.layoutParts();
        fieldLabel.setText(field.labelProperty().getValue());

        setColumnSpan(fieldLabel, field.getSpan());

        add(fieldLabel, 0, 0);
        add(textArea, 2, 0);
    }
}
