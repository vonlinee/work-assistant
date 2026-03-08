package org.assistant.tools.json;

import com.google.gson.JsonElement;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

public class PojoConfigDialog extends JDialog {

    private static final String PREF_CLASS_NAME = "pojoClassName";
    private static final String PREF_PACKAGE = "pojoPackage";
    private static final String PREF_LOMBOK = "pojoLombok";
    private static final String PREF_CAMEL_CASE = "pojoCamelCase";

    private JTextField classNameField;
    private JTextField packageField;
    private JCheckBox lombokCheck;
    private JCheckBox camelCaseCheck;

    private final JsonElement selectedElement;

    public PojoConfigDialog(Frame owner, JsonElement selectedElement) {
        super(owner, "Configure POJO Generation", true);
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

        // Class Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Root Class Name:"), gbc);
        classNameField = new JTextField(15);
        gbc.gridx = 1;
        panel.add(classNameField, gbc);

        // Package Name
        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(new JLabel("Target Package:"), gbc);
        packageField = new JTextField(15);
        gbc.gridx = 1;
        panel.add(packageField, gbc);

        // Lombok Checkbox
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        lombokCheck = new JCheckBox("Use Lombok (@Data, @NoArgsConstructor)");
        panel.add(lombokCheck, gbc);

        // CamelCase Checkbox
        gbc.gridy = 3;
        camelCaseCheck = new JCheckBox("Convert keys to camelCase");
        panel.add(camelCaseCheck, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton generateBtn = new JButton("Generate POJO");
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
        Preferences prefs = Preferences.userNodeForPackage(PojoConfigDialog.class);
        classNameField.setText(prefs.get(PREF_CLASS_NAME, "RootResponse"));
        packageField.setText(prefs.get(PREF_PACKAGE, "com.example.dto"));
        lombokCheck.setSelected(prefs.getBoolean(PREF_LOMBOK, true));
        camelCaseCheck.setSelected(prefs.getBoolean(PREF_CAMEL_CASE, true));
    }

    private void savePreferences() {
        Preferences prefs = Preferences.userNodeForPackage(PojoConfigDialog.class);
        prefs.put(PREF_CLASS_NAME, classNameField.getText().trim());
        prefs.put(PREF_PACKAGE, packageField.getText().trim());
        prefs.putBoolean(PREF_LOMBOK, lombokCheck.isSelected());
        prefs.putBoolean(PREF_CAMEL_CASE, camelCaseCheck.isSelected());
    }

    private void generateAndShow() {
        JsonToPojoGenerator generator = new JsonToPojoGenerator(
                classNameField.getText().trim(),
                packageField.getText().trim(),
                lombokCheck.isSelected(),
                camelCaseCheck.isSelected());

        String result = generator.generate(selectedElement);

        org.assistant.tools.json.PojoResultDialog resultDialog = new org.assistant.tools.json.PojoResultDialog(
                (Frame) getOwner(), result);
        resultDialog.setVisible(true);
    }
}
