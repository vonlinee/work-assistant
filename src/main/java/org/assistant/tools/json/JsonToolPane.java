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

    private SwingTreeTable currentTreeTable;
    private JsonTreeTableModel treeTableModel;

    private JTextField searchField;
    private JButton searchButton;
    private JButton configPojoButton;

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
    }

    private void layoutComponents() {
        // Left Panel (Input)
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Raw JSON Payload"));
        leftPanel.add(new RTextScrollPane(jsonInputArea), BorderLayout.CENTER);

        JPanel leftBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        leftBottom.add(loadFileButton);
        leftBottom.add(formatButton);
        leftBottom.add(parseButton);
        leftPanel.add(leftBottom, BorderLayout.SOUTH);

        // Top Search / Action Bar over the Tree
        JPanel topActionBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topActionBar.add(new JLabel("Search Node Key:"));
        topActionBar.add(searchField);
        topActionBar.add(searchButton);
        topActionBar.add(Box.createHorizontalStrut(50));
        topActionBar.add(configPojoButton);

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
        formatButton.addActionListener(e -> formatJsonContent());
        parseButton.addActionListener(e -> parseJsonContent());

        searchButton.addActionListener(e -> performSearch());
        searchField.addActionListener(e -> performSearch()); // Enter key trigger

        configPojoButton.addActionListener(e -> configureAndGeneratePojo());
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

    private void configureAndGeneratePojo() {
        int viewRow = currentTreeTable.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(borderPane, "Please select a JSON Object node from the tree first.",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = currentTreeTable.convertRowIndexToModel(viewRow);
        TreePath path = currentTreeTable.getPathForRow(modelRow);
        if (path != null) {
            Object node = path.getLastPathComponent();
            if (node instanceof JsonNode jsonNode) {
                JsonElement element = jsonNode.getJsonElement();
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
