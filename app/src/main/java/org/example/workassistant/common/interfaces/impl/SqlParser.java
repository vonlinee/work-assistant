package org.example.workassistant.common.interfaces.impl;

/**
 * 屏蔽各sql解析器的实现，方便切换底层使用的sql解析器
 */
public interface SqlParser {

    /**
     * 解析 select sql
     *
     * @param dbType 数据库类型
     * @param sql    sql语句
     * @return 解析结果
     */
    SelectSqlParseResult parseSelectSql(String dbType, String sql);

    /**
     * 解析 create table sql
     *
     * @param dbType 数据库类型
     * @param sql    sql语句
     * @return 解析结果
     */
    CreateTableParseResult parseCreateTableSql(String dbType, String sql);

    /**
     * 解析 insert sql
     *
     * @param dbType 数据库类型
     * @param sql    sql语句
     * @return 解析结果
     */
    InsertSqlParseResult parseInsertSql(String dbType, String sql);

    /**
     * 解析 update sql
     *
     * @param dbType 数据库类型
     * @param sql    sql语句
     * @return 解析结果
     */
    UpdateSqlParseResult parseUpdateSql(String dbType, String sql);

    /**
     * 解析 alter sql
     *
     * @param dbType 数据库类型
     * @param sql    sql语句
     * @return 解析结果
     */
    AlterSqlParseResult parseAlterSql(String dbType, String sql);
}
