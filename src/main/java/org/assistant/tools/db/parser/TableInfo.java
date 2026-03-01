package org.assistant.tools.db.parser;

import java.util.ArrayList;
import java.util.List;

public class TableInfo {
    private String name;
    private String remarks; // Table comment
    private List<ColumnInfo> columns = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public List<ColumnInfo> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnInfo> columns) {
        this.columns = columns;
    }

    public void addColumn(ColumnInfo column) {
        columns.add(column);
    }
}
