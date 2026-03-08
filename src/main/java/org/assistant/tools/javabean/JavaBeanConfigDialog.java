package org.assistant.tools.javabean;

import javax.swing.*;
import java.awt.*;
import java.util.prefs.Preferences;

/**
 * Dialog to configure the JavaBean code/mock-JSON generator behaviour.
 */
public class JavaBeanConfigDialog extends JDialog {

    // ── Preference keys ───────────────────────────────────────────────────────
    private static final String PREF_VAR_NAME = "jbVarName";
    private static final String PREF_USE_SETTERS = "jbUseSetters";
    private static final String PREF_STR_LEN = "jbStringLength";
    private static final String PREF_COLL_SIZE = "jbCollectionSize";
    private static final String PREF_MAX_DEPTH = "jbMaxDepth";
    private static final String PREF_RANDOM_NUMS = "jbRandomNumbers";
    private static final String PREF_NULL_BEYOND_DEPTH = "jbNullBeyondDepth";

    // ── UI Controls ──────────────────────────────────────────────────────────
    private JTextField varNameField;
    private JRadioButton setterRadio;
    private JRadioButton builderRadio;
    private JRadioButton fieldRadio;
    private JSpinner strLenSpinner;
    private JSpinner collSizeSpinner;
    private JSpinner maxDepthSpinner;
    private JCheckBox randomNumbersCheck;
    private JCheckBox nullBeyondDepthCheck;

    private final JavaBeanConfig config;
    private boolean confirmed = false;

    public JavaBeanConfigDialog(Frame owner, JavaBeanConfig config) {
        super(owner, "Java Bean Generator – Settings", true);
        this.config = config;
        initComponents();
        loadPreferences();
        applyToConfig(); // sync UI → config right away
        pack();
        setMinimumSize(new Dimension(380, 320));
        setLocationRelativeTo(owner);
    }

    // ── Build UI ─────────────────────────────────────────────────────────────

    private void initComponents() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        // ── Object Code Section ──────────────────────────────────────────────
        addSectionLabel(panel, gbc, row++, "Object-Creation Code");

        addLabel(panel, gbc, row, "Root variable name:");
        varNameField = new JTextField(10);
        addControl(panel, gbc, row++, varNameField);

        addLabel(panel, gbc, row, "Assignment style:");
        JPanel stylePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        setterRadio = new JRadioButton("Setters");
        builderRadio = new JRadioButton("Builder");
        fieldRadio = new JRadioButton("Direct field");
        ButtonGroup styleGroup = new ButtonGroup();
        styleGroup.add(setterRadio);
        styleGroup.add(builderRadio);
        styleGroup.add(fieldRadio);
        stylePanel.add(setterRadio);
        stylePanel.add(builderRadio);
        stylePanel.add(fieldRadio);
        gbc.gridx = 1;
        gbc.gridy = row++;
        panel.add(stylePanel, gbc);

        // ── Mock Data Section ─────────────────────────────────────────────────
        addSectionLabel(panel, gbc, row++, "Mock Data");

        addLabel(panel, gbc, row, "String value length:");
        strLenSpinner = new JSpinner(new SpinnerNumberModel(8, 1, 64, 1));
        addControl(panel, gbc, row++, strLenSpinner);

        addLabel(panel, gbc, row, "Collection / array size:");
        collSizeSpinner = new JSpinner(new SpinnerNumberModel(2, 1, 20, 1));
        addControl(panel, gbc, row++, collSizeSpinner);

        addLabel(panel, gbc, row, "Max nesting depth:");
        maxDepthSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));
        addControl(panel, gbc, row++, maxDepthSpinner);

        randomNumbersCheck = new JCheckBox("Use random numeric values");
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        panel.add(randomNumbersCheck, gbc);
        gbc.gridwidth = 1;

        nullBeyondDepthCheck = new JCheckBox("Emit null for complex types beyond max depth");
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.gridwidth = 2;
        panel.add(nullBeyondDepthCheck, gbc);
        gbc.gridwidth = 1;

        // ── Buttons ───────────────────────────────────────────────────────────
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okBtn = new JButton("Apply");
        JButton cancelBtn = new JButton("Cancel");

        okBtn.addActionListener(e -> {
            savePreferences();
            applyToConfig();
            confirmed = true;
            dispose();
        });
        cancelBtn.addActionListener(e -> dispose());

        btnPanel.add(okBtn);
        btnPanel.add(cancelBtn);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(panel, BorderLayout.CENTER);
        getContentPane().add(btnPanel, BorderLayout.SOUTH);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void addSectionLabel(JPanel p, GridBagConstraints gbc, int row, String text) {
        JLabel lbl = new JLabel("── " + text + " ──");
        lbl.setFont(lbl.getFont().deriveFont(Font.BOLD));
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        p.add(lbl, gbc);
        gbc.gridwidth = 1;
    }

    private void addLabel(JPanel p, GridBagConstraints gbc, int row, String text) {
        gbc.gridx = 0;
        gbc.gridy = row;
        p.add(new JLabel(text), gbc);
    }

    private void addControl(JPanel p, GridBagConstraints gbc, int row, JComponent c) {
        gbc.gridx = 1;
        gbc.gridy = row;
        p.add(c, gbc);
    }

    // ── Preferences persistence ───────────────────────────────────────────────

    private void loadPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(JavaBeanConfigDialog.class);
        varNameField.setText(prefs.get(PREF_VAR_NAME, "obj"));

        String style = prefs.get(PREF_USE_SETTERS, "setter");
        if ("builder".equals(style))
            builderRadio.setSelected(true);
        else if ("field".equals(style))
            fieldRadio.setSelected(true);
        else
            setterRadio.setSelected(true);

        strLenSpinner.setValue(prefs.getInt(PREF_STR_LEN, 8));
        collSizeSpinner.setValue(prefs.getInt(PREF_COLL_SIZE, 2));
        maxDepthSpinner.setValue(prefs.getInt(PREF_MAX_DEPTH, 5));
        randomNumbersCheck.setSelected(prefs.getBoolean(PREF_RANDOM_NUMS, false));
        nullBeyondDepthCheck.setSelected(prefs.getBoolean(PREF_NULL_BEYOND_DEPTH, true));
    }

    private void savePreferences() {
        Preferences prefs = Preferences.userNodeForPackage(JavaBeanConfigDialog.class);
        prefs.put(PREF_VAR_NAME, varNameField.getText().trim());

        String style = setterRadio.isSelected() ? "setter" : (builderRadio.isSelected() ? "builder" : "field");
        prefs.put(PREF_USE_SETTERS, style);

        prefs.putInt(PREF_STR_LEN, (int) strLenSpinner.getValue());
        prefs.putInt(PREF_COLL_SIZE, (int) collSizeSpinner.getValue());
        prefs.putInt(PREF_MAX_DEPTH, (int) maxDepthSpinner.getValue());
        prefs.putBoolean(PREF_RANDOM_NUMS, randomNumbersCheck.isSelected());
        prefs.putBoolean(PREF_NULL_BEYOND_DEPTH, nullBeyondDepthCheck.isSelected());
    }

    private void applyToConfig() {
        String varName = varNameField.getText().trim();
        config.setVarName(varName.isEmpty() ? "obj" : varName);
        config.setUseSetters(setterRadio.isSelected() || builderRadio.isSelected());
        config.setUseBuilder(builderRadio.isSelected());
        config.setMockStringLength((int) strLenSpinner.getValue());
        config.setMockCollectionSize((int) collSizeSpinner.getValue());
        config.setMaxDepth((int) maxDepthSpinner.getValue());
        config.setRandomNumbers(randomNumbersCheck.isSelected());
        config.setNullBeyondMaxDepth(nullBeyondDepthCheck.isSelected());
    }

    /** Returns true when the user clicked "Apply". */
    public boolean isConfirmed() {
        return confirmed;
    }
}
