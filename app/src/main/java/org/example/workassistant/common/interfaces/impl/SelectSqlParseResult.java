package org.example.workassistant.common.interfaces.impl;

import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询sql解析结果
 */
@Getter
@Setter
public class SelectSqlParseResult {

    private List<SQLSelectItem> selectItems = new ArrayList<>();

    /**
     * 查询的表
     */
    private List<SelectTable> selectTables = new ArrayList<>();

    /**
     * 查询的列
     */
    private List<SelectColumn> selectColumns = new ArrayList<>();

    /**
     * 查询的参数
     */
    private List<String> parameters = new ArrayList<>();
}
