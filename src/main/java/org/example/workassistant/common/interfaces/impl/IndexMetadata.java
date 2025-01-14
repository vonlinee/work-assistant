package org.example.workassistant.common.interfaces.impl;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * JDBC index metadata
 *
 * @see java.sql.DatabaseMetaData#getIndexInfo(String, String, String, boolean, boolean)
 */
public class IndexMetadata implements JdbcMetadataObject {

    /**
     * TABLE_CAT String => table catalog (may be null)
     **/
    private String tableCatalog;
    /**
     * TABLE_SCHEM String => table schema (maybe null)
     **/
    private String tableSchema;
    /**
     * TABLE_NAME String => table name
     **/
    private String tableName;
    /**
     * NON_UNIQUE boolean => Can index values be non-unique. false when TYPE is tableIndexStatistic
     *
     * @see IndexMetadata#type
     **/
    private boolean nonUnique;
    /**
     * INDEX_QUALIFIER String => index catalog (may be null); null when TYPE is tableIndexStatistic
     **/
    private String indexQualifier;
    /**
     * INDEX_NAME String => index name; null when TYPE is tableIndexStatistic
     **/
    private String indexName;
    /**
     * TYPE short => index type:
     * <li>tableIndexStatistic - this identifies table statistics that are returned in conjunction with a table's index descriptions</li>
     * <li>tableIndexClustered - this is a clustered index</li>
     * <li>tableIndexHashed - this is a hashed index</li>
     * <li>tableIndexOther - this is some other style of index</li>
     **/
    private short type;
    /**
     * ORDINAL_POSITION short => column sequence number within index; zero when TYPE is tableIndexStatistic
     **/
    private short ordinalPosition;
    /**
     * COLUMN_NAME String => column name; null when TYPE is tableIndexStatistic
     **/
    private String columnName;
    /**
     * ASC_OR_DESC String => column sort sequence, "A" => ascending, "D" => descending, may be null if sort sequence is not supported; null when TYPE is tableIndexStatistic
     *
     * @see IndexMetadata#type
     **/
    private String ascOrDesc;
    /**
     * CARDINALITY long => When TYPE is tableIndexStatistic, then this is the number of rows in the table; otherwise, it is the number of unique values in the index.
     *
     * @see IndexMetadata#type
     **/
    private long cardinality;
    /**
     * PAGES long => When TYPE is tableIndexStatistic then this is the number of pages used for the table, otherwise it is the number of pages used for the current index.
     *
     * @see IndexMetadata#type
     **/
    private long pages;
    /**
     * FILTER_CONDITION String => Filter condition, if any. (maybe null)
     **/
    private String filterCondition;

    @Override
    public void initialize(ResultSet resultSet) throws SQLException {
        this.tableCatalog = resultSet.getString(1);
        this.tableSchema = resultSet.getString(2);
        this.tableName = resultSet.getString(3);
        this.nonUnique = resultSet.getBoolean(4);
        this.indexQualifier = resultSet.getString(5);
        this.indexName = resultSet.getString(6);
        this.type = resultSet.getShort(7);
        this.ordinalPosition = resultSet.getShort(8);
        this.columnName = resultSet.getString(9);
        this.ascOrDesc = resultSet.getString(10);
        this.cardinality = resultSet.getShort(11);
        this.pages = resultSet.getShort(12);
        this.filterCondition = resultSet.getString(13);
    }

    public String getTableCatalog() {
        return tableCatalog;
    }

    public void setTableCatalog(String tableCatalog) {
        this.tableCatalog = tableCatalog;
    }

    public String getTableSchema() {
        return tableSchema;
    }

    public void setTableSchema(String tableSchema) {
        this.tableSchema = tableSchema;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public boolean isNonUnique() {
        return nonUnique;
    }

    public void setNonUnique(boolean nonUnique) {
        this.nonUnique = nonUnique;
    }

    public String getIndexQualifier() {
        return indexQualifier;
    }

    public void setIndexQualifier(String indexQualifier) {
        this.indexQualifier = indexQualifier;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
    }

    public short getOrdinalPosition() {
        return ordinalPosition;
    }

    public void setOrdinalPosition(short ordinalPosition) {
        this.ordinalPosition = ordinalPosition;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getAscOrDesc() {
        return ascOrDesc;
    }

    public void setAscOrDesc(String ascOrDesc) {
        this.ascOrDesc = ascOrDesc;
    }

    public long getCardinality() {
        return cardinality;
    }

    public void setCardinality(long cardinality) {
        this.cardinality = cardinality;
    }

    public long getPages() {
        return pages;
    }

    public void setPages(long pages) {
        this.pages = pages;
    }

    public String getFilterCondition() {
        return filterCondition;
    }

    public void setFilterCondition(String filterCondition) {
        this.filterCondition = filterCondition;
    }
}
