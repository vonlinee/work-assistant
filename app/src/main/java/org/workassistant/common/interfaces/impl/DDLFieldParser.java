package org.workassistant.common.interfaces.impl;

import org.workassistant.common.exception.FieldParseException;
import org.workassistant.common.interfaces.FieldParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 从DDL中解析字段
 * <a href="https://juejin.cn/post/7083280831602982919">...</a>
 */
public class DDLFieldParser implements FieldParser {

    private final String dbType;
    private final SqlParser sqlParser;

    public DDLFieldParser(String dbType) {
        this.dbType = dbType;
        this.sqlParser = DruidSqlParser.createSqlParser(dbType);
    }

    @Override
    public List<Map<String, Object>> parse(String sql) throws FieldParseException {
        CreateTableParseResult result = sqlParser.parseCreateTableSql(dbType, sql);

        CreateSqlTable createSqlTable = result.getCreateSqlTable();

        // SQL 注入
//        WallProvider provider = new MySqlWallProvider();
//        WallCheckResult result = provider.check(sql);
//        if (result.getViolations().isEmpty()) {
//            // 无SQL注入风险和错误, 可执行查询
//            List<SQLStatement> sqlStatements = SQLUtils.parseStatements(sql, DbType.mysql);
//            for (SQLStatement stmt : sqlStatements) {
//                stmt.accept(this);
//            }
//        }
        List<Map<String, Object>> list = new ArrayList<>();
        for (CreateSqlColumn createSqlColumn : createSqlTable.getColumns()) {
            Map<String, Object> field = new HashMap<>();
            field.put(FIELD_NAME, createSqlColumn.getName());
            field.put(FIELD_TYPE, createSqlColumn.getDataType());
            field.put(FIELD_DESCRIPTION, createSqlColumn.getComment());
            list.add(field);
        }
        return list;
    }
}
