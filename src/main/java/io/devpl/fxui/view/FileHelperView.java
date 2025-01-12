package io.devpl.fxui.view;

import io.devpl.fxui.fxtras.mvvm.FxmlBinder;
import io.devpl.fxui.utils.Helper;
import io.devpl.fxui.fxtras.mvvm.FxmlView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@FxmlBinder(location = "fxml/filehelper.fxml", label = "文件助手")
public class FileHelperView extends FxmlView {

    @FXML
    public ListView<File> lsvFiles;
    @FXML
    public TextField txfInitDir;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);

        lsvFiles.setCellFactory(param -> {
            TextFieldListCell<File> cell = new TextFieldListCell<>();

            cell.setConverter(new StringConverter<>() {
                @Override
                public String toString(File object) {
                    return object.getAbsolutePath();
                }

                @Override
                public File fromString(String string) {
                    return new File(string);
                }
            });

            return cell;
        });

    }

    @FXML
    public void selectFile(ActionEvent actionEvent) {
        FileChooser chooser = new FileChooser();
        if (Helper.hasText(txfInitDir.getText())) {
            chooser.setInitialDirectory(new File(txfInitDir.getText()));
        }
        List<File> files = chooser.showOpenMultipleDialog(getStage(actionEvent));
        if (files != null && !files.isEmpty()) {
            files.stream().filter(file -> !lsvFiles.getItems().contains(file)).forEach(file -> lsvFiles.getItems().add(file));
        }
    }
}
