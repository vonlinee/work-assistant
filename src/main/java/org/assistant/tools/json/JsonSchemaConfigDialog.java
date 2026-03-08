package org.assistant.tools.json;

import com.google.gson.JsonElement;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

public class JsonSchemaConfigDialog extends JDialog {

    private static final String PREF_SCHEMA_TITLE = "schemaTitle";
    private static final String PREF_SCHEMA_ID = "schemaId";
    private static final String PREF_REQUIRE_ALL = "schemaRequireAll";
    private static final String PREF_ALLOW_ADDITIONAL = "schemaAllowAdditional";

    private JTextField titleField;
    private JTextField idField;
    private JCheckBox requireAllCheck;
    private JCheckBox allowAdditionalCheck;

    private final JsonElement selectedElement;

    public JsonSchemaConfigDialog(Frame owner, JsonElement selectedElement) {
        super(owner, "Configure JSON Schema Generation", true);
        this.selectedElement = selectedElement;

        initComponents();
        loadPreferences();
        pack();
        setLocationRelativeTo(owner);
    }

    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Schema Title
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Schema Title:"), gbc);
        titleField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(titleField, gbc);

        // Schema ID/URL
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Schema ID (URL):"), gbc);
        idField = new JTextField(20);
        gbc.gridx = 1;
        panel.add(idField, gbc);

        // Require all properties Checkbox
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        requireAllCheck = new JCheckBox("Require all properties by default");
        panel.add(requireAllCheck, gbc);

        // Allow additional properties Checkbox
        gbc.gridy = 3;
        allowAdditionalCheck = new JCheckBox("Allow additional properties");
        panel.add(allowAdditionalCheck, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton generateBtn = new JButton("Generate Schema");
        JButton cancelBtn = new JButton("Cancel");

        generateBtn.addActionListener(e -> {
            savePreferences();
            generateAndShow();
            dispose();
        });

        cancelBtn.addActionListener(e -> dispose());

        buttonPanel.add(generateBtn);
        buttonPanel.add(cancelBtn);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(JsonSchemaConfigDialog.class);
        titleField.setText(prefs.get(PREF_SCHEMA_TITLE, "Root Data"));
        idField.setText(prefs.get(PREF_SCHEMA_ID, "http://example.com/schema.json"));
        requireAllCheck.setSelected(prefs.getBoolean(PREF_REQUIRE_ALL, true));
        allowAdditionalCheck.setSelected(prefs.getBoolean(PREF_ALLOW_ADDITIONAL, false));
    }

    private void savePreferences() {
        Preferences prefs = Preferences.userNodeForPackage(JsonSchemaConfigDialog.class);
        prefs.put(PREF_SCHEMA_TITLE, titleField.getText().trim());
        prefs.put(PREF_SCHEMA_ID, idField.getText().trim());
        prefs.putBoolean(PREF_REQUIRE_ALL, requireAllCheck.isSelected());
        prefs.putBoolean(PREF_ALLOW_ADDITIONAL, allowAdditionalCheck.isSelected());
    }

    private void generateAndShow() {
        JsonToSchemaGenerator generator = new JsonToSchemaGenerator(
                titleField.getText().trim(),
                idField.getText().trim(),
                requireAllCheck.isSelected(),
                allowAdditionalCheck.isSelected());

        String result = generator.generate(selectedElement);

        JsonSchemaResultDialog resultDialog = new JsonSchemaResultDialog((Frame) getOwner(), result);
        resultDialog.setVisible(true);
    }
}
