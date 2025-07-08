package org.workassistant.common.interfaces.impl;

import lombok.Getter;
import lombok.Setter;

/**
 * 查询列
 */
@Getter
@Setter
public class SelectColumn extends SqlColumn {

    protected String alias;

    public SelectColumn() {
    }

    public SelectColumn(String table, String name, String alias) {
        this.tableName = table;
        this.columnName = name;
        this.alias = alias;
    }

    public final boolean isSelectAll() {
        return "*".equals(columnName);
    }
}
