package io.devpl.fxui.controller;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import de.marhali.json5.*;
import io.devpl.fxui.fxtras.mvvm.FxmlBinder;
import io.devpl.fxui.fxtras.mvvm.FxmlView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import lombok.Data;

import javax.lang.model.element.Modifier;
import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

@FxmlBinder(location = "layout/json.fxml")
public class JsonView extends FxmlView {

    @FXML
    public TextArea textArea;
    @FXML
    public TextArea rightArea;
    @FXML
    public TextField packageNameTextField;
    @FXML
    public TextField classNameTextField;
    @FXML
    public TextField addJavadocTextField;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        packageNameTextField.setText("");
    }

    Json5 json5 = new Json5();

    @FXML
    public void convert(ActionEvent actionEvent) {
        final String text = textArea.getText();

        Json5Element root = json5.parse(text);
        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(classNameTextField.getText())
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc(CodeBlock.of(addJavadocTextField.getText()));

        typeBuilder.addAnnotation(Data.class);

        if (root.isJson5Object()) {
            Json5Object rootObject = root.getAsJson5Object();

            final Json5Element data = rootObject.get("Data");

            final Json5Array asJson5Array = data.getAsJson5Array();

            final Json5Element json5Element = asJson5Array.get(0);

            final Json5Object asJson5Object = json5Element.getAsJson5Object();

            for (Map.Entry<String, Json5Element> entry : asJson5Object.entrySet()) {


            }
        }

        JavaFile javaFile = JavaFile.builder(packageNameTextField.getText(), typeBuilder.build()).build();

        try (StringWriter writer = new StringWriter()) {
            javaFile.writeTo(writer);
            rightArea.setText(writer.getBuffer().toString().replace("VALUE", "value"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 类型映射
     *
     * @param element
     * @return
     */
    public TypeName assignType(Json5Element element) {
        if (element.isJson5Null()) {
            return TypeName.get(String.class);
        } else if (element.isJson5Array()) {
            return TypeName.get(List.class);
        } else if (element.isJson5Primitive()) {
            final Json5Primitive asJson5Primitive = element.getAsJson5Primitive();
            if (asJson5Primitive.isNumber()) {
                return TypeName.get(Integer.class);
            } else if (asJson5Primitive.isBoolean()) {
                return TypeName.get(Boolean.class);
            } else if (asJson5Primitive.isString()) {
                return TypeName.get(String.class);
            }
        }
        return TypeName.get(String.class);
    }
}
