package io.devpl.fxui.app;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.view.controls.SimpleCheckBoxControl;
import com.dlsc.formsfx.view.renderer.FormRenderer;
import io.devpl.fxui.fxtras.JavaFXApplication;
import javafx.beans.property.*;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestApp extends JavaFXApplication {

    static class Person {

        StringProperty name = new SimpleStringProperty();
        IntegerProperty age = new SimpleIntegerProperty();
        BooleanProperty active = new SimpleBooleanProperty();
        ObjectProperty<LocalDate> bornDate = new SimpleObjectProperty<>();

        @Override
        public String toString() {
            return "Person{" +
                   "name=" + name.get() +
                   ", age=" + age.get() +
                   ", active=" + active.get() +
                   ", bornDate=" + bornDate.get() +
                   '}';
        }
    }

    Person person = new Person();

    @Override
    public void start(Stage primaryStage) throws Exception {

        FormRenderer renderer = new FormRenderer(Form.of(
            Group.of(
                Field.ofStringType(person.name)
                    .label("姓名")
                    .required("项目名称不能为空"),
                Field.ofIntegerType(person.age)
                    .label("年龄")
                    .required("年龄不能为空"),
                Field.ofDate(person.bornDate)
                    .label("出生日期")
                    .required("出生日期不能为空"),
                Field.ofSingleSelectionType(Arrays.asList("Zürich (ZH)", "Bern (BE)"), 1)
                    .label("Capital"),
                Field.ofMultiSelectionType(Arrays.asList("Africa", "Asia"), Collections.singletonList(2))
                    .label("Continent")
                    .render(new SimpleCheckBoxControl<>()),
                Field.ofMultiSelectionType(Arrays.asList("Zürich (ZH)", "Bern (BE)"), List.of(1))
                    .label("Biggest Cities")
            )
        ).title("Login"));

        ToolBar toolBar = new ToolBar();

        Button btn = new Button("Show");
        toolBar.getItems().add(btn);

        btn.setOnAction(event -> System.out.println(person));

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(toolBar);
        borderPane.setCenter(renderer);

        primaryStage.setScene(new Scene(borderPane, 600, 400));
        primaryStage.show();
    }
}
