package org.assistant.tools.db.parser;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class DbSchema {
    private String catalog;
    private String schema;
    private List<TableInfo> tables = new ArrayList<>();

    public void addTable(TableInfo table) {
        tables.add(table);
    }
}
