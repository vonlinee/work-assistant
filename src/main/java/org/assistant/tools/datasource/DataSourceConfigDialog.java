package org.assistant.tools.datasource;

import org.assistant.ui.controls.Button;
import org.assistant.ui.controls.Label;
import org.assistant.ui.controls.TextField;
import org.assistant.ui.pane.HBox;
import org.assistant.ui.pane.VBox;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Dialog for adding or editing a DataSourceConfig.
 */
public class DataSourceConfigDialog extends JDialog {

    private static final String[] DB_TYPES = { "MySQL", "PostgreSQL", "SQLite", "Oracle", "SQL Server" };

    private final JComboBox<String> typeCombo = new JComboBox<>(DB_TYPES);
    private final TextField nameField = new TextField();
    private final TextField hostField = new TextField("localhost");
    private final TextField portField = new TextField();
    private final TextField databaseField = new TextField();
    private final TextField usernameField = new TextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final TextField jdbcUrlField = new TextField();
    private final TextField remarkField = new TextField();

    private DataSourceConfig result;

    public DataSourceConfigDialog(Frame owner, DataSourceConfig existing) {
        super(owner, existing == null ? "Add Datasource" : "Edit Datasource", true);
        setLayout(new BorderLayout());

        // --- Form panel ---
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(12, 12, 8, 12));
        GridBagConstraints lc = new GridBagConstraints();
        lc.anchor = GridBagConstraints.WEST;
        lc.insets = new Insets(4, 4, 4, 8);
        GridBagConstraints fc = new GridBagConstraints();
        fc.fill = GridBagConstraints.HORIZONTAL;
        fc.weightx = 1.0;
        fc.insets = new Insets(4, 0, 4, 4);
        fc.gridwidth = GridBagConstraints.REMAINDER;

        int row = 0;
        addRow(form, lc, fc, row++, "Name *:", nameField);
        addRow(form, lc, fc, row++, "Type:", typeCombo);
        addRow(form, lc, fc, row++, "Host:", hostField);
        addRow(form, lc, fc, row++, "Port:", portField);
        addRow(form, lc, fc, row++, "Database:", databaseField);
        addRow(form, lc, fc, row++, "Username:", usernameField);
        addRow(form, lc, fc, row++, "Password:", passwordField);

        // JDBC URL row with "Generate" button
        JPanel urlPanel = new JPanel(new BorderLayout(4, 0));
        urlPanel.add(jdbcUrlField, BorderLayout.CENTER);
        Button genBtn = new Button("Generate");
        genBtn.setToolTipText("Build JDBC URL from fields above");
        genBtn.addActionListener(e -> {
            String type = (String) typeCombo.getSelectedItem();
            String url = DataSourceConfig.buildJdbcUrl(type, hostField.getText().trim(),
                    portField.getText().trim(), databaseField.getText().trim());
            jdbcUrlField.setText(url);
        });
        urlPanel.add(genBtn, BorderLayout.EAST);
        addRow(form, lc, fc, row++, "JDBC URL:", urlPanel);
        addRow(form, lc, fc, row++, "Remark:", remarkField);

        add(new JScrollPane(form), BorderLayout.CENTER);

        // --- Buttons ---
        HBox btnBar = new HBox();
        btnBar.setBorder(BorderFactory.createEmptyBorder(8, 12, 12, 12));
        Button okBtn = new Button("OK");
        Button cancelBtn = new Button("Cancel");
        btnBar.add(Box.createHorizontalGlue());
        btnBar.add(okBtn);
        btnBar.add(Box.createRigidArea(new Dimension(8, 0)));
        btnBar.add(cancelBtn);
        add(btnBar, BorderLayout.SOUTH);

        // Auto-fill port when type changes
        typeCombo.addActionListener(e -> {
            String type = (String) typeCombo.getSelectedItem();
            if (portField.getText().isBlank()) {
                portField.setText(DataSourceConfig.defaultPort(type));
            }
        });

        okBtn.addActionListener(e -> onOk());
        cancelBtn.addActionListener(e -> dispose());

        // Pre-fill if editing
        if (existing != null) {
            nameField.setText(existing.getName());
            typeCombo.setSelectedItem(existing.getType());
            hostField.setText(existing.getHost());
            portField.setText(existing.getPort());
            databaseField.setText(existing.getDatabase());
            usernameField.setText(existing.getUsername());
            passwordField.setText(existing.getPassword());
            jdbcUrlField.setText(existing.getJdbcUrl());
            remarkField.setText(existing.getRemark());
        } else {
            // default port for default type
            portField.setText(DataSourceConfig.defaultPort((String) typeCombo.getSelectedItem()));
        }

        pack();
        setMinimumSize(new Dimension(520, 0));
        setLocationRelativeTo(owner);
    }

    private void addRow(JPanel form, GridBagConstraints lc, GridBagConstraints fc,
            int row, String label, JComponent field) {
        lc.gridy = row;
        fc.gridy = row;
        lc.gridx = 0;
        fc.gridx = 1;
        form.add(new Label(label), lc);
        form.add(field, fc);
    }

    private void onOk() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name is required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        result = new DataSourceConfig();
        result.setName(name);
        result.setType((String) typeCombo.getSelectedItem());
        result.setHost(hostField.getText().trim());
        result.setPort(portField.getText().trim());
        result.setDatabase(databaseField.getText().trim());
        result.setUsername(usernameField.getText().trim());
        result.setPassword(new String(passwordField.getPassword()));
        result.setJdbcUrl(jdbcUrlField.getText().trim());
        result.setRemark(remarkField.getText().trim());
        dispose();
    }

    /** Returns the filled-in config, or null if cancelled. */
    public DataSourceConfig getResult() {
        return result;
    }
}
