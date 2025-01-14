package org.example.workassistant.common.interfaces.impl;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * ddl sql相关的表信息
 */
@Getter
@Setter
public class CreateSqlTable extends SqlTable {

    private String name;
    private String comment;

    private TableMetadata metadata;
    private List<CreateSqlColumn> columns;

    private List<IndexInfo> indexes;
    private List<Map.Entry<String, String>> options;
}
