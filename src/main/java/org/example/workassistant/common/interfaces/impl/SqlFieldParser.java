package org.example.workassistant.common.interfaces.impl;

import org.example.workassistant.common.exception.FieldParseException;
import org.example.workassistant.common.interfaces.FieldParser;
import org.example.workassistant.sdk.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 从查询Sql中解析字段
 * <a href="https://juejin.cn/post/7083280831602982919">...</a>
 */
public class SqlFieldParser implements FieldParser {

    private final String dbType;

    SqlParser parser;

    public SqlFieldParser(String dbType) {
        this.dbType = dbType;
        parser = DruidSqlParser.createSqlParser(dbType);
    }

    @Override
    public List<Map<String, Object>> parse(String sql) throws FieldParseException {
        List<Map<String, Object>> list = new ArrayList<>();

        SelectSqlParseResult result = parser.parseSelectSql(this.dbType, sql);
        for (SelectColumn selectColumn : result.getSelectColumns()) {
            Map<String, Object> field = new HashMap<>();
            field.put(FIELD_NAME, selectColumn.getColumnName());
            // TODO 推断类型
            field.put(FIELD_TYPE, "String");
            field.put(FIELD_DESCRIPTION, "");
            list.add(field);

            // 添加别名字段
            if (StringUtils.hasText(selectColumn.getAlias())) {
                Map<String, Object> aliasField = new HashMap<>();
                field.put(FIELD_NAME, selectColumn.getAlias());
                // TODO 推断类型
                field.put(FIELD_TYPE, "String");
                field.put(FIELD_DESCRIPTION, "");
                list.add(aliasField);
            }
        }
        return list;
    }
}
