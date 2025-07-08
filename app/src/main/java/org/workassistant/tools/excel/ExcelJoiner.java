package org.workassistant.tools.excel;

import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;

/**
 * EXCEL JOIN操作
 */
public class ExcelJoiner extends BorderPane {

    ExcelHeaderTable table1;
    ExcelHeaderTable table2;

    public ExcelJoiner() {

        table1 = new ExcelHeaderTable();
        table2 = new ExcelHeaderTable();

        SplitPane center = new SplitPane(table1, table2);

        setCenter(center);
    }
}
