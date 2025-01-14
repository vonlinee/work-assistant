package org.example.workassistant.common.interfaces.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * 仅包含表的元数据信息，且所有字段和DatabaseMetaData#getTables返回值一致
 * <a href="https://docs.oracle.com/javase/8/docs/technotes/guides/jdbc/">JDBC specification</a>
 *
 * @see ColumnMetadata
 * @see java.sql.DatabaseMetaData#getTables(String, String, String, String[])
 */
public class TableMetadata implements JdbcMetadataObject {

    /**
     * TABLE_CAT String => table catalog (may be null)
     */
    private String tableCatalog;

    /**
     * TABLE_SCHEM String => table schema (maybe null)
     */
    private String tableSchema;

    /**
     * TABLE_NAME String => table name
     */
    private String tableName;

    /**
     * TABLE_TYPE String => table type. Typical types are
     * "TABLE", "VIEW", "SYSTEM TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY", "ALIAS", "SYNONYM".
     */
    private String tableType;

    /**
     * String => explanatory comment on the table (maybe null)
     */
    private String remarks;

    /**
     * String => the types catalog (maybe null)
     */
    private String typeCatalog;

    /**
     * String => the types schema (maybe null)
     */
    private String typeSchema;

    /**
     * TYPE_NAME String => type name (maybe null)
     */
    private String typeName;

    /**
     * SELF_REFERENCING_COL_NAME String => name of the designated "identifier" column of a typed table (maybe null)
     */
    private String selfReferencingColumnName;

    /**
     * REF_GENERATION String => specifies how values in SELF_REFERENCING_COL_NAME are created. Values are "SYSTEM", "USER", "DERIVED". (maybe null)
     */
    private String refGeneration;

    /**
     * 携带额外的数据
     */
    private Map<String, Object> attributes;

    @Override
    public void initialize(ResultSet resultSet) throws SQLException {
        this.tableCatalog = resultSet.getString("TABLE_CAT");
        this.tableSchema = resultSet.getString("TABLE_SCHEM");
        this.tableName = resultSet.getString("TABLE_NAME");
        this.tableType = resultSet.getString("TABLE_TYPE");
        this.remarks = resultSet.getString("REMARKS");
        this.typeCatalog = resultSet.getString("TYPE_CAT");
        this.tableSchema = resultSet.getString("TYPE_SCHEM");
        this.typeName = resultSet.getString("TYPE_NAME");
        this.selfReferencingColumnName = resultSet.getString("SELF_REFERENCING_COL_NAME");
        this.refGeneration = resultSet.getString("REF_GENERATION");
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

    public String getTableType() {
        return tableType;
    }

    public void setTableType(String tableType) {
        this.tableType = tableType;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getTypeCatalog() {
        return typeCatalog;
    }

    public void setTypeCatalog(String typeCatalog) {
        this.typeCatalog = typeCatalog;
    }

    public String getTypeSchema() {
        return typeSchema;
    }

    public void setTypeSchema(String typeSchema) {
        this.typeSchema = typeSchema;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getSelfReferencingColumnName() {
        return selfReferencingColumnName;
    }

    public void setSelfReferencingColumnName(String selfReferencingColumnName) {
        this.selfReferencingColumnName = selfReferencingColumnName;
    }

    public String getRefGeneration() {
        return refGeneration;
    }

    public void setRefGeneration(String refGeneration) {
        this.refGeneration = refGeneration;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}
