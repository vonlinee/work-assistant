package org.assistant.tools.maven;

import org.assistant.tools.ToolProvider;
import org.assistant.ui.controls.Button;
import org.assistant.ui.controls.Label;
import org.assistant.ui.controls.TextField;
import org.assistant.ui.pane.BorderPane;
import org.assistant.ui.pane.HBox;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Maven tool pane — search the local Maven repository and convert
 * between Maven/Gradle coordinates.
 *
 * Auto-registered as a tab via ToolProvider reflection discovery.
 */
public class MavenToolPane extends BorderPane implements ToolProvider {

    // ── state ──────────────────────────────────────────────────────────────
    private final LocalRepoScanner scanner = new LocalRepoScanner();
    private final CoordinateConverter converter = new CoordinateConverter();
    private List<MavenArtifact> allArtifacts = Collections.emptyList();

    // ── repo panel ─────────────────────────────────────────────────────────
    private TextField repoPathField;
    private JLabel repoStatusLabel;

    // ── search panel ───────────────────────────────────────────────────────
    private TextField searchField;
    private final ArtifactTableModel tableModel = new ArtifactTableModel();
    private final JTable artifactTable = new JTable(tableModel);

    // ── converter panel ────────────────────────────────────────────────────
    private final JTextArea inputArea = new JTextArea(4, 40);
    private final JTextArea outputArea = new JTextArea(8, 40);

    public MavenToolPane() {
        setLayout(new BorderLayout());

        // ── top: repo selector + scan ──────────────────────────────────────
        JPanel topPanel = buildTopPanel();
        add(topPanel, BorderLayout.NORTH);

        // ── center: split pane (search results | converter) ────────────────
        JSplitPane center = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                buildSearchPanel(), buildConverterPanel());
        center.setResizeWeight(0.65);
        center.setOneTouchExpandable(true);
        add(center, BorderLayout.CENTER);
    }

    // ── Top panel: local repo path + scan ──────────────────────────────────

    private JPanel buildTopPanel() {
        HBox bar = new HBox();
        bar.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        bar.add(new Label("Local Repository:"));
        bar.addSpacing(6);

        repoPathField = new TextField(LocalRepoScanner.defaultRepoPath());
        repoPathField.setPreferredWidth(420);
        repoPathField.setMaximumWidth(500);
        bar.add(repoPathField);
        bar.addSpacing(4);

        Button browseBtn = new Button("Browse…");
        browseBtn.addActionListener(e -> {
            JFileChooser fc = new JFileChooser(repoPathField.getText());
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setDialogTitle("Select Local Maven Repository");
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                repoPathField.setText(fc.getSelectedFile().getAbsolutePath());
            }
        });
        bar.add(browseBtn);
        bar.addSpacing(8);

        Button scanBtn = new Button("Scan Repository");
        scanBtn.addActionListener(e -> doScan());
        bar.add(scanBtn);
        bar.addSpacing(12);

        repoStatusLabel = new JLabel("Not scanned yet.");
        repoStatusLabel.setForeground(Color.GRAY);
        bar.add(repoStatusLabel);
        bar.add(Box.createHorizontalGlue());

        return bar;
    }

    // ── Search panel ───────────────────────────────────────────────────────

    private JPanel buildSearchPanel() {
        BorderPane panel = new BorderPane();
        panel.setBorder(BorderFactory.createTitledBorder("Search Local Repository"));

        // search bar
        HBox searchBar = new HBox();
        searchBar.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        searchBar.add(new Label("Search:"));
        searchBar.addSpacing(6);
        searchField = new TextField();
        searchField.setMaximumWidth(400);
        searchBar.add(searchField);
        searchBar.addSpacing(6);
        Button searchBtn = new Button("Search");
        searchBtn.addActionListener(e -> doSearch());
        searchBar.add(searchBtn);
        searchBar.addSpacing(6);
        Button clearBtn = new Button("Clear");
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            doSearch();
        });
        searchBar.add(clearBtn);
        searchBar.addSpacing(12);
        searchBar.add(new Label("(Press Enter to search)"));
        searchBar.add(Box.createHorizontalGlue());
        panel.setTop(searchBar);

        searchField.addActionListener(e -> doSearch());

        // table
        artifactTable.setRowHeight(24);
        artifactTable.setAutoCreateRowSorter(true);
        artifactTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        artifactTable.getColumnModel().getColumn(ArtifactTableModel.COL_SIZE)
                .setCellRenderer(new SizeRenderer());

        int[] widths = { 180, 200, 90, 50, 160 };
        for (int i = 0; i < widths.length; i++) {
            artifactTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }

        // right-click context menu
        JPopupMenu popup = new JPopupMenu();
        JMenuItem copyMaven = new JMenuItem("Copy Maven XML");
        JMenuItem copyCoord = new JMenuItem("Copy Maven Coordinate");
        JMenuItem copyGradle = new JMenuItem("Copy Gradle (Groovy)");
        JMenuItem copyGradleK = new JMenuItem("Copy Gradle (Kotlin)");
        JMenuItem fillConverter = new JMenuItem("Convert in Converter Panel");
        JMenuItem showExplorer = new JMenuItem("Show in File Explorer");
        popup.add(copyMaven);
        popup.add(copyCoord);
        popup.add(copyGradle);
        popup.add(copyGradleK);
        popup.addSeparator();
        popup.add(fillConverter);
        popup.addSeparator();
        popup.add(showExplorer);

        copyMaven.addActionListener(e -> copySelected(a -> a.toMavenXml()));
        copyCoord.addActionListener(e -> copySelected(a -> a.toMavenCoordinate()));
        copyGradle.addActionListener(e -> copySelected(a -> a.toGradleGroovy()));
        copyGradleK.addActionListener(e -> copySelected(a -> a.toGradleKotlin()));
        fillConverter.addActionListener(e -> {
            MavenArtifact a = getSelectedArtifact();
            if (a != null) {
                inputArea.setText(a.toMavenCoordinate());
                doConvert();
            }
        });
        showExplorer.addActionListener(e -> {
            MavenArtifact a = getSelectedArtifact();
            if (a == null)
                return;
            String path = a.getJarPath();
            if (path == null || path.isBlank()) {
                JOptionPane.showMessageDialog(this,
                        "No JAR file found for this artifact (pom-only).",
                        "Not Available", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            showInExplorer(path);
        });

        // Select the row on right-click before showing the popup
        artifactTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (javax.swing.SwingUtilities.isRightMouseButton(e)) {
                    int row = artifactTable.rowAtPoint(e.getPoint());
                    if (row >= 0)
                        artifactTable.setRowSelectionInterval(row, row);
                }
            }
        });

        artifactTable.setComponentPopupMenu(popup);

        // double-click → fill converter
        artifactTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    MavenArtifact a = getSelectedArtifact();
                    if (a != null) {
                        inputArea.setText(a.toMavenCoordinate());
                        doConvert();
                    }
                }
            }
        });

        panel.setCenter(new JScrollPane(artifactTable));

        // hint bar
        HBox hint = new HBox();
        hint.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        hint.add(new Label("Double-click or right-click a row to copy/convert coordinates."));
        panel.setBottom(hint);

        return panel;
    }

    // ── Converter panel ────────────────────────────────────────────────────

    private JPanel buildConverterPanel() {
        BorderPane panel = new BorderPane();
        panel.setBorder(BorderFactory.createTitledBorder(
                "Coordinate Converter  (Maven XML · Maven short · Gradle Groovy · Gradle Kotlin)"));

        // Input
        inputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        inputArea.setLineWrap(true);
        JScrollPane inputScroll = new JScrollPane(inputArea);
        inputScroll.setBorder(BorderFactory.createTitledBorder("Input (any format)"));

        // Output
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        JScrollPane outputScroll = new JScrollPane(outputArea);
        outputScroll.setBorder(BorderFactory.createTitledBorder("All Formats"));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, inputScroll, outputScroll);
        split.setResizeWeight(0.35);
        panel.setCenter(split);

        // Buttons
        HBox btns = new HBox();
        btns.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        Button convertBtn = new Button("Convert →");
        Button clearBtn = new Button("Clear");
        Button copyMavenBtn = new Button("Copy Maven");
        Button copyGradleBtn = new Button("Copy Gradle");
        Button copyKotlinBtn = new Button("Copy Kotlin");

        convertBtn.addActionListener(e -> doConvert());
        clearBtn.addActionListener(e -> {
            inputArea.setText("");
            outputArea.setText("");
        });
        copyMavenBtn.addActionListener(e -> copyOutputSection("── Maven XML"));
        copyGradleBtn.addActionListener(e -> copyOutputSection("── Gradle Groovy DSL"));
        copyKotlinBtn.addActionListener(e -> copyOutputSection("── Gradle Kotlin DSL"));

        btns.add(convertBtn);
        btns.addSpacing(8);
        btns.add(clearBtn);
        btns.addSpacing(16);
        btns.add(copyMavenBtn);
        btns.addSpacing(4);
        btns.add(copyGradleBtn);
        btns.addSpacing(4);
        btns.add(copyKotlinBtn);
        btns.add(Box.createHorizontalGlue());
        panel.setBottom(btns);

        // Convert on Ctrl+Enter in input area
        inputArea.getInputMap().put(
                KeyStroke.getKeyStroke("ctrl ENTER"),
                "convert");
        inputArea.getActionMap().put("convert", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                doConvert();
            }
        });

        return panel;
    }

    // ── Actions ────────────────────────────────────────────────────────────

    private void doScan() {
        String path = repoPathField.getText().trim();
        File dir = new File(path);
        if (!dir.exists() || !dir.isDirectory()) {
            JOptionPane.showMessageDialog(this,
                    "Repository path does not exist:\n" + path,
                    "Invalid Path", JOptionPane.WARNING_MESSAGE);
            return;
        }

        repoStatusLabel.setText("Scanning...");
        repoStatusLabel.setForeground(Color.ORANGE.darker());
        tableModel.setData(Collections.emptyList());

        SwingWorker<List<MavenArtifact>, String> worker = new SwingWorker<>() {
            @Override
            protected List<MavenArtifact> doInBackground() {
                return scanner.scan(path, msg -> publish(msg));
            }

            @Override
            protected void process(List<String> chunks) {
                if (!chunks.isEmpty())
                    repoStatusLabel.setText(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                try {
                    allArtifacts = get();
                    tableModel.setData(allArtifacts);
                    repoStatusLabel.setText("✓ " + allArtifacts.size() + " artifacts indexed.");
                    repoStatusLabel.setForeground(new Color(0, 130, 0));
                } catch (Exception ex) {
                    repoStatusLabel.setText("✗ Scan failed: " + ex.getMessage());
                    repoStatusLabel.setForeground(Color.RED);
                }
            }
        };
        worker.execute();
    }

    private void doSearch() {
        String kw = searchField.getText();
        List<MavenArtifact> results = scanner.search(allArtifacts, kw);
        tableModel.setData(results);
    }

    private void doConvert() {
        String input = inputArea.getText().strip();
        if (input.isEmpty())
            return;
        MavenArtifact a = converter.parse(input);
        if (a == null) {
            outputArea.setText("Could not parse input. Supported formats:\n"
                    + "  groupId:artifactId:version\n"
                    + "  Maven <dependency> XML\n"
                    + "  Gradle implementation 'g:a:v'");
            return;
        }
        outputArea.setText(converter.fullReport(a));
        outputArea.setCaretPosition(0);
    }

    private void copySelected(java.util.function.Function<MavenArtifact, String> fn) {
        MavenArtifact a = getSelectedArtifact();
        if (a == null)
            return;
        copyToClipboard(fn.apply(a));
    }

    private void copyOutputSection(String sectionHeader) {
        String text = outputArea.getText();
        int start = text.indexOf(sectionHeader);
        if (start < 0) {
            copyToClipboard(text);
            return;
        }
        int nextSection = text.indexOf("──", start + sectionHeader.length());
        String section = nextSection < 0 ? text.substring(start) : text.substring(start, nextSection);
        // strip the header line, keep just the content
        int nl = section.indexOf('\n');
        String content = nl >= 0 ? section.substring(nl + 1).strip() : section.strip();
        copyToClipboard(content);
    }

    private void copyToClipboard(String text) {
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(text), null);
    }

    private MavenArtifact getSelectedArtifact() {
        int view = artifactTable.getSelectedRow();
        if (view < 0)
            return null;
        int model = artifactTable.convertRowIndexToModel(view);
        return tableModel.getRow(model);
    }

    /**
     * Opens the system file-explorer and selects (highlights) the given file.
     * <ul>
     * <li>Windows – uses {@code explorer.exe /select,<path>} so the file is
     * highlighted.</li>
     * <li>macOS – uses {@code open -R <path>} (Reveal in Finder).</li>
     * <li>Other – falls back to {@link Desktop#open(java.io.File)} on the parent
     * directory.</li>
     * </ul>
     */
    private void showInExplorer(String filePath) {
        File file = new File(filePath);
        String os = System.getProperty("os.name", "").toLowerCase();
        try {
            if (os.contains("win")) {
                // explorer /select,<path> → opens and highlights the file
                new ProcessBuilder("explorer.exe", "/select,", file.getAbsolutePath())
                        .start();
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", "-R", file.getAbsolutePath())
                        .start();
            } else {
                // Linux / fallback: open the parent directory
                File parent = file.getParentFile();
                if (parent != null && Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(parent);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Cannot open file explorer on this platform.\nFile: " + filePath,
                            "Not Supported", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to open file explorer:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── ToolProvider ───────────────────────────────────────────────────────

    @Override
    public String getLabel() {
        return "Maven Tool";
    }

    @Override
    public JComponent getView() {
        return this;
    }

    @Override
    public int getOrder() {
        return -50;
    }

    // ── Table model ────────────────────────────────────────────────────────

    private static class ArtifactTableModel extends AbstractTableModel {
        static final int COL_GROUP = 0;
        static final int COL_ARTIFACT = 1;
        static final int COL_VERSION = 2;
        static final int COL_PKG = 3;
        static final int COL_SIZE = 4;

        private static final String[] COLS = { "GroupId", "ArtifactId", "Version", "Type", "Size" };
        private final List<MavenArtifact> rows = new ArrayList<>();

        void setData(List<MavenArtifact> data) {
            rows.clear();
            rows.addAll(data);
            fireTableDataChanged();
        }

        MavenArtifact getRow(int i) {
            return rows.get(i);
        }

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return COLS.length;
        }

        @Override
        public String getColumnName(int c) {
            return COLS[c];
        }

        @Override
        public boolean isCellEditable(int r, int c) {
            return false;
        }

        @Override
        public Class<?> getColumnClass(int c) {
            return c == COL_SIZE ? Long.class : String.class;
        }

        @Override
        public Object getValueAt(int r, int c) {
            MavenArtifact a = rows.get(r);
            return switch (c) {
                case COL_GROUP -> a.getGroupId();
                case COL_ARTIFACT -> a.getArtifactId();
                case COL_VERSION -> a.getVersion();
                case COL_PKG -> a.getPackaging();
                case COL_SIZE -> a.getSizeBytes();
                default -> null;
            };
        }
    }

    // ── Size cell renderer ────────────────────────────────────────────────

    private static class SizeRenderer extends DefaultTableCellRenderer {
        @Override
        public void setValue(Object value) {
            if (value instanceof Long bytes && bytes > 0) {
                if (bytes >= 1024 * 1024)
                    setText(String.format("%.1f MB", bytes / 1048576.0));
                else if (bytes >= 1024)
                    setText(String.format("%.1f KB", bytes / 1024.0));
                else
                    setText(bytes + " B");
            } else {
                setText("-");
            }
            setHorizontalAlignment(SwingConstants.RIGHT);
        }
    }
}
