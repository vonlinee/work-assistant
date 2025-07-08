package org.workassistant.common.interfaces.impl;

import com.alibaba.druid.DbType;

/**
 * Druid Sql Parser
 */
public abstract class DruidSqlParser implements SqlParser {

    protected DbType dbType;

    public static SqlParser createSqlParser(String dbType) {
        DbType dbTypeEnum = DbType.of(dbType);
        if (dbTypeEnum == DbType.mysql) {
            return new DruidMySqlParser();
        }
        throw new UnsupportedOperationException("不支持的数据库类型" + dbType);
    }
}
