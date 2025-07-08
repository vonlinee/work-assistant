package org.workassistant.tools.text;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import org.workassistant.tools.ToolProvider;
import org.workassistant.ui.controls.Option;
import org.workassistant.util.StringUtils;

public class TextHandlePane extends BorderPane implements ToolProvider {

    TextField separator;

    TextArea input;
    TextArea output;

    SplitPane center;
    FlowPane bottom;

    @Override
    public String getLabel() {
        return "文本处理";
    }

    enum Separator implements Option {

        COMMA() {
            @Override
            public String getLabel() {
                return "逗号 ,";
            }

            @Override
            public Object getValue() {
                return ",";
            }
        },

        SEPARATOR() {
            @Override
            public String getLabel() {
                return "换行符 \n";
            }

            @Override
            public Object getValue() {
                return "\n";
            }
        },

        FENHAO() {
            @Override
            public String getLabel() {
                return "分号 ;";
            }

            @Override
            public Object getValue() {
                return ";";
            }
        };


        @Override
        public Option deserialize(String value) {
            return valueOf(value);
        }
    }

    @Override
    public Node getRoot() {
        separator = new TextField(",");

        ComboBox<Option> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(Separator.values());
        Label label = new Label("Separator");
        label.setAlignment(Pos.CENTER);
        HBox hBox = new HBox(label, separator, comboBox);

        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Option object) {
                if (object == null) {
                    return null;
                }
                return object.getLabel();
            }

            @Override
            public Option fromString(String string) {
                return null;
            }
        });
        comboBox.valueProperty().addListener(new ChangeListener<Option>() {
            @Override
            public void changed(ObservableValue<? extends Option> observable, Option oldValue, Option newValue) {
                separator.setText(String.valueOf(newValue.getValue()));
            }
        });


        hBox.setSpacing(5);
        hBox.setAlignment(Pos.CENTER);

        setTop(hBox);

        input = new TextArea();
        output = new TextArea();

        center = new SplitPane(input, output);

        setCenter(center);

        bottom = new FlowPane();

        setBottom(bottom);

        addTextHandlers(TextHandlerEnum.values());

        return this;
    }

    public void addTextHandlers(TextHandler... textHandlers) {
        ObservableList<Node> children = bottom.getChildren();
        for (TextHandler textHandler : textHandlers) {
            Button btn = new Button(textHandler.getLabel());
            btn.setPrefWidth(200.0);
            btn.setOnMouseClicked(event -> accept(textHandler));
            children.add(btn);
        }
    }

    public void accept(TextHandler handler) {
        String text = input.getText();
        if (text == null || text.isEmpty()) {
            return;
        }
        String outputText = handler.handle(text);
        if (output != null) {
            output.setText(outputText);
        }
    }

    enum TextHandlerEnum implements TextHandler {
        CAMEL_TO_UNDERLINE {
            @Override
            public String handle(String input) {
                return StringUtils.toCamelCase(input);
            }
        },
        UNDERLINE_TO_CAMEL {
            @Override
            public String handle(String input) {
                return StringUtils.toUnderScoreCase(input);
            }
        },
        ALL_LOWER_CASE {
            @Override
            public String handle(String input) {
                return input.toLowerCase();
            }
        },
        ALL_UPPER_CASE {
            @Override
            public String handle(String input) {
                return input.toUpperCase();
            }
        },
        ;

        @Override
        public String getLabel() {
            return name();
        }
    }
}
