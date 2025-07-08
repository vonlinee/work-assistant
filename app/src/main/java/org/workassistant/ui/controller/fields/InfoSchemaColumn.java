package org.workassistant.ui.controller.fields;

/**
 * CREATE TEMPORARY TABLE `COLUMNS` (
 * `TABLE_CATALOG` varchar(512) NOT NULL DEFAULT '',
 * `TABLE_SCHEMA` varchar(64) NOT NULL DEFAULT '',
 * `TABLE_NAME` varchar(64) NOT NULL DEFAULT '',
 * `COLUMN_NAME` varchar(64) NOT NULL DEFAULT '',
 * `ORDINAL_POSITION` bigint(21) unsigned NOT NULL DEFAULT '0',
 * `COLUMN_DEFAULT` longtext,
 * `IS_NULLABLE` varchar(3) NOT NULL DEFAULT '',
 * `DATA_TYPE` varchar(64) NOT NULL DEFAULT '',
 * `CHARACTER_MAXIMUM_LENGTH` bigint(21) unsigned DEFAULT NULL,
 * `CHARACTER_OCTET_LENGTH` bigint(21) unsigned DEFAULT NULL,
 * `NUMERIC_PRECISION` bigint(21) unsigned DEFAULT NULL,
 * `NUMERIC_SCALE` bigint(21) unsigned DEFAULT NULL,
 * `DATETIME_PRECISION` bigint(21) unsigned DEFAULT NULL,
 * `CHARACTER_SET_NAME` varchar(32) DEFAULT NULL,
 * `COLLATION_NAME` varchar(32) DEFAULT NULL,
 * `COLUMN_TYPE` longtext NOT NULL,
 * `COLUMN_KEY` varchar(3) NOT NULL DEFAULT '',
 * `EXTRA` varchar(30) NOT NULL DEFAULT '',
 * `PRIVILEGES` varchar(80) NOT NULL DEFAULT '',
 * `COLUMN_COMMENT` varchar(1024) NOT NULL DEFAULT '',
 * `GENERATION_EXPRESSION` longtext NOT NULL
 * ) ENGINE=InnoDB DEFAULT CHARSET=utf8
 */
public class InfoSchemaColumn {

    private String tableCatalog;
    private String tableSchema;
    private String tableName;
    private String columnName;
    private Long ordinalPosition;
    private String columnDefault;
    private String isNullable;
    private String dataType;
    private Long characterMaximumLength;
    private Long characterOctetLength;
    private Long numericPrecision;
    private Long numericScale;
    private Long datetimePrecision;
    private String characterSetName;
    private String collationName;
    private String columnType;
    private String columnKey;
    private String extra;
    private String privileges;
    private String columnComment;
    private String generationExpression;

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

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Long getOrdinalPosition() {
        return ordinalPosition;
    }

    public void setOrdinalPosition(Long ordinalPosition) {
        this.ordinalPosition = ordinalPosition;
    }

    public String getColumnDefault() {
        return columnDefault;
    }

    public void setColumnDefault(String columnDefault) {
        this.columnDefault = columnDefault;
    }

    public String getIsNullable() {
        return isNullable;
    }

    public void setIsNullable(String isNullable) {
        this.isNullable = isNullable;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Long getCharacterMaximumLength() {
        return characterMaximumLength;
    }

    public void setCharacterMaximumLength(Long characterMaximumLength) {
        this.characterMaximumLength = characterMaximumLength;
    }

    public Long getCharacterOctetLength() {
        return characterOctetLength;
    }

    public void setCharacterOctetLength(Long characterOctetLength) {
        this.characterOctetLength = characterOctetLength;
    }

    public Long getNumericPrecision() {
        return numericPrecision;
    }

    public void setNumericPrecision(Long numericPrecision) {
        this.numericPrecision = numericPrecision;
    }

    public Long getNumericScale() {
        return numericScale;
    }

    public void setNumericScale(Long numericScale) {
        this.numericScale = numericScale;
    }

    public Long getDatetimePrecision() {
        return datetimePrecision;
    }

    public void setDatetimePrecision(Long datetimePrecision) {
        this.datetimePrecision = datetimePrecision;
    }

    public String getCharacterSetName() {
        return characterSetName;
    }

    public void setCharacterSetName(String characterSetName) {
        this.characterSetName = characterSetName;
    }

    public String getCollationName() {
        return collationName;
    }

    public void setCollationName(String collationName) {
        this.collationName = collationName;
    }

    public String getColumnType() {
        return columnType;
    }

    public void setColumnType(String columnType) {
        this.columnType = columnType;
    }

    public String getColumnKey() {
        return columnKey;
    }

    public void setColumnKey(String columnKey) {
        this.columnKey = columnKey;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public String getPrivileges() {
        return privileges;
    }

    public void setPrivileges(String privileges) {
        this.privileges = privileges;
    }

    public String getColumnComment() {
        return columnComment;
    }

    public void setColumnComment(String columnComment) {
        this.columnComment = columnComment;
    }

    public String getGenerationExpression() {
        return generationExpression;
    }

    public void setGenerationExpression(String generationExpression) {
        this.generationExpression = generationExpression;
    }
}
