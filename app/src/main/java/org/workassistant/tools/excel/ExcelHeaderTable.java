package org.workassistant.tools.excel;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ExcelHeaderTable extends TableView<Header> {

    TableColumn<Header, String> column1 = new TableColumn<>("Title");
    TableColumn<Header, Integer> column2 = new TableColumn<>("Index");

    public ExcelHeaderTable() {
        getColumns().addAll(column1, column2);

        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
}
