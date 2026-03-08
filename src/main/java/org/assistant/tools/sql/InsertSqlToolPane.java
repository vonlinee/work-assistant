package org.assistant.tools.sql;

import org.assistant.tools.ToolProvider;
import org.assistant.ui.controls.DbDialectComboBox;
import org.assistant.ui.pane.BorderPane;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.druid.DbType;

public class InsertSqlToolPane extends BorderPane implements ToolProvider {

    private final DbDialectComboBox dialectBox;
    private final RSyntaxTextArea inputArea;
    private final JButton parseButton;
    private final JButton addEmptyRowButton;
    private final DefaultTableModel tableModel;
    private final JTable dataTable;
    private final JButton generateButton;
    private final RSyntaxTextArea outputArea;

    // Parsed State Context
    private String parsedTableName = "";
    private List<String> parsedColumns = new ArrayList<>();
    private boolean hadExplicitColumns = false;

    // Regex to match: INSERT INTO tableName [ (col1, col2) ] VALUES ...
    private static final Pattern INSERT_PATTERN = Pattern.compile(
            "(?i)^\\s*INSERT\\s+INTO\\s+(\\S+)(?:\\s*\\(([^)]+)\\))?\\s*VALUES\\s*(.*)$",
            Pattern.DOTALL);

    public InsertSqlToolPane() {
        // TOP PANEL: Dialect chooser, Action button, Input Area
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dialectBox = new DbDialectComboBox();
        parseButton = new JButton("Parse SQL");
        addEmptyRowButton = new JButton("Add Empty Row");
        JButton configTypesButton = new JButton("⚙ Config Types");

        controlPanel.add(new JLabel("Dialect: "));
        controlPanel.add(dialectBox);
        controlPanel.add(configTypesButton);
        controlPanel.add(parseButton);
        controlPanel.add(addEmptyRowButton);

        inputArea = new RSyntaxTextArea(6, 60);
        inputArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        inputArea.setText("INSERT INTO user(id, name) VALUES (1, 'zs'), (2, 'ls');");
        RTextScrollPane inputScroll = new RTextScrollPane(inputArea);

        topPanel.add(controlPanel, BorderLayout.NORTH);
        topPanel.add(inputScroll, BorderLayout.CENTER);

        // CENTER PANEL: Editable JTable
        tableModel = new DefaultTableModel();
        dataTable = new JTable(tableModel);
        dataTable.setRowHeight(30);
        dataTable.setShowGrid(true);
        JScrollPane tableScroll = new JScrollPane(dataTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Parsed Data"));

        // BOTTOM PANEL: Generate button, Output area
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        generateButton = new JButton("Generate Insert SQL");
        JPanel generatePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        generatePanel.add(generateButton);

        outputArea = new RSyntaxTextArea(6, 60);
        outputArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
        outputArea.setEditable(false);
        RTextScrollPane outputScroll = new RTextScrollPane(outputArea);

        bottomPanel.add(generatePanel, BorderLayout.NORTH);
        bottomPanel.add(outputScroll, BorderLayout.CENTER);

        // Split Panes for resizability
        JSplitPane splitBottom = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, bottomPanel);
        splitBottom.setResizeWeight(0.6);
        JSplitPane mainSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topPanel, splitBottom);
        mainSplit.setResizeWeight(0.3);

        setCenter(mainSplit);

        // Event Listeners
        parseButton.addActionListener(e -> parseSql());
        addEmptyRowButton.addActionListener(e -> {
            if (tableModel.getColumnCount() == 0) {
                parsedTableName = "table_name";
                tableModel.setColumnIdentifiers(new Object[] { "Column Name", "Type", "Value (Row 1)", "Actions" });
                setupColumnRenderers(1);
            }
            addEmptyRow(-1);
        });
        generateButton.addActionListener(e -> generateSql());

        dialectBox.addActionListener(e -> {
            // Reattach datatypes to combo box column dynamically whenever the dialect
            // changes, if we actually have data loaded already.
            if (tableModel.getColumnCount() >= 2) {
                int numValueRows = tableModel.getColumnCount() - 3;
                setupColumnRenderers(numValueRows);
            }
        });

        configTypesButton.addActionListener(e -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            if (parentWindow instanceof Frame) {
                DataTypeConfigDialog dialog = new DataTypeConfigDialog((Frame) parentWindow);
                // Pre-select the dialog's box to the currently active tool dialect
                // so the user immediately sees the types they wanted to edit.
                // Assuming DbDialectComboBox allows programmatic setting via normal
                // setSelectedItem:
                // dialog logic handles loading automatically on select.

                boolean saved = dialog.showDialog();
                if (saved && tableModel.getColumnCount() >= 2) {
                    int numValueRows = tableModel.getColumnCount() - 3;
                    setupColumnRenderers(numValueRows);
                }
            }
        });

        setupTableContextMenu();
    }

    private void setupTableContextMenu() {
        JPopupMenu popupMenu = new JPopupMenu();

        JMenuItem addRowItem = new JMenuItem("Add Row");
        JMenuItem copyRowItem = new JMenuItem("Copy Row(s)");
        JMenuItem deleteRowItem = new JMenuItem("Delete Row(s)");

        addRowItem.addActionListener(e -> addRow());
        copyRowItem.addActionListener(e -> copySelectedRows());
        deleteRowItem.addActionListener(e -> deleteSelectedRows());

        popupMenu.add(addRowItem);
        popupMenu.add(copyRowItem);
        popupMenu.add(deleteRowItem);

        dataTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    showPopup(e);
                }
            }

            private void showPopup(MouseEvent e) {
                int row = dataTable.rowAtPoint(e.getPoint());
                if (row >= 0) {
                    // Check if clicked row is not already part of an active selection
                    boolean isSelected = false;
                    for (int selectedRow : dataTable.getSelectedRows()) {
                        if (row == selectedRow) {
                            isSelected = true;
                            break;
                        }
                    }
                    if (!isSelected) {
                        dataTable.setRowSelectionInterval(row, row);
                    }
                }
                popupMenu.show(e.getComponent(), e.getX(), e.getY());
            }
        });
    }

    private void addRow() {
        if (tableModel.getColumnCount() == 0)
            return;
        int[] selectedRows = dataTable.getSelectedRows();
        addEmptyRow(selectedRows.length > 0 ? selectedRows[selectedRows.length - 1] : tableModel.getRowCount() - 1);
    }

    private void addEmptyRow(int insertAfterIndex) {
        if (tableModel.getColumnCount() == 0)
            return;
        Object[] emptyData = new Object[tableModel.getColumnCount()];
        emptyData[0] = "Column " + (tableModel.getRowCount() + 1);
        emptyData[1] = "VARCHAR"; // Default Type
        for (int i = 2; i < tableModel.getColumnCount() - 1; i++) {
            emptyData[i] = ""; // Values
        }
        emptyData[tableModel.getColumnCount() - 1] = ""; // Actions column
        if (insertAfterIndex >= 0 && insertAfterIndex < tableModel.getRowCount()) {
            tableModel.insertRow(insertAfterIndex + 1, emptyData);
            dataTable.setRowSelectionInterval(insertAfterIndex + 1, insertAfterIndex + 1);
        } else {
            tableModel.addRow(emptyData);
        }
    }

    private void copySelectedRows() {
        int[] selectedRows = dataTable.getSelectedRows();
        if (selectedRows.length == 0 || tableModel.getColumnCount() == 0)
            return;

        int targetIndex = selectedRows[selectedRows.length - 1] + 1;
        int colCount = tableModel.getColumnCount();

        List<Object[]> rowsToCopy = new ArrayList<>();
        for (int rowIdx : selectedRows) {
            Object[] rowData = new Object[colCount];
            for (int colIdx = 0; colIdx < colCount - 1; colIdx++) { // Skip Actions
                rowData[colIdx] = tableModel.getValueAt(rowIdx, colIdx);
            }
            rowData[colCount - 1] = ""; // Actions column
            rowsToCopy.add(rowData);
        }

        // Insert underneath
        int insertIdx = targetIndex;
        for (Object[] rowData : rowsToCopy) {
            tableModel.insertRow(insertIdx++, rowData);
        }

        // Select the newly copied rows
        dataTable.setRowSelectionInterval(targetIndex, insertIdx - 1);
    }

    private void deleteSelectedRows() {
        int[] selectedRows = dataTable.getSelectedRows();
        if (selectedRows.length == 0)
            return;

        // Sort descending explicitly to safely remove indices
        for (int i = selectedRows.length - 1; i >= 0; i--) {
            tableModel.removeRow(selectedRows[i]);
        }
    }

    private void parseSql() {
        String sql = inputArea.getText().trim();
        // Remove trailing semicolon if exists
        if (sql.endsWith(";")) {
            sql = sql.substring(0, sql.length() - 1);
        }

        Matcher matcher = INSERT_PATTERN.matcher(sql);
        if (!matcher.matches()) {
            JOptionPane.showMessageDialog(this,
                    "Invalid INSERT statement format.\nExpected: INSERT INTO table_name [(columns)] VALUES (values)",
                    "Parse Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        parsedTableName = matcher.group(1).trim();
        String columnsStr = matcher.group(2);
        String valuesStr = matcher.group(3).trim();

        parsedColumns.clear();
        hadExplicitColumns = (columnsStr != null && !columnsStr.trim().isEmpty());

        if (hadExplicitColumns && columnsStr != null) {
            String[] splitCols = columnsStr.split(",");
            for (String col : splitCols) {
                parsedColumns.add(col.trim());
            }
        }

        // Parse Values sets
        List<List<String>> valueSets = parseValues(valuesStr);

        // Handle missing explicit columns
        if (!hadExplicitColumns && !valueSets.isEmpty()) {
            int colCount = valueSets.get(0).size();
            for (int i = 0; i < colCount; i++) {
                parsedColumns.add("Column " + (i + 1));
            }
        }

        // --- PIVOT THE TABLE ---
        // Setup Headers: "Column Name", "Type", "Row 1", "Row 2"..., "Actions"
        List<String> headers = new ArrayList<>();
        headers.add("Column Name");
        headers.add("Type");

        int numValueRows = valueSets.isEmpty() ? 1 : valueSets.size();
        for (int i = 0; i < numValueRows; i++) {
            headers.add("Value (Row " + (i + 1) + ")");
        }
        headers.add("Actions");

        // Update JTable Model Structure
        tableModel.setColumnIdentifiers(headers.toArray());
        tableModel.setRowCount(0);

        setupColumnRenderers(numValueRows);

        // Populate Pivoted Rows (Each Row = 1 Parameter)
        for (int colIdx = 0; colIdx < parsedColumns.size(); colIdx++) {
            List<Object> pivotRow = new ArrayList<>();
            pivotRow.add(parsedColumns.get(colIdx)); // Column Name

            // Infer Type from Row 0
            String firstVal = (valueSets.isEmpty() || valueSets.get(0).size() <= colIdx) ? ""
                    : valueSets.get(0).get(colIdx);
            pivotRow.add(inferDataType(firstVal)); // Type

            // Values
            for (int rowIdx = 0; rowIdx < numValueRows; rowIdx++) {
                List<String> rowSet = valueSets.get(rowIdx);
                if (colIdx < rowSet.size()) {
                    pivotRow.add(rowSet.get(colIdx));
                } else {
                    pivotRow.add(""); // Pad missing dataset
                }
            }

            pivotRow.add(""); // Actions End Column
            tableModel.addRow(pivotRow.toArray());
        }
    }

    private void setupColumnRenderers(int numValueRows) {
        // Actions Renderer (Always the Last Column)
        int actionColIdx = 2 + numValueRows;
        if (actionColIdx < tableModel.getColumnCount()) {
            ActionPanelEditorRenderer actionRenderer = new ActionPanelEditorRenderer();
            dataTable.getColumnModel().getColumn(actionColIdx).setCellRenderer(actionRenderer);
            dataTable.getColumnModel().getColumn(actionColIdx).setCellEditor(actionRenderer);
            dataTable.getColumnModel().getColumn(actionColIdx).setPreferredWidth(120);
            dataTable.getColumnModel().getColumn(actionColIdx).setMaxWidth(120);
        }

        // Type ComboBox Renderer (Always Column 1)
        DbType selectedDialect = (DbType) dialectBox.getSelectedItem();
        String[] types = DataTypeManager.getTypesForDialect(selectedDialect);
        JComboBox<String> typeCombo = new JComboBox<>(types);
        dataTable.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(typeCombo));
    }

    private String inferDataType(String val) {
        if (val == null || val.trim().isEmpty() || val.equalsIgnoreCase("null")) {
            return "VARCHAR";
        }
        val = val.trim();
        if (val.startsWith("'") || val.startsWith("\"")) {
            return "VARCHAR";
        }
        if (val.matches("(?i).*\\(.*\\).*")) { // e.g. NOW()
            return "DATETIME";
        }
        if (val.matches("-?\\d+")) {
            return "INT";
        }
        if (val.matches("-?\\d+\\.\\d+")) {
            return "DECIMAL";
        }
        return "VARCHAR";
    }

    private void generateSql() {
        if (parsedTableName.isEmpty() || tableModel.getRowCount() == 0) {
            outputArea.setText("No data parsed or available to generate.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ").append(parsedTableName);

        int paramCount = tableModel.getRowCount(); // Because Table is Pivoted, RowCount = ParamCount
        int numValueRows = tableModel.getColumnCount() - 3; // "Name, Type ... Actions"

        if (paramCount == 0)
            return;

        // Reattach explicit columns
        sb.append(" (");
        for (int i = 0; i < paramCount; i++) {
            sb.append(tableModel.getValueAt(i, 0)); // Param Name Name
            if (i < paramCount - 1)
                sb.append(", ");
        }
        sb.append(")");

        sb.append(" VALUES \n");

        for (int valRowIdx = 0; valRowIdx < numValueRows; valRowIdx++) {
            sb.append("  (");
            for (int pIdx = 0; pIdx < paramCount; pIdx++) {
                // Column 2 indicates the first value row
                Object val = tableModel.getValueAt(pIdx, 2 + valRowIdx);
                String strVal = (val == null) ? "" : val.toString();
                sb.append(strVal);
                if (pIdx < paramCount - 1)
                    sb.append(", ");
            }
            sb.append(")");

            if (valRowIdx < numValueRows - 1) {
                sb.append(",\n");
            } else {
                sb.append(";");
            }
        }

        outputArea.setText(sb.toString());
    }

    /**
     * Parses the trailing "VALUES (a, b), (c, d)" blocks robustly over commas.
     */
    private List<List<String>> parseValues(String valuesStr) {
        List<List<String>> allRows = new ArrayList<>();

        // Remove outer parens logically
        boolean inQuote = false;
        char quoteChar = 0;
        int depth = 0;

        List<String> currentRow = null;
        StringBuilder currentToken = new StringBuilder();

        for (int i = 0; i < valuesStr.length(); i++) {
            char c = valuesStr.charAt(i);

            if (inQuote) {
                currentToken.append(c);
                if (c == quoteChar) { // Check if it's escaping or ending
                    if (i + 1 < valuesStr.length() && valuesStr.charAt(i + 1) == quoteChar) {
                        // Escaped quote (e.g. '')
                        currentToken.append(quoteChar);
                        i++;
                    } else {
                        // End of quote
                        inQuote = false;
                    }
                }
            } else {
                if (c == '\'' || c == '"') {
                    inQuote = true;
                    quoteChar = c;
                    currentToken.append(c);
                } else if (c == '(') {
                    depth++;
                    if (depth == 1) {
                        currentRow = new ArrayList<>();
                    } else {
                        currentToken.append(c); // nested parens in function calls e.g. NOW()
                    }
                } else if (c == ')') {
                    depth--;
                    if (depth == 0 && currentRow != null) {
                        currentRow.add(currentToken.toString().trim());
                        currentToken.setLength(0);
                        allRows.add(currentRow);
                        currentRow = null;
                    } else {
                        currentToken.append(c);
                    }
                } else if (c == ',' && depth == 1 && currentRow != null) {
                    currentRow.add(currentToken.toString().trim());
                    currentToken.setLength(0);
                } else {
                    if (depth > 0 || !Character.isWhitespace(c)) {
                        currentToken.append(c);
                    }
                }
            }
        }
        return allRows;
    }

    @Override
    public String getLabel() {
        return "Insert SQL (Beta)";
    }

    @Override
    public JComponent getView() {
        return this;
    }

    // --- Custom Icon Implementations ---
    private static class PlusIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(40, 167, 69)); // Green
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(x + 4, y + 8, x + 12, y + 8);
            g2.drawLine(x + 8, y + 4, x + 8, y + 12);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }
    }

    private static class CopyIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(0, 123, 255)); // Blue
            g2.setStroke(new BasicStroke(1.5f));
            // Back page
            g2.drawRect(x + 5, y + 2, 8, 10);
            // Erase behind front page
            g2.setColor(c.getBackground());
            g2.fillRect(x + 2, y + 5, 8, 10);
            // Front page
            g2.setColor(new Color(0, 123, 255));
            g2.drawRect(x + 2, y + 5, 8, 10);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }
    }

    private static class MinusIcon implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(220, 53, 69)); // Red
            g2.setStroke(new BasicStroke(2f));
            g2.drawLine(x + 4, y + 8, x + 12, y + 8);
            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return 16;
        }

        @Override
        public int getIconHeight() {
            return 16;
        }
    }

    class ActionPanelEditorRenderer extends AbstractCellEditor
            implements javax.swing.table.TableCellRenderer, javax.swing.table.TableCellEditor {
        private JPanel panel;
        private JButton btnAdd;
        private JButton btnCopy;
        private JButton btnDelete;
        private int currentRow;

        public ActionPanelEditorRenderer() {
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 0));
            panel.setOpaque(true);
            btnAdd = new JButton(new PlusIcon());
            btnAdd.setToolTipText("Add Row");
            btnCopy = new JButton(new CopyIcon());
            btnCopy.setToolTipText("Copy Row(s)");
            btnDelete = new JButton(new MinusIcon());
            btnDelete.setToolTipText("Delete Row");

            Insets margin = new Insets(2, 4, 2, 4);
            btnAdd.setMargin(margin);
            btnCopy.setMargin(margin);
            btnDelete.setMargin(margin);

            // Remove focus painting for cleaner look
            btnAdd.setFocusPainted(false);
            btnCopy.setFocusPainted(false);
            btnDelete.setFocusPainted(false);

            btnAdd.addActionListener(e -> {
                fireEditingStopped();
                addEmptyRow(currentRow);
            });
            btnCopy.addActionListener(e -> {
                fireEditingStopped();
                if (currentRow >= 0 && currentRow < tableModel.getRowCount()) {
                    int colCount = tableModel.getColumnCount();
                    Object[] rowData = new Object[colCount];
                    for (int c = 0; c < colCount - 1; c++) {
                        rowData[c] = tableModel.getValueAt(currentRow, c);
                    }
                    rowData[colCount - 1] = "";
                    tableModel.insertRow(currentRow + 1, rowData);
                }
            });
            btnDelete.addActionListener(e -> {
                fireEditingStopped();
                if (currentRow >= 0 && currentRow < tableModel.getRowCount()) {
                    tableModel.removeRow(currentRow);
                }
            });

            panel.add(btnAdd);
            panel.add(btnCopy);
            panel.add(btnDelete);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            if (isSelected) {
                panel.setBackground(table.getSelectionBackground());
            } else {
                panel.setBackground(table.getBackground());
            }
            return panel;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
                int column) {
            currentRow = row;
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }

        @Override
        public Object getCellEditorValue() {
            return "";
        }
    }
}
