package org.assistant.tools.db;

import org.assistant.tools.ToolProvider;
import org.assistant.tools.db.export.SchemaExporter;
import org.assistant.tools.db.export.SchemaExporterRegistry;
import org.assistant.tools.db.parser.DbScanner;
import org.assistant.tools.db.parser.DbSchema;
import org.assistant.tools.db.parser.TableInfo;
import org.assistant.ui.controls.Button;
import org.assistant.ui.controls.ComboBox;
import org.assistant.ui.controls.Label;
import org.assistant.ui.controls.TextField;
import org.assistant.ui.pane.BorderPane;
import org.assistant.ui.pane.HBox;
import org.assistant.ui.pane.ScrollPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.tree.*;
import org.assistant.ui.controls.table.TreeTable;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Optional;

public class DbSchemaToolPane extends BorderPane implements ToolProvider {

    private static final Logger log = LoggerFactory.getLogger(DbSchemaToolPane.class);

    private final TextField urlField;
    private final TextField usernameField;
    private final TextField passwordField;

    private final TreeTable<Object> schemaTable;
    private DbSchemaTreeTableModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private final ComboBox<String> formatCombo;

    private DbSchema currentSchema;

    public DbSchemaToolPane() {
        // --- Top: DB connection info ---
        HBox topBar = new HBox();
        topBar.add(new Label("JDBC URL:"));
        urlField = new TextField("jdbc:mysql://localhost:3306/your_db");
        topBar.add(urlField);

        topBar.add(new Label("User:"));
        usernameField = new TextField("root");
        topBar.add(usernameField);

        topBar.add(new Label("Pass:"));
        passwordField = new TextField("");
        topBar.add(passwordField);

        Button connectBtn = new Button("Connect & Parse");
        connectBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                doParse();
            }
        });
        topBar.add(connectBtn);
        setTop(topBar);

        // --- Center: Schema tree table ---
        schemaTable = new TreeTable<>();
        rootNode = new DefaultMutableTreeNode("Database");
        treeModel = new DbSchemaTreeTableModel(rootNode);
        treeModel.bindTable(schemaTable);
        schemaTable.setRowHeight(28);

        schemaTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                    int row = schemaTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        int modelRow = schemaTable.convertRowIndexToModel(row);
                        TreeNode node = treeModel.getVisibleNodes().get(modelRow);
                        if (node.getAllowsChildren()) {
                            treeModel.toggleNode(node);
                        }
                    }
                }
            }
        });

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setViewportView(schemaTable);
        setCenter(scrollPane);

        // --- Bottom: Export options ---
        HBox bottomBar = new HBox();
        bottomBar.add(new Label("Export Format:"));

        SchemaExporterRegistry registry = SchemaExporterRegistry.getInstance();
        formatCombo = new ComboBox<>();
        for (String name : registry.getFormatNames()) {
            formatCombo.addItem(name);
        }
        bottomBar.add(formatCombo);

        Button exportBtn = new Button("Export All");
        exportBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                doExport(currentSchema);
            }
        });
        bottomBar.add(exportBtn);

        Button exportSelectedBtn = new Button("Export Selected");
        exportSelectedBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                DbSchema selected = buildSelectedSchema();
                if (selected != null) {
                    doExport(selected);
                }
            }
        });
        bottomBar.add(exportSelectedBtn);

        Button selectAllBtn = new Button("Select All");
        selectAllBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setAllChecked(true);
            }
        });
        bottomBar.add(selectAllBtn);

        Button deselectAllBtn = new Button("Deselect All");
        deselectAllBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setAllChecked(false);
            }
        });
        bottomBar.add(deselectAllBtn);

        setBottom(bottomBar);
    }

    @Override
    public String getLabel() {
        return "DB Schema Tools";
    }

    @Override
    public JComponent getView() {
        return this;
    }

    private void doParse() {
        String url = urlField.getText().trim();
        String user = usernameField.getText().trim();
        String pass = passwordField.getText().trim();

        if (url.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter JDBC URL.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            DbScanner scanner = new DbScanner();
            currentSchema = scanner.scan(url, user, pass);

            rootNode = new DefaultMutableTreeNode("Database");
            DefaultMutableTreeNode dbNode = new DefaultMutableTreeNode(
                    new DbSchemaTreeTableModel.SchemaWrapper(
                            currentSchema.getCatalog() != null ? currentSchema.getCatalog() : "Database"));

            for (TableInfo table : currentSchema.getTables()) {
                DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(
                        new DbSchemaTreeTableModel.TableWrapper(table));
                dbNode.add(tableNode);
            }
            rootNode.add(dbNode);

            treeModel.unbindTable();
            treeModel = new DbSchemaTreeTableModel(rootNode);
            treeModel.bindTable(schemaTable);

            JOptionPane.showMessageDialog(this,
                    "Parsed " + currentSchema.getTables().size() + " tables.",
                    "Parse Complete", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            log.error("Failed to parse database schema", ex);
            JOptionPane.showMessageDialog(this,
                    "Parse failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private DbSchema buildSelectedSchema() {
        if (currentSchema == null) {
            JOptionPane.showMessageDialog(this, "No schema data. Parse a database first.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        DbSchema filtered = new DbSchema();
        filtered.setCatalog(currentSchema.getCatalog());
        filtered.setSchema(currentSchema.getSchema());

        if (rootNode.getChildCount() > 0) {
            DefaultMutableTreeNode dbNode = (DefaultMutableTreeNode) rootNode.getChildAt(0);
            for (int j = 0; j < dbNode.getChildCount(); j++) {
                DefaultMutableTreeNode tableNode = (DefaultMutableTreeNode) dbNode.getChildAt(j);
                DbSchemaTreeTableModel.TableWrapper tw = (DbSchemaTreeTableModel.TableWrapper) tableNode
                        .getUserObject();
                if (tw.isChecked) {
                    filtered.addTable(tw.table);
                }
            }
        }

        if (filtered.getTables().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please check one or more tables to export.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return filtered;
    }

    private void doExport(DbSchema schemaToExport) {
        if (schemaToExport == null || schemaToExport.getTables().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No table data to export.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String formatName = (String) formatCombo.getSelectedItem();
        if (formatName == null)
            return;

        Optional<SchemaExporter> exporterOpt = SchemaExporterRegistry.getInstance().getExporter(formatName);
        if (exporterOpt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Unknown format: " + formatName,
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SchemaExporter exporter = exporterOpt.get();

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Schema Documentation");
        String defaultName = (currentSchema.getCatalog() != null ? currentSchema.getCatalog() : "schema")
                + "." + exporter.getFileExtension();
        chooser.setSelectedFile(new File(defaultName));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION)
            return;

        File outputFile = chooser.getSelectedFile();

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            exporter.export(schemaToExport, outputFile);
            showExportSuccessDialog(outputFile);
        } catch (Exception ex) {
            log.error("Export failed", ex);
            JOptionPane.showMessageDialog(this,
                    "Export failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void showExportSuccessDialog(File outputFile) {
        String[] options = { "Open File", "Show in Explorer", "Close" };
        int choice = JOptionPane.showOptionDialog(this,
                "Exported to: " + outputFile.getAbsolutePath(),
                "Export Complete",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null, options, options[0]);

        try {
            if (choice == 0 && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(outputFile);
            } else if (choice == 1 && Desktop.isDesktopSupported()) {
                Runtime.getRuntime().exec(new String[] { "explorer.exe", "/select,", outputFile.getAbsolutePath() });
            }
        } catch (Exception ex) {
            log.error("Failed to open/show file", ex);
        }
    }

    private void setAllChecked(boolean checked) {
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            DefaultMutableTreeNode dbNode = (DefaultMutableTreeNode) rootNode.getChildAt(i);
            DbSchemaTreeTableModel.SchemaWrapper sw = (DbSchemaTreeTableModel.SchemaWrapper) dbNode.getUserObject();
            sw.isChecked = checked;
            for (int j = 0; j < dbNode.getChildCount(); j++) {
                DefaultMutableTreeNode tableNode = (DefaultMutableTreeNode) dbNode.getChildAt(j);
                DbSchemaTreeTableModel.TableWrapper tw = (DbSchemaTreeTableModel.TableWrapper) tableNode
                        .getUserObject();
                tw.isChecked = checked;
            }
        }
        treeModel.fireTableDataChanged();
    }
}
