package org.assistant.tools.crud;

import org.assistant.tools.util.TemplateManager;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Swing dialog for editing {@link CrudGeneratorConfig}.
 */
public class CrudGeneratorConfigDialog extends JDialog {

    private static final String NODE = "crud-generator";

    private JTextField basePackageField;
    private JTextField moduleNameField;
    private JTextField authorField;
    private JTextField tablePrefixField;
    private JCheckBox lombokCheck;
    private JCheckBox swaggerCheck;
    private JTextField entitySuperField;
    private JTextField controllerSuperField;
    private JTextField outputDirField;
    private JComboBox<String> entityTemplateCombo;
    private JComboBox<String> mapperJavaTemplateCombo;
    private JComboBox<String> mapperXmlTemplateCombo;
    private JComboBox<String> serviceTemplateCombo;
    private JComboBox<String> serviceImplTemplateCombo;
    private JComboBox<String> controllerTemplateCombo;
    private JComboBox<String> sqlTemplateCombo;

    private final CrudGeneratorConfig config;
    private boolean confirmed;

    public CrudGeneratorConfigDialog(Frame owner, CrudGeneratorConfig config) {
        super(owner, "CRUD Generator – Settings", true);
        this.config = config;
        initUI();
        loadFromConfig();
        pack();
        setMinimumSize(new Dimension(480, 340));
        setLocationRelativeTo(owner);
    }

    // ── Build UI ──────────────────────────────────────────────────────────────

    private void initUI() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 12, 4, 12));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 0;

        int row = 0;

        // ── Project ──────────────────────────────────────────────────────────
        addSection(panel, gbc, row++, "Project Settings");
        basePackageField = addField(panel, gbc, row++, "Base package:");
        moduleNameField = addField(panel, gbc, row++, "Module name (optional):");
        authorField = addField(panel, gbc, row++, "Author:");
        tablePrefixField = addField(panel, gbc, row++, "Strip table prefix(es):");
        outputDirField = addField(panel, gbc, row++, "Output directory:");

        // ── Code style ───────────────────────────────────────────────────────
        addSection(panel, gbc, row++, "Code Style");
        lombokCheck = addCheck(panel, gbc, row++, "Use Lombok (@Data, @Builder…)");
        swaggerCheck = addCheck(panel, gbc, row++, "Use Swagger annotations (@ApiModel…)");

        // ── Superclasses ─────────────────────────────────────────────────────
        addSection(panel, gbc, row++, "Superclasses (leave blank for none)");
        entitySuperField = addField(panel, gbc, row++, "Entity superclass:");
        controllerSuperField = addField(panel, gbc, row++, "Controller superclass:");

        // ── Template Selection ───────────────────────────────────────────────
        addSection(panel, gbc, row++, "Template Selection");
        List<String> allTemplates = TemplateManager.getAllAvailableTemplates();
        String[] templatesArray = allTemplates.toArray(new String[0]);

        entityTemplateCombo = addCombo(panel, gbc, row++, "Entity template:", templatesArray);
        mapperJavaTemplateCombo = addCombo(panel, gbc, row++, "Mapper interface:", templatesArray);
        mapperXmlTemplateCombo = addCombo(panel, gbc, row++, "Mapper XML:", templatesArray);
        serviceTemplateCombo = addCombo(panel, gbc, row++, "Service interface:", templatesArray);
        serviceImplTemplateCombo = addCombo(panel, gbc, row++, "Service impl:", templatesArray);
        controllerTemplateCombo = addCombo(panel, gbc, row++, "Controller template:", templatesArray);
        sqlTemplateCombo = addCombo(panel, gbc, row++, "SQL template:", templatesArray);

        // ── Buttons ───────────────────────────────────────────────────────────
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton ok = new JButton("Apply");
        JButton cancel = new JButton("Cancel");
        ok.addActionListener(e -> {
            applyToConfig();
            savePrefs();
            confirmed = true;
            dispose();
        });
        cancel.addActionListener(e -> dispose());
        btnPanel.add(ok);
        btnPanel.add(cancel);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(btnPanel, BorderLayout.SOUTH);
    }

    // ── Layout helpers ────────────────────────────────────────────────────────

    private void addSection(JPanel p, GridBagConstraints gbc, int row, String text) {
        JLabel lbl = new JLabel("── " + text + " ──");
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        p.add(lbl, gbc);
        gbc.gridwidth = 1;
    }

    private JTextField addField(JPanel p, GridBagConstraints gbc, int row, String label) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        p.add(new JLabel(label), gbc);
        JTextField tf = new JTextField(22);
        gbc.gridx = 1;
        gbc.weightx = 1;
        p.add(tf, gbc);
        gbc.weightx = 0;
        return tf;
    }

    private JCheckBox addCheck(JPanel p, GridBagConstraints gbc, int row, String label) {
        JCheckBox cb = new JCheckBox(label);
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        p.add(cb, gbc);
        gbc.gridwidth = 1;
        return cb;
    }

    private JComboBox<String> addCombo(JPanel p, GridBagConstraints gbc, int row, String label, String[] items) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        p.add(new JLabel(label), gbc);
        JComboBox<String> combo = new JComboBox<>(items);
        gbc.gridx = 1;
        gbc.weightx = 1;
        p.add(combo, gbc);
        gbc.weightx = 0;
        return combo;
    }

    // ── Config ↔ UI sync ─────────────────────────────────────────────────────

    private void loadFromConfig() {
        Preferences prefs = Preferences.userRoot().node(NODE);
        basePackageField.setText(prefs.get("basePackage", config.getBasePackage()));
        moduleNameField.setText(prefs.get("moduleName", config.getModuleName()));
        authorField.setText(prefs.get("author", config.getAuthor()));
        tablePrefixField.setText(prefs.get("tablePrefix", config.getTablePrefix()));
        outputDirField.setText(prefs.get("outputDir", config.getOutputBaseDir()));
        lombokCheck.setSelected(prefs.getBoolean("useLombok", config.isUseLombok()));
        swaggerCheck.setSelected(prefs.getBoolean("useSwagger", config.isUseSwagger()));
        entitySuperField.setText(prefs.get("entitySuper", config.getEntitySuperClass()));
        controllerSuperField.setText(prefs.get("ctrlSuper", config.getControllerSuperClass()));

        entityTemplateCombo.setSelectedItem(prefs.get("entityTpl", config.getEntityTemplate()));
        mapperJavaTemplateCombo.setSelectedItem(prefs.get("mapperJavaTpl", config.getMapperJavaTemplate()));
        mapperXmlTemplateCombo.setSelectedItem(prefs.get("mapperXmlTpl", config.getMapperXmlTemplate()));
        serviceTemplateCombo.setSelectedItem(prefs.get("serviceTpl", config.getServiceTemplate()));
        serviceImplTemplateCombo.setSelectedItem(prefs.get("serviceImplTpl", config.getServiceImplTemplate()));
        controllerTemplateCombo.setSelectedItem(prefs.get("controllerTpl", config.getControllerTemplate()));
        sqlTemplateCombo.setSelectedItem(prefs.get("sqlTpl", config.getSqlTemplate()));
    }

    private void applyToConfig() {
        config.setBasePackage(basePackageField.getText().trim());
        config.setModuleName(moduleNameField.getText().trim());
        config.setAuthor(authorField.getText().trim());
        config.setTablePrefix(tablePrefixField.getText().trim());
        config.setOutputBaseDir(outputDirField.getText().trim());
        config.setUseLombok(lombokCheck.isSelected());
        config.setUseSwagger(swaggerCheck.isSelected());
        config.setEntitySuperClass(entitySuperField.getText().trim());
        config.setControllerSuperClass(controllerSuperField.getText().trim());

        config.setEntityTemplate((String) entityTemplateCombo.getSelectedItem());
        config.setMapperJavaTemplate((String) mapperJavaTemplateCombo.getSelectedItem());
        config.setMapperXmlTemplate((String) mapperXmlTemplateCombo.getSelectedItem());
        config.setServiceTemplate((String) serviceTemplateCombo.getSelectedItem());
        config.setServiceImplTemplate((String) serviceImplTemplateCombo.getSelectedItem());
        config.setControllerTemplate((String) controllerTemplateCombo.getSelectedItem());
        config.setSqlTemplate((String) sqlTemplateCombo.getSelectedItem());
    }

    private void savePrefs() {
        Preferences prefs = Preferences.userRoot().node(NODE);
        prefs.put("basePackage", config.getBasePackage());
        prefs.put("moduleName", config.getModuleName());
        prefs.put("author", config.getAuthor());
        prefs.put("tablePrefix", config.getTablePrefix());
        prefs.put("outputDir", config.getOutputBaseDir());
        prefs.putBoolean("useLombok", config.isUseLombok());
        prefs.putBoolean("useSwagger", config.isUseSwagger());
        prefs.put("entitySuper", config.getEntitySuperClass());
        prefs.put("ctrlSuper", config.getControllerSuperClass());

        prefs.put("entityTpl", config.getEntityTemplate());
        prefs.put("mapperJavaTpl", config.getMapperJavaTemplate());
        prefs.put("mapperXmlTpl", config.getMapperXmlTemplate());
        prefs.put("serviceTpl", config.getServiceTemplate());
        prefs.put("serviceImplTpl", config.getServiceImplTemplate());
        prefs.put("controllerTpl", config.getControllerTemplate());
        prefs.put("sqlTpl", config.getSqlTemplate());
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public CrudGeneratorConfig getConfig() {
        return config;
    }
}
