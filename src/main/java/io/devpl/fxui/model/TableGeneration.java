package io.devpl.fxui.model;

import io.devpl.fxui.utils.Helper;
import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.config.ColumnOverride;
import org.mybatis.generator.config.IgnoredColumn;

import java.util.List;

/**
 * 长驻内存：每个表对应一个单例
 * 表生成配置
 */
@Getter
@Setter
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
}
