package io.devpl.common.interfaces.impl;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.*;
import com.alibaba.druid.sql.ast.expr.*;
import com.alibaba.druid.sql.ast.statement.*;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlKey;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlPrimaryKey;
import com.alibaba.druid.sql.dialect.mysql.ast.MySqlUnique;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlCreateTableStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlInsertStatement;
import com.alibaba.druid.sql.dialect.mysql.ast.statement.MySqlUpdateStatement;
import com.alibaba.druid.sql.dialect.mysql.visitor.MySqlSchemaStatVisitor;
import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import com.alibaba.druid.stat.TableStat;
import com.alibaba.druid.wall.WallCheckResult;
import com.alibaba.druid.wall.WallProvider;
import com.alibaba.druid.wall.spi.MySqlWallProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class DruidMySqlParser extends DruidSqlParser {

    @Override
    public SelectSqlParseResult parseSelectSql(String dbType, String sql) {
        WallProvider provider = new MySqlWallProvider();
        WallCheckResult result = provider.check(sql);
        SelectSqlVisitor visitor = new SelectSqlVisitor();
        if (result.getViolations().isEmpty()) {
            // 无SQL注入风险和错误, 可执行查询
            List<SQLStatement> sqlStatements = SQLUtils.parseStatements(sql, DbType.mysql);
            for (SQLStatement stmt : sqlStatements) {
                stmt.accept(visitor);
                return visitor.getResult();
            }
        }
        return null;
    }

    @Override
    public CreateTableParseResult parseCreateTableSql(String dbType, String sql) {
        CreateTableParseResult result = new CreateTableParseResult();
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType);
        if (!(parser.parseStatement() instanceof MySqlCreateTableStatement stmt)) {
            return result;
        }
        CreateSqlTable createSqlTable = parseDDL(sql, DbType.of(dbType));
        result.setCreateSqlTable(createSqlTable);
        return result;
    }

    private CreateSqlTable parseDDL(String ddl, DbType dbType) {
        CreateSqlTable createSqlTable = new CreateSqlTable();
        // 创建解析器
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(ddl, dbType);
        if (!(parser.parseStatement() instanceof MySqlCreateTableStatement stmt)) {
            return createSqlTable;
        }
        MySQLColumnVisitor columnVisitor = new MySQLColumnVisitor();
        // 解析
        stmt.accept(columnVisitor);
        // 表名称
        createSqlTable.setName(SQLUtils.normalize(stmt.getTableName(), dbType));
        // 表备注
        createSqlTable.setComment(SQLUtils.normalize(String.valueOf(stmt.getComment()), dbType));
        // 表配置信息
        List<Map.Entry<String, String>> options = new ArrayList<>();
        for (SQLAssignItem tableOption : stmt.getTableOptions()) {
            options.add(Map.entry(String.valueOf(tableOption.getTarget()), String.valueOf(tableOption.getValue())));
        }
        createSqlTable.setOptions(options);
        // 列信息
        List<CreateSqlColumn> columns = new ArrayList<>();

        Map<String, CreateSqlColumn> columnInfoMap = new HashMap<>();
        for (TableStat.Column column : columnVisitor.getColumns()) {
            CreateSqlColumn createSqlColumn = new CreateSqlColumn();
            createSqlColumn.setName(column.getName());
            createSqlColumn.setFullName(column.getFullName());
            createSqlColumn.setTableName(column.getTable());
            createSqlColumn.setDataType(column.getDataType());
            // 字段注释信息
            Map<String, Object> attributes = column.getAttributes();
            if (attributes != null) {
                createSqlColumn.setComment(String.valueOf(attributes.getOrDefault("comment", "")));
                createSqlColumn.setAttributes(attributes);
            }
            columnInfoMap.put(column.getName(), createSqlColumn);
            columns.add(createSqlColumn);
        }

        List<SQLColumnDefinition> columnDefinitions = stmt.getColumnDefinitions();
        for (SQLColumnDefinition cd : columnDefinitions) {
            // 这里的 columnName 可能包括列名称的引号
            String columnName = cd.getColumnName();
            columnName = columnName.substring(1, columnName.length() - 1);

            CreateSqlColumn createSqlColumn = columnInfoMap.get(columnName);
            if (createSqlColumn != null) {
                // 完整的数据类型定义
                createSqlColumn.setDataTypeDefinition(String.valueOf(cd.getDataType()));
                createSqlColumn.setCharsetDefinition(String.valueOf(cd.getCharsetExpr()));
            }
        }

        createSqlTable.setColumns(columns);
        // 索引信息
        createSqlTable.setIndexes(columnVisitor.getIndices());
        return createSqlTable;
    }

    /**
     * 单条 insert 语句
     *
     * @param dbType 数据库类型
     * @param sql    sql语句
     * @return 解析结果
     */
    @Override
    public InsertSqlParseResult parseInsertSql(String dbType, String sql) {
        InsertSqlParseResult result = new InsertSqlParseResult();

        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType);
        SQLStatement statement = parser.parseStatement();

        if (!(statement instanceof MySqlInsertStatement insertStatement)) {
            return result;
        }

        // INSERT SQL的表信息
        SQLExprTableSource tableSource = insertStatement.getTableSource();
        SqlTable table = new SqlTable();
        table.setName(tableSource.getTableName());
        table.setCatalog(tableSource.getCatalog());
        table.setSchema(tableSource.getSchema());
        result.setTable(table);

        // INSERT SQL的列信息
        List<InsertColumn> insertColumns = new ArrayList<>();
        List<SQLExpr> columns = insertStatement.getColumns();
        for (SQLExpr column : columns) {
            if (column instanceof SQLIdentifierExpr identifierExpr) {
                InsertColumn insertColumn = new InsertColumn();
                insertColumn.setColumnName(identifierExpr.getName());
                insertColumn.setTableName(table.getName());
                insertColumns.add(insertColumn);
            }
        }
        result.setInsertColumns(insertColumns);

        // 插入语句的值
        List<List<String>> columnValues = new ArrayList<>();
        // 如果是批量插入的insert：insert into tab(id,name) values(1,'a'),(2,'b'),(3,'c');
        List<SQLInsertStatement.ValuesClause> assignValueClauses = insertStatement.getValuesList();
        if (assignValueClauses != null && assignValueClauses.size() > 1) {   // 批量插入
            for (int j = 0; j < assignValueClauses.size(); j++) {
                SQLInsertStatement.ValuesClause valueClause = assignValueClauses.get(j);
                List<SQLExpr> values = valueClause.getValues();
                List<String> columnValuesOfOneRow = new ArrayList<>();
                for (SQLExpr sqlExpression : values) {
                    // 处理不同的数据类型
                    columnValuesOfOneRow.add(sqlExpression.toString());
                }
                columnValues.add(columnValuesOfOneRow);
            }
        } else {
            // 非批量插入
            List<SQLExpr> sqlExpressions = insertStatement.getValues().getValues();
            List<String> columnValuesOfOneRow = new ArrayList<>();
            for (SQLExpr sqlExpression : sqlExpressions) {
                // 处理不同的数据类型
                columnValuesOfOneRow.add(sqlExpression.toString());
            }
            columnValues.add(columnValuesOfOneRow);
        }
        result.setColumnValues(columnValues);

        // 如果是 INTO INTO SELECT 语句，则可以获取 select查询
        if (insertStatement.getQuery() != null) {
            SQLSelect select = insertStatement.getQuery();
        }

        // ON DUPLICATE UPDATE 部分可以使用下面的语句获取
        List<SQLExpr> dku = insertStatement.getDuplicateKeyUpdate();
        if (dku != null && !dku.isEmpty()) {

        }
        return result;
    }

    @Override
    public UpdateSqlParseResult parseUpdateSql(String dbType, String sql) {
        UpdateSqlParseResult result = new UpdateSqlParseResult();
        try {
            SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType);
            SQLStatement stmt = parser.parseStatement();
            MySqlUpdateStatement updateStmt = (MySqlUpdateStatement) stmt;
            SQLTableSource ts = updateStmt.getTableSource();
            // 如果是多表 UPDATE 语句，可以使用下面的语句进行判断
            if (ts != null && ts.toString().contains(",")) {
                throw new UnsupportedOperationException("parsing multiple-table update sql is not supported");
            }

            SqlTable table = new SqlTable();
            table.setName(updateStmt.getTableName().getSimpleName().replace("`", ""));
            result.setTable(table);

            // 获得 UPDATE 语句的 WHERE 部分

            SQLExpr se = updateStmt.getWhere();
            // WHERE 中有子查询： update company set name='com' where id in (select id from xxx where ...)
            if (se instanceof SQLInSubQueryExpr) {

            }
            if (updateStmt.getWhere() != null) {
                result.setWhereCondition(updateStmt.getWhere().toString());
            }

            // 如果where 部分由 select 语句，由：se instanceof SQLInSubQueryExpr 来判断。

            List<SQLUpdateSetItem> items = updateStmt.getItems();
            // update 对应的列和值
            List<UpdateColumn> updateColumns = new ArrayList<>();
            for (int i = 0; i < items.size(); i++) {
                SQLUpdateSetItem item = items.get(i);
                UpdateColumn updateColumn = new UpdateColumn();
                updateColumn.setColumnName(item.getColumn().toString());
                updateColumn.setValue(item.getValue().toString());
                updateColumns.add(updateColumn);
            }
            result.setUpdateColumns(updateColumns);


            // order by 部分
            SQLOrderBy orderBy = updateStmt.getOrderBy();
            // limit 部分
            SQLLimit limit = updateStmt.getLimit();

            // TODO 待补充
            /*
            boolean flag = false;
            if (orderBy != null && orderBy.getItems() != null && !orderBy.getItems().isEmpty()) {
                for (int i = 0; i < orderBy.getItems().size(); i++) {
                    SQLSelectOrderByItem item = orderBy.getItems().get(i);
                    SQLOrderingSpecification os = item.getType();
                }
            }
            if (limit != null) {
                // 分为两种情况： limit 10;   limit 10,10;
            }
             */
        } catch (Exception exception) {

        }
        return result;
    }

    /**
     * 例如 alter table t add colomn name varchar(30)
     *
     * @param dbType 数据库类型
     * @param sql    sql语句
     * @return 解析结果
     */
    @Override
    public AlterSqlParseResult parseAlterSql(String dbType, String sql) {
        AlterSqlParseResult result = new AlterSqlParseResult();
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(sql, dbType);
        SQLStatement statement = parser.parseStatement();
        SQLAlterTableStatement alter = (SQLAlterTableStatement) statement;
        SQLExprTableSource source = alter.getTableSource();
        String tableName = source.toString();
        return result;
    }

    private static class SelectSqlVisitor implements SQLASTVisitor {

        public static final Pattern PARAMETER_PATTERN = Pattern.compile("#\\{[a-zA-z]*}");
        private static final int PARAMETER_START_INDEX = 2;

        private final List<SQLSelectItem> selectItems = new ArrayList<>();

        /**
         * 查询的表
         */
        private final List<SelectTable> selectTables = new ArrayList<>();

        /**
         * 查询的列
         */
        private final List<SelectColumn> selectColumns = new ArrayList<>();

        /**
         * 查询的参数
         */
        private final List<String> parameters = new ArrayList<>();

        /**
         * select 语句
         *
         * @param x SQLSelectQueryBlock
         */
        @Override
        public void endVisit(SQLSelectQueryBlock x) {
            computeSelectColumns();
        }

        @Override
        public boolean visit(SQLExprTableSource x) {
            selectTables.add(new SelectTable(x.getTableName(), x.getAlias()));
            return false;
        }

        @Override
        public boolean visit(SQLCharExpr x) {
            computeParameter(x.toString());
            return false;
        }

        @Override
        public boolean visit(SQLSelectItem x) {
            selectItems.add(x);
            return false;
        }

        @Override
        public boolean visit(SQLVariantRefExpr x) {
            computeParameter(x.getName());
            return false;
        }

        /**
         * 访问查询参数表达式, 匹配查询参数
         *
         * @param expr 查询参数表达式
         */
        protected void computeParameter(String expr) {
            Matcher matcher = PARAMETER_PATTERN.matcher(expr);
            if (matcher.find()) {
                String match = matcher.group();
                parameters.add(match.substring(PARAMETER_START_INDEX, match.length() - 1));
            }
        }

        /**
         * 计算查询列
         */
        protected void computeSelectColumns() {
            selectItems.forEach(item -> {
                String alias = item.getAlias();
                if (item.getExpr() instanceof SQLIdentifierExpr expr) {
                    selectColumns.add(new SelectColumn(selectTables.get(0).getName(), expr.getName(), alias));
                } else if (item.getExpr() instanceof SQLAllColumnExpr expr) {
                    selectColumns.add(new SelectColumn(selectTables.get(0).getName(), expr.toString(), alias));
                } else if (item.getExpr() instanceof SQLMethodInvokeExpr expr) {
                    selectColumns.add(new SelectColumn(null, expr.toString(), alias));
                } else if (item.getExpr() instanceof SQLPropertyExpr expr) {
                    selectColumns.add(new SelectColumn(getSelectTableNameByAlias(expr.getOwnerName()), expr.getName(), item.getAlias()));
                }
            });
        }

        /**
         * 根据查询表别名获取查询表名
         * getSelectTableNameByAlias("t") -> "t_user" or null
         *
         * @param alias 查询表别名
         * @return 查询表名
         */
        protected String getSelectTableNameByAlias(String alias) {
            return getSelectTableByAlias(alias).map(SelectTable::getName).orElse(null);
        }

        /**
         * 根据查询表别名获取查询表
         *
         * @param alias 查询表别名
         * @return 查询表
         */
        protected Optional<SelectTable> getSelectTableByAlias(String alias) {
            return selectTables.stream().filter(table -> alias.equals(table.getAlias())).findFirst();
        }

        public SelectSqlParseResult getResult() {
            SelectSqlParseResult result = new SelectSqlParseResult();
            result.setSelectColumns(this.selectColumns);
            result.setParameters(this.parameters);
            return result;
        }
    }

    private static class MySQLColumnVisitor extends MySqlSchemaStatVisitor {

        /**
         * 保存索引信息
         */
        private final List<IndexInfo> indices = new ArrayList<>();

        @Override
        public boolean visit(SQLColumnDefinition x) {
            String tableName = null;
            SQLObject parent = x.getParent();
            if (parent instanceof SQLCreateTableStatement) {
                tableName = SQLUtils.normalize(((SQLCreateTableStatement) parent).getTableName());
            }
            if (Objects.isNull(tableName)) {
                return true;
            }
            String columnName = SQLUtils.normalize(x.getName().toString());
            TableStat.Column column = this.addColumn(tableName, columnName);
            column.setDataType(x.getDataType().getName());
            Map<String, Object> attr = column.getAttributes();
            if (Objects.isNull(attr)) {
                attr = new HashMap<>();
                column.setAttributes(attr);
            }
            // 其他属性
            // attr.put("sqlSegment", x.toString());
            if (Objects.nonNull(x.getComment())) {
                attr.put("comment", SQLUtils.normalize(x.getComment().toString()));
            }
            attr.put("unsigned", ((SQLDataTypeImpl) x.getDataType()).isUnsigned());
            if (Objects.nonNull(x.getDefaultExpr())) {
                attr.put("defaultValue", SQLUtils.normalize(x.getDefaultExpr().toString()));
            }
            List<Object> typeArgs = new ArrayList<>();
            attr.put("typeArgs", typeArgs);
            for (SQLExpr argument : x.getDataType().getArguments()) {
                if (argument instanceof SQLIntegerExpr) {
                    Number number = ((SQLIntegerExpr) argument).getNumber();
                    typeArgs.add(number);
                }
            }
            for (Object item : x.getConstraints()) {
                if (item instanceof SQLPrimaryKey) {
                    column.setPrimaryKey(true);
                } else if (item instanceof SQLUnique) {
                    column.setUnique(true);
                } else if (item instanceof SQLNotNullConstraint) {
                    attr.put("notnull", true);
                } else if (item instanceof SQLNullConstraint) {
                    attr.put("notnull", false);
                }
            }
            return false;
        }

        @Override
        public boolean visit(MySqlKey x) {
            addIndex(x);
            return false;
        }

        @Override
        public boolean visit(MySqlUnique x) {
            addIndex(x);
            return false;
        }

        @Override
        public boolean visit(MySqlPrimaryKey x) {
            addIndex(x);
            return false;
        }


        private void addIndex(MySqlKey x) {
            SQLIndexDefinition indexDefinition = x.getIndexDefinition();
            List<String> indexColumns = indexDefinition.getColumns().stream().map(v -> SQLUtils.normalize(v.getExpr().toString())).collect(Collectors.toList());
            IndexInfo index = new IndexInfo(
                getOrDefault(indexDefinition.getName(), "")
                , getOrDefault(indexDefinition.getType(), "")
                , getOrDefault(indexDefinition.getOptions().getComment(), "")
                , indexColumns);
            this.indices.add(index);
        }

        private String getOrDefault(Object obj, String defaultValue) {
            return Objects.isNull(obj) ? defaultValue : SQLUtils.normalize(String.valueOf(obj));
        }

        /**
         * 获取索引信息
         *
         * @return 索引信息
         */
        public List<IndexInfo> getIndices() {
            return new ArrayList<>(indices);
        }
    }
}
