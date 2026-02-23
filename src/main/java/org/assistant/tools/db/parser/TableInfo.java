package org.assistant.tools.db.parser;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class TableInfo {
    private String name;
    private String remarks; // Table comment
    private List<ColumnInfo> columns = new ArrayList<>();

    public void addColumn(ColumnInfo column) {
        columns.add(column);
    }
}
