package org.assistant.tools;

import org.assistant.tools.util.TemplateManager;
import org.assistant.ui.controls.Button;
import org.assistant.ui.controls.Label;
import org.assistant.ui.controls.TextField;
import org.assistant.ui.pane.BorderPane;
import org.assistant.ui.pane.HBox;
import org.assistant.ui.pane.VBox;
import org.assistant.ui.pane.ScrollPane;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;

public class TemplateToolPane extends BorderPane implements ToolProvider {

    private final DefaultListModel<String> listModel;
    private final JList<String> templateList;
    private final TextField searchField;
    private final RSyntaxTextArea textArea;
    private final Label statusLabel;

    private String currentTemplate = null;

    public TemplateToolPane() {
        // --- Left Panel: Template List ---
        VBox leftPane = new VBox();
        leftPane.setPreferredSize(new Dimension(250, 0));

        // Search bar
        HBox searchBox = new HBox();
        searchBox.add(new Label("Search:"));
        searchField = new TextField("");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                filterList();
            }

            public void removeUpdate(DocumentEvent e) {
                filterList();
            }

            public void changedUpdate(DocumentEvent e) {
                filterList();
            }
        });
        searchBox.add(searchField);
        leftPane.add(searchBox, BorderLayout.NORTH);

        // List
        listModel = new DefaultListModel<>();
        templateList = new JList<>(listModel);
        templateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        templateList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedTemplate();
            }
        });

        // Custom cell renderer to mark custom overrides
        templateList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
                    boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,
                        cellHasFocus);
                String name = (String) value;
                if (TemplateManager.isCustomized(name)) {
                    label.setText(name + " [Customized]");
                    label.setForeground(new Color(0, 100, 0));
                }
                return label;
            }
        });

        ScrollPane listScroll = new ScrollPane();
        listScroll.setViewportView(templateList);
        leftPane.add(listScroll, BorderLayout.CENTER);

        // Buttons
        HBox listButtons = new HBox();
        Button addBtn = new Button("+ Add");
        addBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                addNewTemplate();
            }
        });
        Button removeBtn = new Button("- Remove");
        removeBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                removeSelectedTemplate();
            }
        });
        listButtons.add(addBtn);
        listButtons.add(removeBtn);
        leftPane.add(listButtons, BorderLayout.SOUTH);

        setLeft(leftPane);

        // --- Right Panel: Editor ---
        BorderPane rightPane = new BorderPane();

        // Editor
        textArea = new RSyntaxTextArea(20, 60);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML); // Velocity syntax not natively supported,
                                                                           // HTML is closest
        textArea.setCodeFoldingEnabled(true);
        RTextScrollPane editorScroll = new RTextScrollPane(textArea);
        rightPane.setCenter(editorScroll);

        // Save bar
        HBox saveBar = new HBox();
        Button saveBtn = new Button("Save Template");
        saveBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                saveCurrentTemplate();
            }
        });
        statusLabel = new Label(" ");
        saveBar.add(saveBtn);
        saveBar.add(statusLabel);
        rightPane.setBottom(saveBar);

        setCenter(rightPane);

        // Initialize Data
        refreshList();
    }

    private void refreshList() {
        String filter = searchField.getText().toLowerCase();
        List<String> templates = TemplateManager.getAllAvailableTemplates();

        String previouslySelected = templateList.getSelectedValue();

        listModel.clear();
        for (String t : templates) {
            if (filter.isEmpty() || t.toLowerCase().contains(filter)) {
                listModel.addElement(t);
            }
        }

        if (previouslySelected != null && listModel.contains(previouslySelected)) {
            templateList.setSelectedValue(previouslySelected, true);
        } else if (!listModel.isEmpty()) {
            templateList.setSelectedIndex(0);
        }
    }

    private void filterList() {
        refreshList();
    }

    private void loadSelectedTemplate() {
        String selected = templateList.getSelectedValue();
        if (selected == null) {
            textArea.setText("");
            currentTemplate = null;
            statusLabel.setText("");
            return;
        }

        try {
            String content = TemplateManager.readTemplate(selected);
            textArea.setText(content);
            textArea.setCaretPosition(0);
            currentTemplate = selected;
            statusLabel.setText("Loaded: " + selected);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to load template: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveCurrentTemplate() {
        if (currentTemplate == null) {
            return;
        }
        try {
            TemplateManager.saveTemplate(currentTemplate, textArea.getText());
            statusLabel.setText("Saved: " + currentTemplate + " (Customized)");
            refreshList(); // Update UI to show [Customized]
            JOptionPane.showMessageDialog(this, "Template saved successfully!", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save template: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addNewTemplate() {
        String name = JOptionPane.showInputDialog(this, "Enter new template name (e.g., api-custom.vm):",
                "New Template", JOptionPane.PLAIN_MESSAGE);
        if (name != null && !name.trim().isEmpty()) {
            name = name.trim();
            if (!name.endsWith(".vm")) {
                name += ".vm";
            }
            if (TemplateManager.getAllAvailableTemplates().contains(name)) {
                JOptionPane.showMessageDialog(this, "Template already exists!", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                TemplateManager.saveTemplate(name, "## New Template\n");
                searchField.setText(""); // clear search to ensure it's visible
                refreshList();
                templateList.setSelectedValue(name, true);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "Failed to create template: " + e.getMessage(), "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void removeSelectedTemplate() {
        String selected = templateList.getSelectedValue();
        if (selected == null) {
            return;
        }

        if (!TemplateManager.isCustomized(selected)) {
            JOptionPane.showMessageDialog(this,
                    "Cannot delete default classpath template. You can only discard custom overrides.", "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete/discard custom template: " + selected + "?", "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            if (TemplateManager.deleteCustomTemplate(selected)) {
                statusLabel.setText("Deleted custom template: " + selected);
                refreshList();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete template file.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    @Override
    public String getLabel() {
        return "Template Manager";
    }

    @Override
    public JComponent getView() {
        return this;
    }
}
