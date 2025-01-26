package org.example.workassistant.ui.model;

import org.example.workassistant.utils.Helper;
import org.mybatis.generator.config.ColumnOverride;
import org.mybatis.generator.config.IgnoredColumn;

import java.util.List;

/**
 * 长驻内存：每个表对应一个单例
 * 表生成配置
 */
public class TableGeneration {

    /**
     * 数据库连接名称
     */
    private String connectionName;

    /**
     * 该表所在的数据库
     */
    private String databaseName;

    /**
     * 该表名称
     */
    private String tableName;

    /**
     * 唯一标识
     */
    private String uniqueKey;

    /**
     * 表生成选项
     */
    private CodeGenOption option = new CodeGenOption();

    /**
     * 忽略的列
     */
    private List<IgnoredColumn> ignoredColumns;

    /**
     * 覆盖的列
     */
    private List<ColumnOverride> columnOverrides;

    /**
     * 唯一标识符
     *
     * @return 唯一标识符  连接
     */
    public String getUniqueKey() {
        if (uniqueKey == null) {
            uniqueKey = connectionName + "#" + databaseName + "#" + tableName;
        }
        return uniqueKey;
    }

    public String getMapperName() {
        String tableNameCamel = Helper.underlineToCamel(tableName);
        tableNameCamel = Helper.upperFirst(tableNameCamel);
        return tableNameCamel + "Mapper";
    }

    public String getDomainObjectName() {
        String tableNameCamel = Helper.underlineToCamel(tableName);
        tableNameCamel = Helper.upperFirst(tableNameCamel);
        return tableNameCamel;
    }

    public String getConnectionName() {
        return this.connectionName;
    }

    public String getDatabaseName() {
        return this.databaseName;
    }

    public String getTableName() {
        return this.tableName;
    }

    public CodeGenOption getOption() {
        return this.option;
    }

    public List<IgnoredColumn> getIgnoredColumns() {
        return this.ignoredColumns;
    }

    public List<ColumnOverride> getColumnOverrides() {
        return this.columnOverrides;
    }

    public void setConnectionName(String connectionName) {
        this.connectionName = connectionName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    public void setOption(CodeGenOption option) {
        this.option = option;
    }

    public void setIgnoredColumns(List<IgnoredColumn> ignoredColumns) {
        this.ignoredColumns = ignoredColumns;
    }

    public void setColumnOverrides(List<ColumnOverride> columnOverrides) {
        this.columnOverrides = columnOverrides;
    }
}
