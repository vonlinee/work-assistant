package org.assistant.tools.db.parser;

import java.util.ArrayList;
import java.util.List;

public class DbSchema {
    private String catalog;
    private String schema;
    private List<TableInfo> tables = new ArrayList<>();

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public List<TableInfo> getTables() {
        return tables;
    }

    public void setTables(List<TableInfo> tables) {
        this.tables = tables;
    }

    public void addTable(TableInfo table) {
        tables.add(table);
    }
}
