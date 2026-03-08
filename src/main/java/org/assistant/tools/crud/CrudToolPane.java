package org.assistant.tools.crud;

import org.assistant.tools.ToolProvider;
import org.assistant.tools.datasource.DataSourceConfig;
import org.assistant.tools.datasource.DataSourceConfigStore;
import org.assistant.ui.pane.BorderPane;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Tool pane for generating MyBatis-Plus style CRUD files
 * (Entity, Mapper, Service, ServiceImpl, Controller, SQL)
 * from live database tables.
 */
public class CrudToolPane extends BorderPane implements ToolProvider {

    // ── Left panel controls ───────────────────────────────────────────────────
    private JComboBox<DataSourceConfig> dsCombo;
    private DefaultListModel<String> tableListModel;
    private JList<String> tableList;
    private JButton loadTablesBtn;
    private JButton generateBtn;
    private JButton settingsBtn;

    // ── Center panel ──────────────────────────────────────────────────────────
    private JTree fileTree;
    private DefaultTreeModel treeModel;
    private RSyntaxTextArea contentViewer;
    private JLabel statusLabel;

    // ── State ─────────────────────────────────────────────────────────────────
    private final CrudGeneratorConfig config = new CrudGeneratorConfig();

    public CrudToolPane() {
        setLayout(new BorderLayout());
        initUI();
    }

    // ── Build UI ──────────────────────────────────────────────────────────────

    private void initUI() {
        // ══════════ LEFT: datasource + tables ══════════
        JPanel leftPanel = new JPanel(new BorderLayout(0, 4));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        leftPanel.setPreferredSize(new Dimension(250, 0));

        // Datasource selector
        JPanel dsPanel = new JPanel(new BorderLayout(4, 0));
        dsPanel.setBorder(BorderFactory.createTitledBorder("Datasource"));
        dsCombo = new JComboBox<>();
        dsCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean sel, boolean focus) {
                super.getListCellRendererComponent(list, value, index, sel, focus);
                if (value instanceof DataSourceConfig ds) {
                    setText(ds.getName() + " (" + ds.getType() + ")");
                }
                return this;
            }
        });
        refreshDatasources();
        JButton refreshDsBtn = new JButton("↻");
        refreshDsBtn.setToolTipText("Refresh datasource list");
        refreshDsBtn.addActionListener(e -> refreshDatasources());
        dsPanel.add(dsCombo, BorderLayout.CENTER);
        dsPanel.add(refreshDsBtn, BorderLayout.EAST);
        leftPanel.add(dsPanel, BorderLayout.NORTH);

        // Table list
        JPanel tablePanel = new JPanel(new BorderLayout(0, 4));
        tablePanel.setBorder(BorderFactory.createTitledBorder("Tables"));
        tableListModel = new DefaultListModel<>();
        tableList = new JList<>(tableListModel);
        tableList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        tablePanel.add(new JScrollPane(tableList), BorderLayout.CENTER);

        JPanel tableBtnPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        loadTablesBtn = new JButton("Load Tables");
        loadTablesBtn.addActionListener(e -> doLoadTables());
        JButton selectAllBtn = new JButton("Select All");
        selectAllBtn.addActionListener(e -> {
            if (tableListModel.size() > 0) {
                tableList.setSelectionInterval(0, tableListModel.size() - 1);
            }
        });
        tableBtnPanel.add(loadTablesBtn);
        tableBtnPanel.add(selectAllBtn);
        tablePanel.add(tableBtnPanel, BorderLayout.SOUTH);
        leftPanel.add(tablePanel, BorderLayout.CENTER);

        // Bottom buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 4));
        generateBtn = new JButton("▶ Generate");
        generateBtn.addActionListener(e -> doGenerate());
        settingsBtn = new JButton("⚙ Settings");
        settingsBtn.addActionListener(e -> openSettings());
        actionPanel.add(generateBtn);
        actionPanel.add(settingsBtn);
        leftPanel.add(actionPanel, BorderLayout.SOUTH);

        // ══════════ CENTER: file tree + content viewer ══════════
        GeneratedFileNode emptyRoot = new GeneratedFileNode("(No files generated)");
        treeModel = new DefaultTreeModel(emptyRoot);
        fileTree = new JTree(treeModel);
        fileTree.setRootVisible(true);
        fileTree.addTreeSelectionListener(e -> showSelectedFile());
        JScrollPane treeScroll = new JScrollPane(fileTree);
        treeScroll.setPreferredSize(new Dimension(280, 0));

        contentViewer = new RSyntaxTextArea(24, 60);
        contentViewer.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        contentViewer.setEditable(false);
        contentViewer.setCodeFoldingEnabled(true);
        RTextScrollPane viewerScroll = new RTextScrollPane(contentViewer);

        JSplitPane centerSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, viewerScroll);
        centerSplit.setResizeWeight(0.3);

        // ══════════ BOTTOM: status bar + actions ══════════
        JPanel bottomBar = new JPanel(new BorderLayout());
        bottomBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.GRAY);

        JPanel bottomActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        JButton copyBtn = new JButton("Copy");
        copyBtn.setToolTipText("Copy the displayed file content to clipboard");
        copyBtn.addActionListener(e -> {
            String text = contentViewer.getText();
            if (text != null && !text.isBlank()) {
                Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new StringSelection(text), null);
                setStatus("Copied to clipboard.");
            }
        });
        JButton saveAllBtn = new JButton("Save All");
        saveAllBtn.setToolTipText("Write all generated files to the output directory");
        saveAllBtn.addActionListener(e -> doSaveAll());
        bottomActions.add(copyBtn);
        bottomActions.add(saveAllBtn);

        bottomBar.add(statusLabel, BorderLayout.CENTER);
        bottomBar.add(bottomActions, BorderLayout.EAST);

        // ══════════ Assemble ══════════
        add(leftPanel, BorderLayout.WEST);
        add(centerSplit, BorderLayout.CENTER);
        add(bottomBar, BorderLayout.SOUTH);
    }

    // ── Actions ───────────────────────────────────────────────────────────────

    private void refreshDatasources() {
        dsCombo.removeAllItems();
        DataSourceConfigStore.getInstance().findAll()
                .forEach(dsCombo::addItem);
    }

    private void doLoadTables() {
        DataSourceConfig ds = (DataSourceConfig) dsCombo.getSelectedItem();
        if (ds == null) {
            showError("Please select a datasource first.");
            return;
        }
        setStatus("Loading tables…");
        tableListModel.clear();

        SwingWorker<List<String>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return new TableSchemaLoader(ds).loadTableNames();
            }

            @Override
            protected void done() {
                try {
                    List<String> names = get();
                    names.forEach(tableListModel::addElement);
                    setStatus("✓ " + names.size() + " tables loaded.");
                } catch (Exception ex) {
                    showError("Failed to load tables: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void doGenerate() {
        DataSourceConfig ds = (DataSourceConfig) dsCombo.getSelectedItem();
        if (ds == null) {
            showError("Please select a datasource.");
            return;
        }
        List<String> selectedTables = tableList.getSelectedValuesList();
        if (selectedTables.isEmpty()) {
            showError("Please select at least one table.");
            return;
        }

        setStatus("Generating…");
        generateBtn.setEnabled(false);

        SwingWorker<Map<String, String>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<String, String> doInBackground() throws Exception {
                TableSchemaLoader loader = new TableSchemaLoader(ds);
                CrudGenerator gen = new CrudGenerator(config, loader);
                return gen.generate(selectedTables);
            }

            @Override
            protected void done() {
                generateBtn.setEnabled(true);
                try {
                    Map<String, String> files = get();
                    buildFileTree(files);
                    setStatus("✓ Generated " + files.size() + " files for " +
                            selectedTables.size() + " table(s).");
                } catch (Exception ex) {
                    showError("Generation failed: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void openSettings() {
        Frame frame = (Frame) SwingUtilities.getWindowAncestor(this);
        CrudGeneratorConfigDialog dlg = new CrudGeneratorConfigDialog(frame, config);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            setStatus("Settings applied.");
        }
    }

    // ── File tree ─────────────────────────────────────────────────────────────

    private void buildFileTree(Map<String, String> files) {
        GeneratedFileNode root = new GeneratedFileNode("generated");
        for (Map.Entry<String, String> entry : files.entrySet()) {
            String path = entry.getKey();
            String content = entry.getValue();
            String[] parts = path.split("/");

            GeneratedFileNode current = root;
            for (int i = 0; i < parts.length; i++) {
                boolean isLast = (i == parts.length - 1);
                if (isLast) {
                    // leaf / file
                    current.add(new GeneratedFileNode(path, content));
                } else {
                    // find or create directory node
                    GeneratedFileNode child = findChild(current, parts[i]);
                    if (child == null) {
                        child = new GeneratedFileNode(parts[i]);
                        current.add(child);
                    }
                    current = child;
                }
            }
        }
        treeModel.setRoot(root);
        expandAllNodes(fileTree, 0, fileTree.getRowCount());
    }

    private GeneratedFileNode findChild(GeneratedFileNode parent, String name) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            GeneratedFileNode child = (GeneratedFileNode) parent.getChildAt(i);
            if (child.isDirectory() && name.equals(child.getUserObject())) {
                return child;
            }
        }
        return null;
    }

    private void expandAllNodes(JTree tree, int startRow, int rowCount) {
        for (int i = startRow; i < rowCount; i++) {
            tree.expandRow(i);
        }
        if (tree.getRowCount() != rowCount) {
            expandAllNodes(tree, rowCount, tree.getRowCount());
        }
    }

    private void showSelectedFile() {
        TreePath path = fileTree.getSelectionPath();
        if (path == null)
            return;
        Object last = path.getLastPathComponent();
        if (last instanceof GeneratedFileNode node && node.isFile()) {
            contentViewer.setSyntaxEditingStyle(node.getSyntaxStyle());
            contentViewer.setText(node.getContent());
            contentViewer.setCaretPosition(0);
        }
    }

    // ── Save all files ────────────────────────────────────────────────────────

    private void doSaveAll() {
        Object rootObj = treeModel.getRoot();
        if (!(rootObj instanceof GeneratedFileNode root) || root.getChildCount() == 0) {
            showError("Nothing to save. Generate files first.");
            return;
        }

        JFileChooser fc = new JFileChooser(config.getOutputBaseDir());
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setDialogTitle("Select output directory");
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
            return;
        File outDir = fc.getSelectedFile();
        config.setOutputBaseDir(outDir.getAbsolutePath());

        int count = saveRecursive(root, outDir.toPath());
        setStatus("✓ Saved " + count + " files to " + outDir.getAbsolutePath());
    }

    private int saveRecursive(GeneratedFileNode node, Path baseDir) {
        int count = 0;
        for (int i = 0; i < node.getChildCount(); i++) {
            GeneratedFileNode child = (GeneratedFileNode) node.getChildAt(i);
            if (child.isFile()) {
                try {
                    Path target = baseDir.resolve(child.getFilePath());
                    Files.createDirectories(target.getParent());
                    Files.writeString(target, child.getContent(), StandardCharsets.UTF_8);
                    count++;
                } catch (IOException e) {
                    showError("Failed to write " + child.getFilePath() + ": " + e.getMessage());
                }
            } else {
                count += saveRecursive(child, baseDir);
            }
        }
        return count;
    }

    // ── Status helpers ────────────────────────────────────────────────────────

    private void setStatus(String msg) {
        statusLabel.setForeground(msg.startsWith("✓") ? new Color(0, 120, 0) : Color.GRAY);
        statusLabel.setText(msg);
    }

    private void showError(String msg) {
        statusLabel.setForeground(Color.RED);
        statusLabel.setText("✗ " + msg);
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ── ToolProvider ──────────────────────────────────────────────────────────

    @Override
    public String getLabel() {
        return "CRUD Generator";
    }

    @Override
    public JComponent getView() {
        return this;
    }

    @Override
    public int getOrder() {
        return -30;
    }
}
