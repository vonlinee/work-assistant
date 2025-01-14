package org.example.workassistant.common.interfaces.impl;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class InsertSqlParseResult {

    /**
     * 插入语句涉及到的表
     */
    private SqlTable table;

    /**
     * 插入的列，INSERT SQL中可以没有列信息
     * INSERT INTO t_user VALUES (1, "zs"), (2, "ls")
     */
    private List<InsertColumn> insertColumns;

    /**
     * 插入字段的值
     */
    private List<List<String>> columnValues;

    public boolean hasAnyInsertColumn() {
        return insertColumns != null && !insertColumns.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.table == null) {
            sb.append("unknown table");
        } else {
            if (this.table.getCatalog() != null) {
                sb.append(this.table.getCatalog()).append(".");
            }
            sb.append(this.table.getName());
            if (this.insertColumns != null) {
                sb.append(" insert ").append(insertColumns.size()).append(" column");
            }
            if (this.columnValues != null) {
                sb.append(" ").append(columnValues.size()).append(" rows");
            }
        }
        return sb.toString();
    }
}
