package org.assistant.tools.driver;

import org.assistant.tools.ToolProvider;
import org.assistant.ui.controls.Button;
import org.assistant.ui.controls.Label;
import org.assistant.ui.pane.BorderPane;
import org.assistant.ui.pane.HBox;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Tool pane for managing JDBC driver JARs at runtime.
 * Registered as a tab via ToolProvider auto-discovery.
 */
public class JdbcDriverManagerPane extends BorderPane implements ToolProvider {

    private final DriverTableModel tableModel = new DriverTableModel();
    private final JTable table = new JTable(tableModel);
    private final JdbcDriverStore store = JdbcDriverStore.getInstance();
    private final JdbcDriverLoader loader = JdbcDriverLoader.getInstance();

    public JdbcDriverManagerPane() {
        // ---- Toolbar ----
        HBox toolbar = new HBox();
        toolbar.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        Button addBtn = new Button("Add Driver");
        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete");
        Button loadBtn = new Button("Load");
        Button unloadBtn = new Button("Unload");
        Button reloadBtn = new Button("Reload");

        toolbar.add(addBtn);
        toolbar.addSpacing(4);
        toolbar.add(editBtn);
        toolbar.addSpacing(4);
        toolbar.add(deleteBtn);
        toolbar.addSpacing(12);
        toolbar.add(loadBtn);
        toolbar.addSpacing(4);
        toolbar.add(unloadBtn);
        toolbar.addSpacing(4);
        toolbar.add(reloadBtn);
        toolbar.add(Box.createHorizontalGlue());
        setTop(toolbar);

        // ---- Table ----
        table.setRowHeight(26);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(DriverTableModel.COL_STATUS).setCellRenderer(new StatusCellRenderer());
        int[] widths = { 140, 220, 320, 80 };
        for (int i = 0; i < widths.length; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2)
                    doEdit();
            }
        });
        setCenter(new JScrollPane(table));

        // ---- Hint bar ----
        HBox hint = new HBox();
        hint.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        hint.add(new Label("Loaded drivers are shared across all datasource connections in this session."));
        setBottom(hint);

        // ---- Actions ----
        addBtn.addActionListener(e -> doAdd());
        editBtn.addActionListener(e -> doEdit());
        deleteBtn.addActionListener(e -> doDelete());
        loadBtn.addActionListener(e -> doLoad());
        unloadBtn.addActionListener(e -> doUnload());
        reloadBtn.addActionListener(e -> doReload());

        // ---- Load data & auto-load registered drivers ----
        loadData();
    }

    private void loadData() {
        List<JdbcDriverInfo> drivers = store.findAll();
        // Auto-load all persisted drivers in background on first startup
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                loader.loadAll(drivers);
                return null;
            }

            @Override
            protected void done() {
                tableModel.setData(drivers);
            }
        };
        worker.execute();
    }

    private void doAdd() {
        JdbcDriverInfo info = showDialog(null);
        if (info == null)
            return;
        try {
            store.save(info);
            tableModel.addRow(info);
            if (JOptionPane.showConfirmDialog(this,
                    "Driver saved. Load it now?", "Load Driver",
                    JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                doLoadInfo(tableModel.getRowCount() - 1, info);
            }
        } catch (Exception ex) {
            showError("Failed to save: " + ex.getMessage());
        }
    }

    private void doEdit() {
        int row = selectedModelRow();
        if (row < 0) {
            showWarn("Select a driver to edit.");
            return;
        }
        JdbcDriverInfo existing = tableModel.getRow(row);
        JdbcDriverInfo updated = showDialog(existing);
        if (updated == null)
            return;
        updated.setId(existing.getId());
        try {
            store.update(updated);
            tableModel.updateRow(row, updated);
        } catch (Exception ex) {
            showError("Update failed: " + ex.getMessage());
        }
    }

    private void doDelete() {
        int row = selectedModelRow();
        if (row < 0) {
            showWarn("Select a driver to delete.");
            return;
        }
        JdbcDriverInfo info = tableModel.getRow(row);
        if (JOptionPane.showConfirmDialog(this,
                "Delete \"" + info.getName() + "\"?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION)
            return;
        // unload first if loaded
        if (info.isLoaded())
            loader.unload(info);
        try {
            store.delete(info.getId());
            tableModel.removeRow(row);
        } catch (Exception ex) {
            showError("Delete failed: " + ex.getMessage());
        }
    }

    private void doLoad() {
        int row = selectedModelRow();
        if (row < 0) {
            showWarn("Select a driver to load.");
            return;
        }
        doLoadInfo(row, tableModel.getRow(row));
    }

    private void doLoadInfo(int modelRow, JdbcDriverInfo info) {
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private String error;

            @Override
            protected Void doInBackground() {
                try {
                    loader.load(info);
                } catch (Exception e) {
                    error = e.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                tableModel.fireTableRowsUpdated(modelRow, modelRow);
                if (error != null)
                    showError("Load failed: " + error);
                else
                    JOptionPane.showMessageDialog(JdbcDriverManagerPane.this,
                            "Driver loaded: " + info.getName(), "Success",
                            JOptionPane.INFORMATION_MESSAGE);
            }
        };
        worker.execute();
    }

    private void doUnload() {
        int row = selectedModelRow();
        if (row < 0) {
            showWarn("Select a driver to unload.");
            return;
        }
        JdbcDriverInfo info = tableModel.getRow(row);
        loader.unload(info);
        tableModel.fireTableRowsUpdated(row, row);
        JOptionPane.showMessageDialog(this, "Driver unloaded: " + info.getName(),
                "Unloaded", JOptionPane.INFORMATION_MESSAGE);
    }

    private void doReload() {
        int row = selectedModelRow();
        if (row < 0) {
            showWarn("Select a driver to reload.");
            return;
        }
        JdbcDriverInfo info = tableModel.getRow(row);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private String error;

            @Override
            protected Void doInBackground() {
                try {
                    loader.reload(info);
                } catch (Exception e) {
                    error = e.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                tableModel.fireTableRowsUpdated(row, row);
                if (error != null)
                    showError("Reload failed: " + error);
                else
                    JOptionPane.showMessageDialog(JdbcDriverManagerPane.this,
                            "Driver reloaded: " + info.getName(), "Success",
                            JOptionPane.INFORMATION_MESSAGE);
            }
        };
        worker.execute();
    }

    /**
     * Shows the Add/Edit dialog. Returns null if cancelled.
     */
    private JdbcDriverInfo showDialog(JdbcDriverInfo existing) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(4, 4, 4, 8);
        GridBagConstraints fc = new GridBagConstraints();
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0;
        fc.insets = new Insets(4, 0, 4, 4);
        fc.gridwidth = GridBagConstraints.REMAINDER;

        JTextField nameField = new JTextField(existing == null ? "" : existing.getName(), 30);
        JTextField classField = new JTextField(existing == null ? "" : existing.getDriverClass(), 30);
        JTextField jarField = new JTextField(existing == null ? "" : existing.getJarPath(), 30);
        Button browseBtn = new Button("Browse…");

        // JAR path row with browse button
        JPanel jarRow = new JPanel(new BorderLayout(4, 0));
        jarRow.add(jarField, BorderLayout.CENTER);
        jarRow.add(browseBtn, BorderLayout.EAST);

        int row = 0;
        lc.gridy = row;
        fc.gridy = row;
        lc.gridx = 0;
        fc.gridx = 1;
        form.add(new JLabel("Name *:"), lc);
        form.add(nameField, fc);
        row++;
        lc.gridy = row;
        fc.gridy = row;
        lc.gridx = 0;
        fc.gridx = 1;
        form.add(new JLabel("Driver Class *:"), lc);
        form.add(classField, fc);
        row++;
        lc.gridy = row;
        fc.gridy = row;
        lc.gridx = 0;
        fc.gridx = 1;
        form.add(new JLabel("JAR Path *:"), lc);
        form.add(jarRow, fc);

        browseBtn.addActionListener(e -> {
            JFileChooser fc2 = new JFileChooser();
            fc2.setDialogTitle("Select JDBC Driver JAR");
            fc2.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("JAR files", "jar"));
            if (jarField.getText() != null && !jarField.getText().isBlank()) {
                fc2.setCurrentDirectory(new File(jarField.getText()).getParentFile());
            }
            if (fc2.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                jarField.setText(fc2.getSelectedFile().getAbsolutePath());
                // auto-guess driver class from common JARs
                String fileName = fc2.getSelectedFile().getName().toLowerCase();
                if (classField.getText().isBlank()) {
                    classField.setText(guessDriverClass(fileName));
                }
            }
        });

        int result = JOptionPane.showConfirmDialog(this, form,
                existing == null ? "Add JDBC Driver" : "Edit JDBC Driver",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION)
            return null;

        String name = nameField.getText().trim();
        String clazz = classField.getText().trim();
        String jar = jarField.getText().trim();
        if (name.isEmpty() || clazz.isEmpty() || jar.isEmpty()) {
            showWarn("Name, Driver Class and JAR Path are required.");
            return null;
        }
        JdbcDriverInfo info = new JdbcDriverInfo(name, clazz, jar);
        if (existing != null)
            info.setId(existing.getId());
        return info;
    }

    private String guessDriverClass(String jarName) {
        if (jarName.contains("mysql"))
            return "com.mysql.cj.jdbc.Driver";
        if (jarName.contains("postgresql"))
            return "org.postgresql.Driver";
        if (jarName.contains("ojdbc"))
            return "oracle.jdbc.OracleDriver";
        if (jarName.contains("mssql") || jarName.contains("sqlserver"))
            return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        if (jarName.contains("h2"))
            return "org.h2.Driver";
        if (jarName.contains("sqlite"))
            return "org.sqlite.JDBC";
        return "";
    }

    private int selectedModelRow() {
        int view = table.getSelectedRow();
        return view < 0 ? -1 : table.convertRowIndexToModel(view);
    }

    private void showWarn(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ---- ToolProvider ----

    @Override
    public String getLabel() {
        return "JDBC Drivers";
    }

    @Override
    public JComponent getView() {
        return this;
    }

    @Override
    public int getOrder() {
        return -200;
    }

    // ---- Table model ----

    private static class DriverTableModel extends AbstractTableModel {
        static final int COL_NAME = 0;
        static final int COL_CLASS = 1;
        static final int COL_JAR = 2;
        static final int COL_STATUS = 3;

        private static final String[] COLS = { "Name", "Driver Class", "JAR Path", "Status" };
        private final List<JdbcDriverInfo> rows = new ArrayList<>();

        void setData(List<JdbcDriverInfo> data) {
            rows.clear();
            rows.addAll(data);
            fireTableDataChanged();
        }

        void addRow(JdbcDriverInfo d) {
            rows.add(d);
            fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
        }

        void updateRow(int i, JdbcDriverInfo d) {
            rows.set(i, d);
            fireTableRowsUpdated(i, i);
        }

        void removeRow(int i) {
            rows.remove(i);
            fireTableRowsDeleted(i, i);
        }

        JdbcDriverInfo getRow(int i) {
            return rows.get(i);
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return COLS.length;
        }

        @Override
        public String getColumnName(int c) {
            return COLS[c];
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }

        @Override
        public Object getValueAt(int r, int c) {
            JdbcDriverInfo d = rows.get(r);
            return switch (c) {
                case COL_NAME -> d.getName();
                case COL_CLASS -> d.getDriverClass();
                case COL_JAR -> d.getJarPath();
                case COL_STATUS -> d.isLoaded() ? "✓ Loaded" : "○ Not Loaded";
                default -> null;
            };
        }
    }

    // ---- Status cell renderer ----

    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object val,
                boolean sel, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, val, sel, focus, row, col);
            String text = val == null ? "" : val.toString();
            if (text.startsWith("✓")) {
                setForeground(sel ? Color.WHITE : new Color(0, 140, 0));
            } else {
                setForeground(sel ? Color.WHITE : UIManager.getColor("Table.foreground"));
            }
            return this;
        }
    }
}
