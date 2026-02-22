package org.assistant.tools.doc;

import org.assistant.tools.doc.export.ApiExporter;
import org.assistant.tools.doc.export.ApiExporterRegistry;
import org.assistant.tools.doc.parser.WebApiScanner;
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
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Swing UI pane for the API Doc tool.
 * <p>
 * Provides project path input, parse button, API tree preview with
 * checkboxes, format selector, and export buttons.
 * </p>
 */
public class ApiDocToolPane extends BorderPane {

    private static final Logger log = LoggerFactory.getLogger(ApiDocToolPane.class);

    /** HTTP method → (background, foreground) color pair */
    private static final Map<String, Color[]> METHOD_COLORS = Map.of(
            "GET", new Color[] { new Color(0x61, 0xAF, 0xFE), Color.WHITE },
            "POST", new Color[] { new Color(0x49, 0xCC, 0x90), Color.WHITE },
            "PUT", new Color[] { new Color(0xFC, 0xA1, 0x30), Color.WHITE },
            "DELETE", new Color[] { new Color(0xF9, 0x3E, 0x3E), Color.WHITE },
            "PATCH", new Color[] { new Color(0x50, 0xE3, 0xC2), new Color(0x33, 0x33, 0x33) },
            "HEAD", new Color[] { new Color(0x90, 0x12, 0xFE), Color.WHITE },
            "OPTIONS", new Color[] { new Color(0x0D, 0x5A, 0xA7), Color.WHITE });

    private final TextField projectPathField;
    private final JTree apiTree;
    private final DefaultTreeModel treeModel;
    private final DefaultMutableTreeNode rootNode;
    private final ComboBox<String> formatCombo;
    private ApiProject currentProject;

    public ApiDocToolPane() {
        // --- Top: project path + Browse + Parse ---
        HBox topBar = new HBox();
        topBar.add(new Label("Project Path:"));

        projectPathField = new TextField("");
        projectPathField.setMaximumHeight(25);
        topBar.add(projectPathField);

        Button browseBtn = new Button("Browse");
        browseBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setDialogTitle("Select Java Project Root");
                int result = chooser.showOpenDialog(ApiDocToolPane.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    projectPathField.setText(chooser.getSelectedFile().getAbsolutePath());
                }
            }
        });
        topBar.add(browseBtn);

        Button parseBtn = new Button("Parse");
        parseBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                doParse();
            }
        });
        topBar.add(parseBtn);

        setTop(topBar);

        // --- Center: API tree with checkboxes ---
        rootNode = new DefaultMutableTreeNode("APIs");
        treeModel = new DefaultTreeModel(rootNode);
        apiTree = new JTree(treeModel);
        apiTree.setRootVisible(false);
        apiTree.setShowsRootHandles(true);
        apiTree.setRowHeight(28);
        apiTree.setCellRenderer(new ApiTreeCellRenderer());

        // Toggle checkbox on click
        apiTree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                TreePath path = apiTree.getPathForLocation(e.getX(), e.getY());
                if (path == null)
                    return;
                // Only toggle if click is in the checkbox area (first ~24px)
                Rectangle bounds = apiTree.getPathBounds(path);
                if (bounds != null && e.getX() < bounds.x + 24) {
                    Object node = path.getLastPathComponent();
                    if (node instanceof CheckableTreeNode) {
                        CheckableTreeNode checkNode = (CheckableTreeNode) node;
                        boolean newState = !checkNode.isChecked();
                        checkNode.setChecked(newState);
                        // Propagate to children
                        setChildrenChecked(checkNode, newState);
                        // Update parent state
                        updateParentCheckState(checkNode);
                        treeModel.nodeChanged(checkNode);
                        apiTree.repaint();
                    }
                }
            }
        });

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setViewportView(apiTree);
        setCenter(scrollPane);

        // --- Bottom: format selector + export ---
        HBox bottomBar = new HBox();
        bottomBar.add(new Label("Export Format:"));

        ApiExporterRegistry registry = ApiExporterRegistry.getInstance();
        formatCombo = new ComboBox<>();
        for (String name : registry.getFormatNames()) {
            formatCombo.addItem(name);
        }
        bottomBar.add(formatCombo);

        Button exportBtn = new Button("Export All");
        exportBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                doExport(currentProject);
            }
        });
        bottomBar.add(exportBtn);

        Button exportSelectedBtn = new Button("Export Selected");
        exportSelectedBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ApiProject selected = buildSelectedProject();
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

    private void doParse() {
        String projectPath = projectPathField.getText().trim();
        if (projectPath.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a project path.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Path path = Path.of(projectPath);
        if (!path.toFile().isDirectory()) {
            JOptionPane.showMessageDialog(this, "Invalid directory: " + projectPath, "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            WebApiScanner scanner = new WebApiScanner();
            currentProject = scanner.scan(path);

            // Populate tree
            rootNode.removeAllChildren();
            int apiCount = 0;
            for (ApiGroup group : currentProject.getGroups()) {
                CheckableTreeNode groupNode = new CheckableTreeNode(group.getName(), true);
                groupNode.setUserObject(group);

                for (WebApiInfo api : group.getApis()) {
                    CheckableTreeNode apiNode = new CheckableTreeNode(
                            api.getMethod() + " " + api.getPath(), true);
                    apiNode.setUserObject(api);
                    groupNode.add(apiNode);
                    apiCount++;
                }
                rootNode.add(groupNode);
            }
            treeModel.reload();

            // Expand all groups
            for (int i = 0; i < apiTree.getRowCount(); i++) {
                apiTree.expandRow(i);
            }

            JOptionPane.showMessageDialog(this,
                    "Parsed " + apiCount + " API endpoints from " +
                            currentProject.getGroups().size() + " controllers.",
                    "Parse Complete", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            log.error("Failed to parse project", ex);
            JOptionPane.showMessageDialog(this,
                    "Parse failed: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    private void doExport(ApiProject projectToExport) {
        if (projectToExport == null || projectToExport.getGroups().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No API data to export. Parse a project first.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String formatName = (String) formatCombo.getSelectedItem();
        if (formatName == null)
            return;

        ApiExporterRegistry registry = ApiExporterRegistry.getInstance();
        Optional<ApiExporter> exporterOpt = registry.getExporter(formatName);
        if (exporterOpt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Unknown format: " + formatName,
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ApiExporter exporter = exporterOpt.get();

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export API Documentation");
        String defaultName = (currentProject.getProjectName() != null ? currentProject.getProjectName() : "api")
                + "-api." + exporter.getFileExtension();
        chooser.setSelectedFile(new File(defaultName));
        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION)
            return;

        File outputFile = chooser.getSelectedFile();

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            exporter.export(projectToExport, outputFile);
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

    /**
     * Build a filtered ApiProject from tree nodes with checked checkboxes.
     */
    private ApiProject buildSelectedProject() {
        if (currentProject == null) {
            JOptionPane.showMessageDialog(this, "No API data. Parse a project first.",
                    "Warning", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        ApiProject filtered = new ApiProject();
        filtered.setProjectName(currentProject.getProjectName());
        filtered.setVersion(currentProject.getVersion());
        filtered.setDescription(currentProject.getDescription());
        filtered.setBasePath(currentProject.getBasePath());

        for (int i = 0; i < rootNode.getChildCount(); i++) {
            CheckableTreeNode groupNode = (CheckableTreeNode) rootNode.getChildAt(i);
            ApiGroup originalGroup = (ApiGroup) groupNode.getUserObject();

            List<WebApiInfo> checkedApis = new ArrayList<>();
            for (int j = 0; j < groupNode.getChildCount(); j++) {
                CheckableTreeNode apiNode = (CheckableTreeNode) groupNode.getChildAt(j);
                if (apiNode.isChecked()) {
                    checkedApis.add((WebApiInfo) apiNode.getUserObject());
                }
            }

            if (!checkedApis.isEmpty()) {
                ApiGroup filteredGroup = new ApiGroup();
                filteredGroup.setName(originalGroup.getName());
                filteredGroup.setDescription(originalGroup.getDescription());
                filteredGroup.setBasePath(originalGroup.getBasePath());
                filteredGroup.setApis(checkedApis);
                filtered.addGroup(filteredGroup);
            }
        }

        if (filtered.getGroups().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please check one or more APIs to export.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return filtered;
    }

    private void setAllChecked(boolean checked) {
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            CheckableTreeNode groupNode = (CheckableTreeNode) rootNode.getChildAt(i);
            groupNode.setChecked(checked);
            setChildrenChecked(groupNode, checked);
        }
        apiTree.repaint();
    }

    private void setChildrenChecked(CheckableTreeNode parent, boolean checked) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            CheckableTreeNode child = (CheckableTreeNode) parent.getChildAt(i);
            child.setChecked(checked);
            setChildrenChecked(child, checked);
        }
    }

    private void updateParentCheckState(CheckableTreeNode child) {
        TreeNode parent = child.getParent();
        if (parent instanceof CheckableTreeNode) {
            CheckableTreeNode parentNode = (CheckableTreeNode) parent;
            boolean allChecked = true;
            for (int i = 0; i < parentNode.getChildCount(); i++) {
                if (!((CheckableTreeNode) parentNode.getChildAt(i)).isChecked()) {
                    allChecked = false;
                    break;
                }
            }
            parentNode.setChecked(allChecked);
            treeModel.nodeChanged(parentNode);
        }
    }

    // ==================== Tree node with checkbox state ====================

    /**
     * A tree node that carries a checked/unchecked state for selection.
     */
    private static class CheckableTreeNode extends DefaultMutableTreeNode {
        private boolean checked;
        private final String displayName;

        CheckableTreeNode(String displayName, boolean checked) {
            super(displayName);
            this.displayName = displayName;
            this.checked = checked;
        }

        boolean isChecked() {
            return checked;
        }

        void setChecked(boolean checked) {
            this.checked = checked;
        }

        String getDisplayName() {
            return displayName;
        }
    }

    // ==================== Custom tree cell renderer ====================

    /**
     * Renders tree nodes with a checkbox and color-coded HTTP method badges
     * for API leaf nodes, and folder-style labels for group parent nodes.
     */
    private class ApiTreeCellRenderer extends DefaultTreeCellRenderer {

        private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        private final JCheckBox checkBox = new JCheckBox();
        private final JLabel methodLabel = new JLabel();
        private final JLabel textLabel = new JLabel();

        ApiTreeCellRenderer() {
            panel.setOpaque(false);
            checkBox.setOpaque(false);
            methodLabel.setOpaque(true);
            methodLabel.setFont(methodLabel.getFont().deriveFont(Font.BOLD, 11f));
            methodLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
            textLabel.setFont(textLabel.getFont().deriveFont(13f));
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            panel.removeAll();

            if (value instanceof CheckableTreeNode) {
                CheckableTreeNode node = (CheckableTreeNode) value;
                checkBox.setSelected(node.isChecked());
                panel.add(checkBox);

                Object data = node.getUserObject();
                if (data instanceof WebApiInfo) {
                    // Leaf: API endpoint — show [✓] [GET] /users - summary
                    WebApiInfo api = (WebApiInfo) data;

                    String method = api.getMethod().toUpperCase();
                    methodLabel.setText(method);
                    Color[] colors = METHOD_COLORS.get(method);
                    if (colors != null) {
                        methodLabel.setBackground(colors[0]);
                        methodLabel.setForeground(colors[1]);
                    } else {
                        methodLabel.setBackground(new Color(0x99, 0x99, 0x99));
                        methodLabel.setForeground(Color.WHITE);
                    }
                    panel.add(methodLabel);

                    String text = api.getPath();
                    if (api.getSummary() != null && !api.getSummary().isEmpty()) {
                        text += "  —  " + api.getSummary();
                    }
                    if (api.isDeprecated()) {
                        text += "  ⚠ DEPRECATED";
                    }
                    textLabel.setText(text);
                    textLabel.setForeground(sel ? getTextSelectionColor() : getTextNonSelectionColor());
                    panel.add(textLabel);
                } else if (data instanceof ApiGroup) {
                    // Parent: controller group — show [✓] 📂 ControllerName (N APIs)
                    ApiGroup group = (ApiGroup) data;
                    int apiCount = node.getChildCount();
                    long checkedCount = 0;
                    for (int i = 0; i < apiCount; i++) {
                        if (((CheckableTreeNode) node.getChildAt(i)).isChecked())
                            checkedCount++;
                    }
                    textLabel.setText("📂 " + group.getName() + "  (" + checkedCount + "/" + apiCount + ")");
                    textLabel.setForeground(sel ? getTextSelectionColor() : new Color(0x1a, 0x1a, 0x2e));
                    textLabel.setFont(textLabel.getFont().deriveFont(Font.BOLD, 13f));
                    panel.add(textLabel);
                } else {
                    textLabel.setText(node.getDisplayName());
                    textLabel.setForeground(getTextNonSelectionColor());
                    panel.add(textLabel);
                }
            }

            if (sel) {
                panel.setOpaque(true);
                panel.setBackground(getBackgroundSelectionColor());
            } else {
                panel.setOpaque(false);
            }

            return panel;
        }
    }
}
