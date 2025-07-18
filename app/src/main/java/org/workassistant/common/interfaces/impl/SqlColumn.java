package org.workassistant.common.interfaces.impl;

import lombok.Getter;
import lombok.Setter;

/**
 * sql中的列信息
 */
@Setter
@Getter
public class SqlColumn {

    protected String tableName;
    protected String columnName;
}
