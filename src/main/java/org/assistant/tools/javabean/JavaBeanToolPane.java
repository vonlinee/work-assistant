package org.assistant.tools.javabean;

import org.assistant.tools.ToolProvider;
import org.assistant.ui.pane.BorderPane;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Tool pane that parses a Java Bean source snippet and generates either:
 * <ul>
 * <li>Java object-creation code (new + setters)</li>
 * <li>A mock JSON document that can be deserialized back to the bean</li>
 * </ul>
 *
 * Automatically registered as a tab via the {@code ToolCollectionPane}
 * reflection scan.
 */
public class JavaBeanToolPane implements ToolProvider {

    private final BorderPane rootPane;

    // ── Input ──────────────────────────────────────────────────────────────
    private RSyntaxTextArea inputArea;

    // ── Output panels (inside a JTabbedPane) ───────────────────────────────
    private RSyntaxTextArea objectCodeArea;
    private RSyntaxTextArea mockJsonArea;
    private JTabbedPane outputTabs;

    // ── Toolbar buttons ────────────────────────────────────────────────────
    private JButton genObjectBtn;
    private JButton genJsonBtn;
    private JButton settingsBtn;
    private JLabel statusLabel;

    // ── State ──────────────────────────────────────────────────────────────
    private final JavaBeanConfig config = new JavaBeanConfig();
    /** Last successfully parsed class map (key = simple class name). */
    private Map<String, JavaBeanParser.ClassInfo> lastParsed = null;

    public JavaBeanToolPane() {
        rootPane = new BorderPane();
        initComponents();
        layoutComponents();
        setupListeners();
    }

    // ── Build UI ──────────────────────────────────────────────────────────

    private void initComponents() {
        // Input area – Java syntax highlighting
        inputArea = new RSyntaxTextArea(24, 50);
        inputArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        inputArea.setCodeFoldingEnabled(true);
        inputArea.setToolTipText("Paste one or more Java Bean class definitions here");
        inputArea.setText(buildPlaceholder());

        // Output: object creation code
        objectCodeArea = new RSyntaxTextArea(24, 50);
        objectCodeArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        objectCodeArea.setEditable(false);
        objectCodeArea.setCodeFoldingEnabled(true);

        // Output: mock JSON
        mockJsonArea = new RSyntaxTextArea(24, 50);
        mockJsonArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
        mockJsonArea.setEditable(false);
        mockJsonArea.setCodeFoldingEnabled(true);

        outputTabs = new JTabbedPane();

        // Toolbar
        genObjectBtn = new JButton("⚙ Generate Code");
        genObjectBtn.setToolTipText("Generate Java object-creation code (new + setters)");
        genJsonBtn = new JButton("{ } Generate JSON");
        genJsonBtn.setToolTipText("Generate mock JSON that can be deserialized to the bean");
        settingsBtn = new JButton("⚙ Settings");
        settingsBtn.setToolTipText("Configure generator behaviour");

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.GRAY);
    }

    private void layoutComponents() {
        // ── Left: Input ───────────────────────────────────────────────────
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Java Bean Source Code"));
        leftPanel.add(new RTextScrollPane(inputArea), BorderLayout.CENTER);

        JPanel inputButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        inputButtons.add(new JButton("Clear") {
            {
                addActionListener(e -> inputArea.setText(""));
            }
        });
        leftPanel.add(inputButtons, BorderLayout.SOUTH);

        // ── Right: Output tabs ────────────────────────────────────────────
        outputTabs.addTab("Object Code  (Java)", new RTextScrollPane(objectCodeArea));
        outputTabs.addTab("Mock JSON", new RTextScrollPane(mockJsonArea));

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setBorder(BorderFactory.createTitledBorder("Generated Output"));

        JPanel outputButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 4));
        outputButtons.add(new JButton("Copy") {
            {
                addActionListener(e -> copyActiveOutput());
            }
        });
        rightPanel.add(outputTabs, BorderLayout.CENTER);
        rightPanel.add(outputButtons, BorderLayout.SOUTH);

        // ── Split ─────────────────────────────────────────────────────────
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        split.setResizeWeight(0.45);
        split.setOneTouchExpandable(true);

        // ── Toolbar ───────────────────────────────────────────────────────
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        toolbar.setBorder(BorderFactory.createEtchedBorder());
        toolbar.add(genObjectBtn);
        toolbar.add(genJsonBtn);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(settingsBtn);
        toolbar.add(Box.createHorizontalStrut(12));
        toolbar.add(statusLabel);

        rootPane.add(toolbar, BorderLayout.NORTH);
        rootPane.add(split, BorderLayout.CENTER);
    }

    private void setupListeners() {
        genObjectBtn.addActionListener(e -> generateObjectCode());
        genJsonBtn.addActionListener(e -> generateMockJson());
        settingsBtn.addActionListener(e -> openSettings());
    }

    // ── Actions ───────────────────────────────────────────────────────────

    private void parseInput() {
        String src = inputArea.getText();
        if (src == null || src.isBlank()) {
            showError("Please paste at least one Java Bean class definition.");
            lastParsed = null;
            return;
        }
        lastParsed = JavaBeanParser.parse(src);
        if (lastParsed.isEmpty()) {
            showError("No class declarations found in the input.");
            lastParsed = null;
        }
    }

    private void generateObjectCode() {
        try {
            parseInput();
            if (lastParsed == null)
                return;

            // Use the first class as root
            JavaBeanParser.ClassInfo primary = lastParsed.values().iterator().next();
            ObjectCreationGenerator gen = new ObjectCreationGenerator(config, lastParsed);
            String code = gen.generate(primary);

            objectCodeArea.setText(code);
            objectCodeArea.setCaretPosition(0);
            outputTabs.setSelectedIndex(0);
            showStatus("✓ Object code generated for class: " + primary.getClassName());
        } catch (IllegalArgumentException ex) {
            showError("Parse error: " + ex.getMessage());
        } catch (Exception ex) {
            showError("Unexpected error: " + ex.getMessage());
        }
    }

    private void generateMockJson() {
        try {
            parseInput();
            if (lastParsed == null)
                return;

            JavaBeanParser.ClassInfo primary = lastParsed.values().iterator().next();
            MockJsonGenerator gen = new MockJsonGenerator(config, lastParsed);
            String json = gen.generate(primary);

            mockJsonArea.setText(json);
            mockJsonArea.setCaretPosition(0);
            outputTabs.setSelectedIndex(1);
            showStatus("✓ Mock JSON generated for class: " + primary.getClassName());
        } catch (IllegalArgumentException ex) {
            showError("Parse error: " + ex.getMessage());
        } catch (Exception ex) {
            showError("Unexpected error: " + ex.getMessage());
        }
    }

    private void openSettings() {
        Window parent = SwingUtilities.getWindowAncestor(rootPane);
        Frame frame = (parent instanceof Frame f) ? f
                : (parent != null ? (Frame) SwingUtilities.getWindowAncestor(parent) : null);
        JavaBeanConfigDialog dlg = new JavaBeanConfigDialog(frame, config);
        dlg.setVisible(true);
        if (dlg.isConfirmed()) {
            showStatus("Settings applied.");
        }
    }

    private void copyActiveOutput() {
        int idx = outputTabs.getSelectedIndex();
        String text = idx == 0 ? objectCodeArea.getText() : mockJsonArea.getText();
        if (text != null && !text.isBlank()) {
            Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new java.awt.datatransfer.StringSelection(text), null);
            showStatus("Copied to clipboard.");
        }
    }

    // ── Status helpers ────────────────────────────────────────────────────

    private void showStatus(String msg) {
        statusLabel.setForeground(new Color(0, 120, 0));
        statusLabel.setText(msg);
    }

    private void showError(String msg) {
        statusLabel.setForeground(Color.RED);
        statusLabel.setText("✗ " + msg);
        JOptionPane.showMessageDialog(rootPane, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ── Placeholder example ───────────────────────────────────────────────

    private static String buildPlaceholder() {
        return """
                import java.util.List;
                import java.math.BigDecimal;

                public class Order {
                    private Long id;
                    private String orderNo;
                    private BigDecimal totalAmount;
                    private boolean paid;
                    private Customer customer;
                    private List<OrderItem> items;
                }

                public class Customer {
                    private Long id;
                    private String name;
                    private String email;
                }

                public class OrderItem {
                    private String sku;
                    private int quantity;
                    private BigDecimal unitPrice;
                }
                """;
    }

    // ── ToolProvider ──────────────────────────────────────────────────────

    @Override
    public String getLabel() {
        return "Java Bean Generator";
    }

    @Override
    public JComponent getView() {
        return rootPane;
    }

    @Override
    public int getOrder() {
        return -40;
    }
}
