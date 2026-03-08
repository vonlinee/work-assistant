package org.assistant.tools.datasource;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Table model for the datasource configuration list.
 */
public class DataSourceConfigTableModel extends AbstractTableModel {

    public static final int COL_NAME = 0;
    public static final int COL_TYPE = 1;
    public static final int COL_HOST = 2;
    public static final int COL_PORT = 3;
    public static final int COL_DATABASE = 4;
    public static final int COL_USERNAME = 5;
    public static final int COL_REMARK = 6;
    public static final int COL_STATUS = 7;

    private static final String[] COLUMNS = {
            "Name", "Type", "Host", "Port", "Database", "Username", "Remark", "Status"
    };

    private final List<DataSourceConfig> rows = new ArrayList<>();

    public void setData(List<DataSourceConfig> data) {
        rows.clear();
        rows.addAll(data);
        fireTableDataChanged();
    }

    public void addRow(DataSourceConfig config) {
        rows.add(config);
        fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
    }

    public void updateRow(int index, DataSourceConfig config) {
        rows.set(index, config);
        fireTableRowsUpdated(index, index);
    }

    public void removeRow(int index) {
        rows.remove(index);
        fireTableRowsDeleted(index, index);
    }

    public DataSourceConfig getRow(int index) {
        return rows.get(index);
    }

    public List<DataSourceConfig> getAll() {
        return new ArrayList<>(rows);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        DataSourceConfig c = rows.get(rowIndex);
        return switch (columnIndex) {
            case COL_NAME -> c.getName();
            case COL_TYPE -> c.getType();
            case COL_HOST -> c.getHost();
            case COL_PORT -> c.getPort();
            case COL_DATABASE -> c.getDatabase();
            case COL_USERNAME -> c.getUsername();
            case COL_REMARK -> c.getRemark();
            case COL_STATUS -> c.getStatus() != null ? c.getStatus() : "";
            default -> null;
        };
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
