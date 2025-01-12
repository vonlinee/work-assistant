package io.devpl.fxui.app;

import io.devpl.fxui.common.ExceptionDialog;
import io.devpl.fxui.components.table.OperationColumnTableCell;
import io.devpl.fxui.components.table.TableVIewBase;
import io.devpl.fxui.utils.FXControl;
import io.devpl.fxui.utils.FXUtils;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.layout.HBox;
import javafx.util.Callback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class JavaProcessTableBase extends TableVIewBase<JavaProcessInfo> {

    public JavaProcessTableBase() {
        FXUtils.initTableViewColumns(this, JavaProcessInfo.class);

        TableColumn<JavaProcessInfo, String> optColumn = new TableColumn<>("操作");
        optColumn.setCellFactory(new Callback<>() {
            @Override
            public TableCell<JavaProcessInfo, String> call(TableColumn<JavaProcessInfo, String> param) {
                return new OperationColumnTableCell<>(param) {

                    @Override
                    protected void init(HBox container) {
                        container.getChildren().add(FXControl.button("停止", event -> {
                            JavaProcessInfo selectedItem = getTableView().getSelectionModel().getSelectedItem();
                            try {
                                Runtime.getRuntime().exec("kill " + selectedItem.getPid());
                            } catch (IOException e) {
                                ExceptionDialog.show(e);
                            }
                            refreshData();
                        }));
                    }
                };
            }
        });

        addColumns(optColumn);
    }

    public void refreshData() {
        Runtime runtime = Runtime.getRuntime();
        try (BufferedReader br = new BufferedReader(
            new InputStreamReader(
                runtime.exec("jps").getInputStream(),
                Charset.defaultCharset()
            )
        )) {
            String line;
            StringBuilder b = new StringBuilder();

            clearItems();

            while ((line = br.readLine()) != null) {
                String[] columns = line.split(" ");

                if (columns.length < 2) {
                    continue;
                }
                JavaProcessInfo row = new JavaProcessInfo();

                row.setPid(columns[0]);
                row.setMainClassName(columns[1]);

                addItem(row);
            }
            System.out.println(b);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
