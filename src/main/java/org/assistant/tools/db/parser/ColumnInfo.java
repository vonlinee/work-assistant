package org.assistant.tools.db.parser;

import lombok.Data;

@Data
public class ColumnInfo {
    private String name;
    private int dataType; // java.sql.Types
    private String typeName; // DB specific type name (e.g., VARCHAR, INT)
    private int size;
    private int decimalDigits;
    private boolean nullable;
    private boolean primaryKey;
    private boolean autoIncrement;
    private String defaultValue;
    private String remarks; // Column comment
}
