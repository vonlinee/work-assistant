package io.devpl.fxui.view;

import io.devpl.fxui.editor.CodeEditor;
import io.devpl.fxui.editor.LanguageMode;
import javafx.scene.control.SplitPane;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileGenerationResultView extends SplitPane {

    FileTreeView treeView;
    CodeEditor editor;

    public FileGenerationResultView(File root) {
        treeView = new FileTreeView(root);
        editor = CodeEditor.newInstance(LanguageMode.JAVA);
        treeView.setOnNodeClickHandler(f -> {
            File file = new File(f.getAbsolutePath());
            try {
                editor.setText(Files.readString(file.toPath()), false);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        getItems().addAll(treeView, editor.getView());
    }

    public final void setRoot(File root) {
        this.treeView.updateRoot(root);
    }
}
