package org.assistant.tools.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.assistant.tools.ToolProvider;
import org.assistant.ui.controls.table.SwingTreeTable;
import org.assistant.ui.pane.BorderPane;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class JsonToolPane implements ToolProvider {

    private final BorderPane borderPane;
    private RSyntaxTextArea jsonInputArea;
    private JButton loadFileButton;
    private JButton formatButton;
    private JButton parseButton;
    private JButton exportButton;

    private SwingTreeTable currentTreeTable;
    private JsonTreeTableModel treeTableModel;

    private JTextField searchField;
    private JButton searchButton;
    private JButton configPojoButton;
    private JButton configSchemaButton;

    // Search state
    private String lastSearchTerm = "";
    private List<TreePath> searchResults = new ArrayList<>();
    private int currentSearchIndex = -1;

    public JsonToolPane() {
        borderPane = new BorderPane();
        initComponents();
        layoutComponents();
        setupListeners();
    }

    private void initComponents() {
        jsonInputArea = new RSyntaxTextArea(20, 40);
        jsonInputArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        jsonInputArea.setCodeFoldingEnabled(true);
        jsonInputArea.setToolTipText("Paste JSON payload here");

        loadFileButton = new JButton("Load File");
        formatButton = new JButton("Format JSON");
        parseButton = new JButton("Parse / Analyze JSON");
        exportButton = new JButton("Export to JSON");

        treeTableModel = new JsonTreeTableModel();
        currentTreeTable = new SwingTreeTable(treeTableModel);
        currentTreeTable.setRowHeight(24);
        currentTreeTable.setShowGrid(true, true);

        TableColumn typeColumn = currentTreeTable.getColumnModel().getColumn(2);
        typeColumn.setPreferredWidth(80);
        typeColumn.setMaxWidth(100);

        searchField = new JTextField(20);
        searchField.setToolTipText("Find specific Key in hierarchy...");

        searchButton = new JButton("Search");
        configPojoButton = new JButton("⚙ Config POJO Setup");
        configSchemaButton = new JButton("⚙ Config JSON Schema");
    }

    private void layoutComponents() {
        // Left Panel (Input)
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Raw JSON Payload"));
        leftPanel.add(new RTextScrollPane(jsonInputArea), BorderLayout.CENTER);

        JPanel leftBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        leftBottom.add(loadFileButton);
        leftBottom.add(exportButton);
        leftBottom.add(formatButton);
        leftBottom.add(parseButton);
        leftPanel.add(leftBottom, BorderLayout.SOUTH);

        // Top Search / Action Bar over the Tree
        JPanel topActionBar = new JPanel(new BorderLayout());

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search Node Key:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        JPanel configPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        configPanel.add(configPojoButton);
        configPanel.add(configSchemaButton);

        topActionBar.add(searchPanel, BorderLayout.NORTH);
        topActionBar.add(configPanel, BorderLayout.SOUTH);

        // Right Panel (Tree)
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Interactive Structure Viewer"));
        rightPanel.add(topActionBar, BorderLayout.NORTH);

        JScrollPane treeScrollPane = new JScrollPane(currentTreeTable);
        rightPanel.add(treeScrollPane, BorderLayout.CENTER);

        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        mainSplitPane.setResizeWeight(0.3); // 30% left / 70% right

        borderPane.add(mainSplitPane, BorderLayout.CENTER);
    }

    private void setupListeners() {
        loadFileButton.addActionListener(e -> loadJsonFile());
        exportButton.addActionListener(e -> exportJsonContent());
        formatButton.addActionListener(e -> formatJsonContent());
        parseButton.addActionListener(e -> parseJsonContent());

        searchButton.addActionListener(e -> performSearch());
        searchField.addActionListener(e -> performSearch()); // Enter key trigger

        configPojoButton.addActionListener(e -> configureAndGeneratePojo());
        configSchemaButton.addActionListener(e -> configureAndGenerateSchema());

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem expandItem = new JMenuItem("Expand Node");
        JMenuItem expandRecursiveItem = new JMenuItem("Expand Node (Recursively)");
        JMenuItem collapseItem = new JMenuItem("Collapse Node");
        JMenuItem collapseRecursiveItem = new JMenuItem("Collapse Node (Recursively)");

        JMenuItem addNodeItem = new JMenuItem("Add Child Node");
        JMenuItem removeNodeItem = new JMenuItem("Remove Node");
        JMenuItem renameKeyItem = new JMenuItem("Rename Key");
        JMenuItem mergeJsonItem = new JMenuItem("Merge JSON Data");

        popupMenu.add(expandItem);
        popupMenu.add(expandRecursiveItem);
        popupMenu.add(collapseItem);
        popupMenu.add(collapseRecursiveItem);
        popupMenu.addSeparator();
        popupMenu.add(addNodeItem);
        popupMenu.add(removeNodeItem);
        popupMenu.add(renameKeyItem);
        popupMenu.add(mergeJsonItem);

        java.awt.event.ActionListener popupListener = ev -> {
            int row = currentTreeTable.getSelectedRow();
            if (row >= 0) {
                TreePath path = currentTreeTable.getPathForRow(row);
                if (path != null) {
                    Object node = path.getLastPathComponent();
                    if (node instanceof JsonNode jsonNode) {
                        if (ev.getSource() == expandItem || ev.getSource() == expandRecursiveItem || ev.getSource() == collapseItem || ev.getSource() == collapseRecursiveItem) {
                            if (!jsonNode.isLeaf()) {
                                boolean expand = ev.getSource() == expandItem || ev.getSource() == expandRecursiveItem;
                                boolean recursive = ev.getSource() == expandRecursiveItem || ev.getSource() == collapseRecursiveItem;
                                SwingUtilities.invokeLater(() -> {
                                    if (expand) {
                                        org.assistant.util.SwingUtils.expandNode(currentTreeTable, path, recursive);
                                    } else {
                                        org.assistant.util.SwingUtils.collapseNode(currentTreeTable, path, recursive);
                                    }
                                });
                            }
                        } else if (ev.getSource() == removeNodeItem) {
                            org.jdesktop.swingx.treetable.TreeTableNode parent = jsonNode.getParent();
                            if (parent instanceof JsonNode parentNode && parentNode.getJsonElement() != null) {
                                com.google.gson.JsonElement parentEl = parentNode.getJsonElement();
                                if (parentEl.isJsonObject()) {
                                    parentEl.getAsJsonObject().remove(jsonNode.getKey());
                                } else if (parentEl.isJsonArray()) {
                                    String keyStr = jsonNode.getKey();
                                    if (keyStr.startsWith("[") && keyStr.endsWith("]")) {
                                        try {
                                            int idx = Integer.parseInt(keyStr.substring(1, keyStr.length() - 1));
                                            parentEl.getAsJsonArray().remove(idx);
                                        } catch (NumberFormatException ignored) {}
                                    }
                                }
                                treeTableModel.removeNodeFromParent(jsonNode);
                                parentNode.reloadChildren(); // resync array indices visually
                                treeTableModel.fireNodeStructureChanged(parentNode);
                            } else if (parent == null || parent == treeTableModel.getRoot()) {
                                JOptionPane.showMessageDialog(borderPane, "Cannot remove the root node.", "Warning", JOptionPane.WARNING_MESSAGE);
                            }
                        } else if (ev.getSource() == renameKeyItem) {
                            org.jdesktop.swingx.treetable.TreeTableNode parent = jsonNode.getParent();
                            if (parent instanceof JsonNode parentNode && parentNode.getJsonElement() != null && parentNode.getJsonElement().isJsonObject()) {
                                String newKey = JOptionPane.showInputDialog(borderPane, "Enter new key name:", jsonNode.getKey());
                                if (newKey != null && !newKey.isBlank() && !newKey.equals(jsonNode.getKey())) {
                                    com.google.gson.JsonObject parentObj = parentNode.getJsonElement().getAsJsonObject();
                                    if (parentObj.has(newKey)) {
                                        JOptionPane.showMessageDialog(borderPane, "Key already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                                        return;
                                    }
                                    com.google.gson.JsonElement el = parentObj.remove(jsonNode.getKey());
                                    parentObj.add(newKey, el);
                                    jsonNode.setKey(newKey);
                                    treeTableModel.fireNodeChanged(jsonNode);
                                }
                            } else {
                                JOptionPane.showMessageDialog(borderPane, "Can only rename keys within a JSON Object.", "Warning", JOptionPane.WARNING_MESSAGE);
                            }
                        } else if (ev.getSource() == addNodeItem) {
                            if (!jsonNode.getJsonElement().isJsonObject() && !jsonNode.getJsonElement().isJsonArray()) {
                                JOptionPane.showMessageDialog(borderPane, "Can only add children to Arrays or Objects.", "Warning", JOptionPane.WARNING_MESSAGE);
                                return;
                            }
                            String key = "";
                            if (jsonNode.getJsonElement().isJsonObject()) {
                                key = JOptionPane.showInputDialog(borderPane, "Enter new key name:");
                                if (key == null || key.isBlank()) return;
                                if (jsonNode.getJsonElement().getAsJsonObject().has(key)) {
                                    JOptionPane.showMessageDialog(borderPane, "Key already exists!", "Error", JOptionPane.ERROR_MESSAGE);
                                    return;
                                }
                            }
                            String val = JOptionPane.showInputDialog(borderPane, "Enter string value (or valid JSON):");
                            if (val == null) return;
                            
                            com.google.gson.JsonElement newEl;
                            try {
                                newEl = com.google.gson.JsonParser.parseString(val);
                                if (!newEl.isJsonPrimitive() && !newEl.isJsonNull()) {
                                    newEl = new com.google.gson.JsonPrimitive(val); // fallback if accidentally parsed as object string without intent
                                }
                            } catch (Exception e) {
                                newEl = new com.google.gson.JsonPrimitive(val);
                            }
                            
                            if (jsonNode.getJsonElement().isJsonObject()) {
                                jsonNode.getJsonElement().getAsJsonObject().add(key, newEl);
                            } else {
                                jsonNode.getJsonElement().getAsJsonArray().add(newEl);
                            }
                            jsonNode.reloadChildren();
                            treeTableModel.fireNodeStructureChanged(jsonNode);
                            currentTreeTable.expandPath(path);
                        } else if (ev.getSource() == mergeJsonItem) {
                            if (!jsonNode.getJsonElement().isJsonObject() && !jsonNode.getJsonElement().isJsonArray()) {
                                JOptionPane.showMessageDialog(borderPane, "Can only merge into Arrays or Objects.", "Warning", JOptionPane.WARNING_MESSAGE);
                                return;
                            }
                            JTextArea textArea = new JTextArea(10, 30);
                            int result = JOptionPane.showConfirmDialog(borderPane, new JScrollPane(textArea), "Paste JSON to Merge", JOptionPane.OK_CANCEL_OPTION);
                            if (result == JOptionPane.OK_OPTION) {
                                try {
                                    com.google.gson.JsonElement parsed = com.google.gson.JsonParser.parseString(textArea.getText());
                                    if (jsonNode.getJsonElement().isJsonObject() && parsed.isJsonObject()) {
                                        for (java.util.Map.Entry<String, com.google.gson.JsonElement> entry : parsed.getAsJsonObject().entrySet()) {
                                            jsonNode.getJsonElement().getAsJsonObject().add(entry.getKey(), entry.getValue());
                                        }
                                    } else if (jsonNode.getJsonElement().isJsonArray()) {
                                        jsonNode.getJsonElement().getAsJsonArray().add(parsed);
                                    } else {
                                        JOptionPane.showMessageDialog(borderPane, "Cannot merge structural mismatch (e.g., Array into Object root payload).", "Error", JOptionPane.ERROR_MESSAGE);
                                        return;
                                    }
                                    jsonNode.reloadChildren();
                                    treeTableModel.fireNodeStructureChanged(jsonNode);
                                    currentTreeTable.expandPath(path);
                                } catch (Exception e) {
                                    JOptionPane.showMessageDialog(borderPane, "Invalid JSON input:\n" + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            }
                        }
                    }
                }
            }
        };

        expandItem.addActionListener(popupListener);
        expandRecursiveItem.addActionListener(popupListener);
        collapseItem.addActionListener(popupListener);
        collapseRecursiveItem.addActionListener(popupListener);
        addNodeItem.addActionListener(popupListener);
        removeNodeItem.addActionListener(popupListener);
        renameKeyItem.addActionListener(popupListener);
        mergeJsonItem.addActionListener(popupListener);

        currentTreeTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) {
                    int row = currentTreeTable.rowAtPoint(e.getPoint());
                    if (row >= 0) {
                        TreePath path = currentTreeTable.getPathForRow(row);
                        if (path != null) {
                            Object node = path.getLastPathComponent();
                            if (node instanceof JsonNode jsonNode && !jsonNode.isLeaf()) {
                                boolean recursive = e.isShiftDown();
                                SwingUtilities.invokeLater(() -> {
                                    if (currentTreeTable.isExpanded(path)) {
                                        org.assistant.util.SwingUtils.collapseNode(currentTreeTable, path, recursive);
                                    } else {
                                        org.assistant.util.SwingUtils.expandNode(currentTreeTable, path, recursive);
                                    }
                                });
                            }
                        }
                    }
                }
            }

            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                showPopup(e);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                showPopup(e);
            }

            private void showPopup(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) {
                    int row = currentTreeTable.rowAtPoint(e.getPoint());
                    if (row >= 0 && row < currentTreeTable.getRowCount()) {
                        currentTreeTable.setRowSelectionInterval(row, row);
                        popupMenu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        });
    }

    private void loadJsonFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select JSON File");
        chooser.setFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter("JSON Documents (*.json)", "json", "txt"));
        int result = chooser.showOpenDialog(borderPane);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            try {
                String content = Files.readString(selectedFile.toPath());
                jsonInputArea.setText(content);
                jsonInputArea.setCaretPosition(0);
                parseJsonContent(); // Auto-parse after loading
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(borderPane,
                        "Error reading file:\n" + ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void configureAndGenerateSchema() {
        JsonNode targetNode = null;
        int viewRow = currentTreeTable.getSelectedRow();
        if (viewRow < 0) {
            targetNode = (JsonNode) treeTableModel.getRoot();
            if (targetNode == null) {
                JOptionPane.showMessageDialog(borderPane, "Please load and parse a JSON payload first.",
                        "No Data", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } else {
            int modelRow = currentTreeTable.convertRowIndexToModel(viewRow);
            TreePath path = currentTreeTable.getPathForRow(modelRow);
            if (path != null) {
                Object node = path.getLastPathComponent();
                if (node instanceof JsonNode jsonNode) {
                    targetNode = jsonNode;
                }
            }
        }

        if (targetNode != null) {
            JsonElement element = targetNode.getJsonElement();
            if (element != null) {
                Window parentWindow = SwingUtilities.getWindowAncestor(borderPane);
                if (parentWindow == null)
                    parentWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();

                if (parentWindow instanceof Frame frame) {
                    JsonSchemaConfigDialog dialog = new JsonSchemaConfigDialog(frame, element);
                    dialog.setVisible(true);
                }
            }
        }
    }

    private void configureAndGeneratePojo() {
        JsonNode targetNode = null;
        int viewRow = currentTreeTable.getSelectedRow();
        if (viewRow < 0) {
            targetNode = (JsonNode) treeTableModel.getRoot();
            if (targetNode == null) {
                JOptionPane.showMessageDialog(borderPane, "Please load and parse a JSON payload first.",
                        "No Data", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } else {
            int modelRow = currentTreeTable.convertRowIndexToModel(viewRow);
            TreePath path = currentTreeTable.getPathForRow(modelRow);
            if (path != null) {
                Object node = path.getLastPathComponent();
                if (node instanceof JsonNode jsonNode) {
                    targetNode = jsonNode;
                }
            }
        }

        if (targetNode != null) {
            JsonElement element = targetNode.getJsonElement();
            if (element != null && element.isJsonObject()) {
                Window parentWindow = SwingUtilities.getWindowAncestor(borderPane);
                if (parentWindow == null)
                    parentWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();

                if (parentWindow instanceof Frame frame) {
                    PojoConfigDialog dialog = new PojoConfigDialog(frame, element);
                    dialog.setVisible(true);
                }
            } else {
                JOptionPane.showMessageDialog(borderPane,
                        "Selected node is not a JSON Object. POJO generation requires an Object mapping.",
                        "Invalid Selection", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void performSearch() {
        String term = searchField.getText();
        if (term == null || term.isEmpty()) {
            searchResults.clear();
            currentSearchIndex = -1;
            return;
        }

        // If a new term is entered, do a deep search
        if (!term.equals(lastSearchTerm)) {
            lastSearchTerm = term;
            searchResults.clear();
            currentSearchIndex = -1;

            JsonNode root = (JsonNode) treeTableModel.getRoot();
            if (root != null) {
                TreePath path = new TreePath(root);
                dfsSearch(root, path, term.toLowerCase());
            }
        }

        if (searchResults.isEmpty()) {
            JOptionPane.showMessageDialog(borderPane, "No matches found for key: " + term, "Search Results",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // Cycle to next
        currentSearchIndex = (currentSearchIndex + 1) % searchResults.size();
        TreePath targetPath = searchResults.get(currentSearchIndex);

        // Force expansion of the path
        currentTreeTable.expandPath(targetPath);

        // Select row
        int viewRow = currentTreeTable.getRowForPath(targetPath);
        if (viewRow >= 0) {
            currentTreeTable.getSelectionModel().setSelectionInterval(viewRow, viewRow);
            currentTreeTable.scrollRectToVisible(currentTreeTable.getCellRect(viewRow, 0, true));
        }
    }

    private void dfsSearch(JsonNode node, TreePath currentPath, String term) {
        if (node.getKey() != null && node.getKey().toLowerCase().contains(term)) {
            searchResults.add(currentPath);
        }

        // Lazy load forced to inspect deepest paths dynamically
        for (int i = 0; i < node.getChildCount(); i++) {
            JsonNode child = (JsonNode) node.getChildAt(i);
            dfsSearch(child, currentPath.pathByAddingChild(child), term);
        }
    }

    private void exportJsonContent() {
        Object root = treeTableModel.getRoot();
        if (root instanceof JsonNode rootNode) {
            JsonElement rootElement = rootNode.getJsonElement();
            if (rootElement != null) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String formattedJson = gson.toJson(rootElement);
                jsonInputArea.setText(formattedJson);
                jsonInputArea.setCaretPosition(0);
                
                JOptionPane.showMessageDialog(borderPane,
                    "JSON exported to the text area successfully.", "Export Success", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
        JOptionPane.showMessageDialog(borderPane,
            "No valid JSON tree loaded to export.", "Export Warning", JOptionPane.WARNING_MESSAGE);
    }

    private void formatJsonContent() {
        String content = jsonInputArea.getText();
        if (content == null || content.trim().isEmpty()) {
            return;
        }
        try {
            JsonElement rootElement = JsonParser.parseString(content);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String formattedJson = gson.toJson(rootElement);
            jsonInputArea.setText(formattedJson);
            jsonInputArea.setCaretPosition(0);
        } catch (JsonSyntaxException ex) {
            JOptionPane.showMessageDialog(borderPane,
                    "Cannot format invalid JSON:\n" + ex.getMessage(), "Format Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(borderPane,
                    "Error formatting JSON:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void parseJsonContent() {
        String content = jsonInputArea.getText();
        if (content == null || content.trim().isEmpty()) {
            JOptionPane.showMessageDialog(borderPane, "Please enter a valid JSON payload.", "Empty Input",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            JsonElement rootElement = JsonParser.parseString(content);
            JsonNode rootNode = new JsonNode("ROOT", rootElement);
            treeTableModel.setRoot(rootNode);
            currentTreeTable.updateUI();

            // Clear searches
            searchResults.clear();
            currentSearchIndex = -1;
        } catch (JsonSyntaxException jse) {
            JOptionPane.showMessageDialog(borderPane, "Malformed JSON syntax:\n" + jse.getMessage(), "Syntax Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(borderPane, "Error parsing JSON payload:\n" + ex.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public String getLabel() {
        return "JSON Visualizer";
    }

    @Override
    public JComponent getView() {
        return borderPane;
    }
}
