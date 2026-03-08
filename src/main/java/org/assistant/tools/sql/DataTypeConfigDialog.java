package org.assistant.tools.sql;

import com.alibaba.druid.DbType;
import org.assistant.ui.controls.DbDialectComboBox;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DataTypeConfigDialog extends JDialog {

    private final DbDialectComboBox dialectBox;
    private final DefaultTableModel tableModel;
    private final JTable typeTable;
    private boolean typesModified = false;

    public DataTypeConfigDialog(Frame owner) {
        super(owner, "Configure Dialect Data Types", true);
        setSize(400, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // TOP: Dialect Selector
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        topPanel.add(new JLabel("Dialect:"));
        dialectBox = new DbDialectComboBox();
        topPanel.add(dialectBox);
        add(topPanel, BorderLayout.NORTH);

        // CENTER: Editable JTable containing Data Types
        tableModel = new DefaultTableModel(new Object[] { "Data Type constraints" }, 0);
        typeTable = new JTable(tableModel);
        typeTable.setRowHeight(24);
        typeTable.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        JScrollPane scrollPane = new JScrollPane(typeTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Mapped Types"));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // BOTTOM/RIGHT: Action Controls
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAdd = new JButton("Add Type");
        JButton btnDelete = new JButton("Delete Selected");
        JButton btnReset = new JButton("Reset to Defaults");
        JButton btnSave = new JButton("Save & Close");
        JButton btnCancel = new JButton("Cancel");

        actionPanel.add(btnAdd);
        actionPanel.add(btnDelete);
        actionPanel.add(btnReset);
        actionPanel.add(btnSave);
        actionPanel.add(btnCancel);
        add(actionPanel, BorderLayout.SOUTH);

        // Action Listeners
        dialectBox.addActionListener(e -> loadTypesForDialect());

        btnAdd.addActionListener(e -> {
            stopTableEditing();
            tableModel.addRow(new Object[] { "NEW_TYPE" });
            int lastRow = tableModel.getRowCount() - 1;
            typeTable.setRowSelectionInterval(lastRow, lastRow);
            typeTable.editCellAt(lastRow, 0);
            typeTable.requestFocus();
        });

        btnDelete.addActionListener(e -> {
            stopTableEditing();
            int[] selectedRows = typeTable.getSelectedRows();
            for (int i = selectedRows.length - 1; i >= 0; i--) {
                tableModel.removeRow(selectedRows[i]);
            }
        });

        btnReset.addActionListener(e -> {
            stopTableEditing();
            DbType selectedDialect = (DbType) dialectBox.getSelectedItem();
            DataTypeManager.resetToDefaults(selectedDialect);
            loadTypesForDialect();
        });

        btnSave.addActionListener(e -> {
            stopTableEditing();
            saveActiveTypes();
            typesModified = true;
            dispose();
        });

        btnCancel.addActionListener(e -> dispose());

        // Initialize First load
        loadTypesForDialect();
    }

    private void stopTableEditing() {
        if (typeTable.isEditing()) {
            typeTable.getCellEditor().stopCellEditing();
        }
    }

    private void loadTypesForDialect() {
        DbType selectedDialect = (DbType) dialectBox.getSelectedItem();
        String[] types = DataTypeManager.getTypesForDialect(selectedDialect);
        tableModel.setRowCount(0);
        for (String type : types) {
            tableModel.addRow(new Object[] { type });
        }
    }

    private void saveActiveTypes() {
        DbType selectedDialect = (DbType) dialectBox.getSelectedItem();
        List<String> activeTypes = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object val = tableModel.getValueAt(i, 0);
            if (val != null) {
                String strVal = val.toString().trim();
                // Ensure array doesn't save empty artifact blanks
                if (!strVal.isEmpty()) {
                    activeTypes.add(strVal.toUpperCase());
                }
            }
        }
        DataTypeManager.saveTypesForDialect(selectedDialect, activeTypes);
    }

    /**
     * @return true if the user hit Save & Close
     */
    public boolean showDialog() {
        setVisible(true);
        return typesModified;
    }
}
