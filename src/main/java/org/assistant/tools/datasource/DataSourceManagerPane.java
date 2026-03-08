package org.assistant.tools.datasource;

import org.assistant.tools.ToolProvider;
import org.assistant.ui.controls.Button;
import org.assistant.ui.controls.Label;
import org.assistant.ui.pane.BorderPane;
import org.assistant.ui.pane.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

/**
 * Main tool panel for datasource connection info management.
 * Registered as a tab via ToolProvider auto-discovery.
 */
public class DataSourceManagerPane extends BorderPane implements ToolProvider {

    private static final Logger log = LoggerFactory.getLogger(DataSourceManagerPane.class);

    private final DataSourceConfigTableModel tableModel = new DataSourceConfigTableModel();
    private final JTable table = new JTable(tableModel);
    private final DataSourceConfigStore store = DataSourceConfigStore.getInstance();
    private final DataSourceExporter exporter = new DataSourceExporter();

    public DataSourceManagerPane() {
        // ---- Toolbar (top) ----
        HBox toolbar = new HBox();
        toolbar.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        Button addBtn = new Button("Add");
        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete");
        Button testBtn = new Button("Test Connection");
        Button exportMdBtn = new Button("Export Markdown");
        Button exportXlsBtn = new Button("Export Excel");

        toolbar.add(addBtn);
        toolbar.addSpacing(4);
        toolbar.add(editBtn);
        toolbar.addSpacing(4);
        toolbar.add(deleteBtn);
        toolbar.addSpacing(12);
        toolbar.add(testBtn);
        toolbar.addSpacing(12);
        toolbar.add(exportMdBtn);
        toolbar.addSpacing(4);
        toolbar.add(exportXlsBtn);
        toolbar.add(Box.createHorizontalGlue());

        setTop(toolbar);

        // ---- Table (center) ----
        table.setRowHeight(26);
        table.setAutoCreateRowSorter(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(DataSourceConfigTableModel.COL_STATUS)
                .setCellRenderer(new StatusCellRenderer());

        // column widths
        int[] widths = { 120, 80, 130, 60, 100, 90, 140, 80 };
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // double-click to edit
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    doEdit();
                }
            }
        });

        setCenter(new JScrollPane(table));

        // ---- Status bar (bottom) ----
        HBox statusBar = new HBox();
        statusBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        Label hint = new Label("Double-click a row to edit. Select a row to test/export.");
        statusBar.add(hint);
        setBottom(statusBar);

        // ---- Actions ----
        addBtn.addActionListener(e -> doAdd());
        editBtn.addActionListener(e -> doEdit());
        deleteBtn.addActionListener(e -> doDelete());
        testBtn.addActionListener(e -> doTestConnection());
        exportMdBtn.addActionListener(e -> doExport("md"));
        exportXlsBtn.addActionListener(e -> doExport("xlsx"));

        // ---- Load data ----
        loadData();
    }

    private void loadData() {
        List<DataSourceConfig> data = store.findAll();
        tableModel.setData(data);
    }

    private void doAdd() {
        DataSourceConfigDialog dialog = new DataSourceConfigDialog(getParentFrame(), null);
        dialog.setVisible(true);
        DataSourceConfig result = dialog.getResult();
        if (result != null) {
            try {
                store.save(result);
                tableModel.addRow(result);
            } catch (Exception ex) {
                showError("Failed to save: " + ex.getMessage());
            }
        }
    }

    private void doEdit() {
        int row = getSelectedModelRow();
        if (row < 0) {
            showWarn("Please select a datasource to edit.");
            return;
        }
        DataSourceConfig existing = tableModel.getRow(row);
        DataSourceConfigDialog dialog = new DataSourceConfigDialog(getParentFrame(), existing);
        dialog.setVisible(true);
        DataSourceConfig result = dialog.getResult();
        if (result != null) {
            result.setId(existing.getId()); // preserve id
            try {
                store.update(result);
                tableModel.updateRow(row, result);
            } catch (Exception ex) {
                showError("Failed to update: " + ex.getMessage());
            }
        }
    }

    private void doDelete() {
        int row = getSelectedModelRow();
        if (row < 0) {
            showWarn("Please select a datasource to delete.");
            return;
        }
        DataSourceConfig config = tableModel.getRow(row);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete datasource \"" + config.getName() + "\"?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION)
            return;
        try {
            store.delete(config.getId());
            tableModel.removeRow(row);
        } catch (Exception ex) {
            showError("Failed to delete: " + ex.getMessage());
        }
    }

    private void doTestConnection() {
        int row = getSelectedModelRow();
        if (row < 0) {
            showWarn("Please select a datasource to test.");
            return;
        }
        DataSourceConfig config = tableModel.getRow(row);
        String url = config.getEffectiveJdbcUrl();
        if (url.isBlank()) {
            showWarn("No JDBC URL configured for this datasource.");
            return;
        }

        // Run in background to avoid freezing UI
        final int modelRow = row;
        config.setStatus("Testing...");
        tableModel.fireTableRowsUpdated(modelRow, modelRow);

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                try (Connection conn = DriverManager.getConnection(
                        url, config.getUsername(), config.getPassword())) {
                    String product = conn.getMetaData().getDatabaseProductName();
                    String version = conn.getMetaData().getDatabaseProductVersion();
                    return "✓ Connected (" + product + " " + version + ")";
                } catch (Exception ex) {
                    log.warn("Test connection failed for {}: {}", config.getName(), ex.getMessage());
                    return "✗ " + ex.getMessage();
                }
            }

            @Override
            protected void done() {
                try {
                    String status = get();
                    config.setStatus(status);
                    tableModel.fireTableRowsUpdated(modelRow, modelRow);
                    if (status.startsWith("✓")) {
                        JOptionPane.showMessageDialog(DataSourceManagerPane.this,
                                status, "Connection Successful", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(DataSourceManagerPane.this,
                                status, "Connection Failed", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    log.error("Worker error", ex);
                }
            }
        };
        worker.execute();
    }

    private void doExport(String format) {
        List<DataSourceConfig> data = tableModel.getAll();
        if (data.isEmpty()) {
            showWarn("No datasources to export.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Datasource Connections");
        chooser.setSelectedFile(new File("datasources." + format));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;
        File file = chooser.getSelectedFile();

        try {
            if ("md".equals(format)) {
                exporter.exportMarkdown(data, file);
            } else {
                exporter.exportExcel(data, file);
            }
            String[] opts = { "Open File", "Show in Explorer", "Close" };
            int choice = JOptionPane.showOptionDialog(this,
                    "Exported to: " + file.getAbsolutePath(),
                    "Export Complete", JOptionPane.DEFAULT_OPTION,
                    JOptionPane.INFORMATION_MESSAGE, null, opts, opts[0]);
            if (choice == 0 && Desktop.isDesktopSupported())
                Desktop.getDesktop().open(file);
            else if (choice == 1 && Desktop.isDesktopSupported())
                Runtime.getRuntime().exec(new String[] { "explorer.exe", "/select,", file.getAbsolutePath() });
        } catch (Exception ex) {
            log.error("Export failed", ex);
            showError("Export failed: " + ex.getMessage());
        }
    }

    // ---- Helpers ----

    private int getSelectedModelRow() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0)
            return -1;
        return table.convertRowIndexToModel(viewRow);
    }

    private Frame getParentFrame() {
        return (Frame) SwingUtilities.getWindowAncestor(this);
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
        return "Datasource Manager";
    }

    @Override
    public JComponent getView() {
        return this;
    }

    @Override
    public int getOrder() {
        return -100;
    }

    // ---- Status cell renderer: green for success, red for failure ----

    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value,
                boolean selected, boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, value, selected, focus, row, col);
            String text = value == null ? "" : value.toString();
            if (text.startsWith("✓")) {
                setForeground(selected ? Color.WHITE : new Color(0, 140, 0));
            } else if (text.startsWith("✗")) {
                setForeground(selected ? Color.WHITE : new Color(180, 0, 0));
            } else {
                setForeground(selected ? Color.WHITE : UIManager.getColor("Table.foreground"));
            }
            return this;
        }
    }
}
