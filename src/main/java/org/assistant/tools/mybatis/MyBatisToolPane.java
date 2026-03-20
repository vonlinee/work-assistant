package org.assistant.tools.mybatis;

import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.MappedStatement;
import org.assistant.tools.ToolProvider;
import org.assistant.ui.ExceptionDialog;
import org.assistant.ui.controls.table.SwingTreeTable;
import org.assistant.ui.pane.BorderPane;
import org.assistant.util.Messages;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.awt.*;
import java.io.File;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyBatisToolPane implements ToolProvider {

	private final BorderPane borderPane;
	private JTextField pathField;
	private JButton browseButton;
	private JButton scanButton;
	private SwingTreeTable treeTable;
	private MyBatisTreeTableModel treeTableModel;
	private ParamTable paramTable;
	private RSyntaxTextArea sqlTextArea;
	private RSyntaxTextArea parsedContentArea;
	private JTabbedPane bottomTabbedPane;
	private JButton renderButton;
	private JButton execQueryButton;
	private JButton importParamsButton;
	private JButton configTypesButton;

	private MyBatisScanner scanner;

	public MyBatisToolPane() {
		borderPane = new BorderPane();
		initComponents();
		layoutComponents();
		setupListeners();
	}

	private void initComponents() {
		pathField = new JTextField("D:\\Develop\\Code\\devpl-main\\devpl-backend");
		pathField.setToolTipText("Enter Maven/Gradle Project Path");
		browseButton = new JButton("Browse");
		scanButton = new JButton("Scan / Refresh");

		MyBatisNode root = new MyBatisNode("Root", "", "");
		treeTableModel = new MyBatisTreeTableModel();
		treeTable = new StatementTreeTable(treeTableModel);
		treeTable.setRowHeight(24);
		treeTable.setShowGrid(true, true);

		// Set column widths and renderer
		treeTable.getColumnModel().getColumn(0).setPreferredWidth(450);
		TableColumn typeColumn = treeTable.getColumnModel().getColumn(1);
		typeColumn.setPreferredWidth(60);
		typeColumn.setMinWidth(60);
		typeColumn.setMaxWidth(60);
		typeColumn.setCellRenderer(new StatementTypeRenderer());
		treeTable.getColumnModel().getColumn(2).setPreferredWidth(270);

		paramTable = new ParamTable();

		sqlTextArea = new RSyntaxTextArea(20, 60);
		sqlTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
		sqlTextArea.setCodeFoldingEnabled(true);

		parsedContentArea = new RSyntaxTextArea(20, 60);
		parsedContentArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
		parsedContentArea.setCodeFoldingEnabled(true);
		parsedContentArea.setEditable(false);

		renderButton = new JButton("Render SQL");
		execQueryButton = new JButton("▶ Execute Query");
		importParamsButton = new JButton("📥 Import Params");
		configTypesButton = new JButton("⚙ Config Param Types");
		configTypesButton.addActionListener(e -> {
			Window parentWindow = SwingUtilities.getWindowAncestor(borderPane);
			if (parentWindow == null) {
				// Fallback if not yet attached to a visible hierarchy
				parentWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
			}
			if (parentWindow instanceof Frame) {
				MyBatisTypeConfigDialog dialog = new MyBatisTypeConfigDialog((Frame) parentWindow);
				boolean saved = dialog.showDialog();
				if (saved && paramTable != null) {
					paramTable.refreshTypeEditors();
				}
			}
		});
	}

	private void layoutComponents() {
		// Top Panel: Project Path Selection
		JPanel topPanel = new JPanel(new BorderLayout(5, 5));
		topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel pathPanel = new JPanel(new BorderLayout(5, 0));
		pathPanel.add(new JLabel("Project Path:"), BorderLayout.WEST);
		pathPanel.add(pathField, BorderLayout.CENTER);
		pathPanel.add(browseButton, BorderLayout.EAST);

		topPanel.add(pathPanel, BorderLayout.CENTER);
		topPanel.add(scanButton, BorderLayout.EAST);

		// Center Panel: Tree Table
		JScrollPane scrollPane = new JScrollPane(treeTable);
		scrollPane.setBorder(BorderFactory.createTitledBorder("Parsed Mapped Statements"));

		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(paramTable, BorderLayout.CENTER);
		JPanel rightBottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		rightBottomPanel.add(configTypesButton);
		rightBottomPanel.add(importParamsButton);
		rightBottomPanel.add(renderButton);
		rightBottomPanel.add(execQueryButton);
		rightPanel.add(rightBottomPanel, BorderLayout.SOUTH);

		JPanel sqlPanel = new JPanel(new BorderLayout());
		sqlPanel.add(new RTextScrollPane(sqlTextArea), BorderLayout.CENTER);

		JPanel xmlPanel = new JPanel(new BorderLayout());
		xmlPanel.add(new RTextScrollPane(parsedContentArea), BorderLayout.CENTER);

		bottomTabbedPane = new JTabbedPane();
		bottomTabbedPane.addTab("Parsed Content (XML)", xmlPanel);
		bottomTabbedPane.addTab("Rendered SQL", sqlPanel);

		JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, rightPanel, bottomTabbedPane);
		rightSplitPane.setResizeWeight(0.5);

		JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollPane, rightSplitPane);
		mainSplitPane.setResizeWeight(0.4);

		borderPane.add(topPanel, BorderLayout.NORTH);
		borderPane.add(mainSplitPane, BorderLayout.CENTER);
	}

	private void setupListeners() {
		browseButton.addActionListener(e -> {
			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			if (chooser.showOpenDialog(borderPane) == JFileChooser.APPROVE_OPTION) {
				pathField.setText(chooser.getSelectedFile().getAbsolutePath());
			}
		});

		scanButton.addActionListener(e -> scanProject());

		treeTable.getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				updateParamTable();
			}
		});

		treeTable.addMouseListener(new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				if (e.getClickCount() == 2) {
					int viewRow = treeTable.rowAtPoint(e.getPoint());
					if (viewRow >= 0) {
						int modelRow = treeTable.convertRowIndexToModel(viewRow);
						TreePath pathForRow = treeTable.getPathForRow(modelRow);
						if (pathForRow != null) {
							if (treeTable.isExpanded(pathForRow)) {
								treeTable.collapsePath(pathForRow);
							} else {
								treeTable.expandPath(pathForRow);
							}
						}
					}
				}
			}
		});

		renderButton.addActionListener(e -> renderSql());
		importParamsButton.addActionListener(e -> openImportDialog());

		configTypesButton.addActionListener(e -> {
			Window parentWindow = SwingUtilities.getWindowAncestor(borderPane);
			if (parentWindow == null) {
				// Fallback if not yet attached to a visible hierarchy
				parentWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
			}
			if (parentWindow instanceof Frame) {
				MyBatisTypeConfigDialog dialog = new MyBatisTypeConfigDialog((Frame) parentWindow);
				boolean saved = dialog.showDialog();
				if (saved && paramTable != null) {
					paramTable.refreshTypeEditors();
				}
			}
		});
	}

	private void scanProject() {
		String path = pathField.getText();
		if (StringUtils.isBlank(path)) {
			JOptionPane.showMessageDialog(borderPane, "Please enter a project path.", "Error",
				JOptionPane.ERROR_MESSAGE);
			return;
		}

		try {
			Path projectRoot = Paths.get(path);
			if (scanner == null || !scanner.getProjectRoot().equals(projectRoot)) {
				scanner = new MyBatisScanner(projectRoot);
			}

			// Scan and parse
			Map<File, List<MappedStatement>> groupedStatements = scanner.scanAndParseGrouped();

			// Update UI
			updateTreeTable(groupedStatements);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(borderPane, "Error scanning project: " + ex.getMessage(), "Error",
				JOptionPane.ERROR_MESSAGE);
		}
	}

	private void updateTreeTable(Map<File, List<MappedStatement>> groupedStatements) {
		MyBatisNode root = new MyBatisNode("Root", "", "");

		for (Map.Entry<File, List<MappedStatement>> entry : groupedStatements.entrySet()) {
			File file = entry.getKey();
			List<MappedStatement> statements = entry.getValue();

			MyBatisNode fileNode = new MyBatisNode(file.getName(), "", file.getAbsolutePath());
			root.addChild(fileNode);

			for (MappedStatement ms : statements) {
				String fullId = ms.getId();
				String shortId = fullId;
				int lastDot = fullId.lastIndexOf('.');
				if (lastDot > 0) {
					shortId = fullId.substring(lastDot + 1);
				}

				MyBatisNode msNode = new MyBatisNode(shortId, ms.getSqlCommandType().name(), "");
				msNode.setMappedStatement(ms);
				fileNode.addChild(msNode);
			}
		}

		treeTableModel.setRoot(root);
		treeTable.updateUI();
		// treeTable.expandAll();
	}

	private void updateParamTable() {
		int viewRow = treeTable.getSelectedRow();
		if (viewRow >= 0) {
			int modelRow = treeTable.convertRowIndexToModel(viewRow);
			Object node = treeTable.getPathForRow(modelRow).getLastPathComponent();
			if (node instanceof MyBatisNode myNode) {
				if (myNode.getMappedStatement() != null) {
					paramTable.setStatement(myNode.getMappedStatement());
					parsedContentArea.setText(extractStatementXml(myNode.getMappedStatement()));
					if (bottomTabbedPane != null) {
						bottomTabbedPane.setSelectedIndex(0);
					}
					return;
				}
			}
		}
		paramTable.setStatement(null);
		parsedContentArea.setText("");
	}

	private void openImportDialog() {
		Window parentWindow = SwingUtilities.getWindowAncestor(borderPane);
		if (parentWindow == null)
			parentWindow = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();

		if (parentWindow instanceof Frame frame) {
			ParamImportDialog dialog = new ParamImportDialog(frame, paramTable.getRootNode(), (params, override) -> {
				paramTable.importParameters(params, override);
			});
			dialog.setVisible(true);
		}
	}

	private void renderSql() {
		int viewRow = treeTable.getSelectedRow();
		if (viewRow < 0)
			return;
		int modelRow = treeTable.convertRowIndexToModel(viewRow);
		TreePath pathForRow = treeTable.getPathForRow(modelRow);
		Object node = pathForRow.getLastPathComponent();
		if (node instanceof MyBatisNode myNode) {
			MappedStatement ms = myNode.getMappedStatement();
			if (ms != null) {
				try {
					Map<String, Object> paramMap = new HashMap<>();
					List<ParamNode> flattenedNodes = new java.util.ArrayList<>();
					ParamNode rootNode = paramTable.getRootNode();
					if (rootNode != null) {
						for (int i = 0; i < rootNode.getChildCount(); i++) {
							extractParams((ParamNode) rootNode.getChildAt(i), "", paramMap, flattenedNodes);
						}
					}
					org.apache.ibatis.mapping.BoundSql boundSql = ms.getBoundSql(paramMap);
					String sql = boundSql.getSql();
					// Format SQL with literal parameters if possible (basic replacement for now)
					if (!flattenedNodes.isEmpty()) {
						for (ParamNode pNode : flattenedNodes) {
							if (pNode != null && pNode.getValue() != null && !pNode.getValue().isEmpty()) {
								try {
									ParamDataType enumType = ParamDataType.asMap()
										.getOrDefault(pNode.getDataType().toUpperCase(), ParamDataType.STRING);
									String val = enumType.quote(pNode.getValue());
									sql = sql.replaceFirst("\\?",
										val != null ? java.util.regex.Matcher.quoteReplacement(val) : "null");
								} catch (Exception ignored) {
								}
							}
						}
					}
					sqlTextArea.setText(sql);
					if (bottomTabbedPane != null) {
						bottomTabbedPane.setSelectedIndex(1);
					}
				} catch (Exception ex) {
					sqlTextArea.setText("Error generating SQL:\n" + ex.getMessage());
				}
			}
		}
	}

	private void extractParams(ParamNode node, String prefix, Map<String, Object> paramMap,
														 List<ParamNode> flattenedNodes) {
		String currentPath = prefix;
		if (currentPath.isEmpty()) {
			currentPath = node.getKey();
		} else {
			if (node.getKey() != null && node.getKey().startsWith("[")) {
				currentPath = prefix + node.getKey();
			} else {
				currentPath = prefix + "." + node.getKey();
			}
		}

		// If it's a leaf node (has no children or has a value), capture it
		if (node.getChildCount() == 0) {
			flattenedNodes.add(node);
			try {
				ParamDataType enumType = ParamDataType.asMap()
					.getOrDefault(node.getDataType() != null ? node.getDataType().toUpperCase() : "STRING",
						ParamDataType.STRING);
				Object val = enumType.parseObject(node.getValue(), null);
				paramMap.put(currentPath, val);
			} catch (Exception ignored) {
			}
		} else {
			// Recurse children
			for (int i = 0; i < node.getChildCount(); i++) {
				extractParams((ParamNode) node.getChildAt(i), currentPath, paramMap, flattenedNodes);
			}
		}
	}

	private String extractStatementXml(MappedStatement ms) {
		try {
			String resource = ms.getResource();
			if (resource == null)
				return "No resource registered for this mapped statement.";

			// Extract plain file path if registered as 'file [C:\path\to\Mapper.xml]'
			if (resource.startsWith("file [") && resource.endsWith("]")) {
				resource = resource.substring(6, resource.length() - 1);
			}

			File xmlFile = new File(resource);
			if (!xmlFile.exists() || !xmlFile.isFile())
				return "Cannot locate XML file: " + resource;

			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setNamespaceAware(false);
			// Disable DTD loading for performance & offline parsing safety
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(xmlFile);

			String fullId = ms.getId();
			String shortId = fullId.substring(fullId.lastIndexOf('.') + 1);

			XPath xPath = XPathFactory.newInstance().newXPath();
			String expression = "//*[@id='" + shortId + "']";
			Node node = (Node) xPath.compile(expression).evaluate(doc, XPathConstants.NODE);

			if (node != null) {
				Transformer transformer = TransformerFactory.newInstance().newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
				transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

				StringWriter writer = new StringWriter();
				transformer.transform(new DOMSource(node), new StreamResult(writer));
				return writer.toString();
			} else {
				return "Could not find node with id='" + shortId + "' in " + xmlFile.getName();
			}
		} catch (Exception ex) {
			return "Error parsing XML for statement: " + ex.getMessage();
		}
	}

	@Override
	public String getLabel() {
		return Messages.getString("tools.mybatis.label");
	}

	@Override
	public JComponent getView() {
		return borderPane;
	}

	/**
	 * Custom cell renderer for coloring the Statement Type column.
	 */
	private static class StatementTypeRenderer implements TableCellRenderer {
		private static final Color COLOR_SELECT = new Color(230, 247, 255);
		private static final Color BORDER_SELECT = new Color(145, 213, 255);
		private static final Color TEXT_SELECT = new Color(9, 105, 218);

		private static final Color COLOR_INSERT = new Color(246, 255, 237);
		private static final Color BORDER_INSERT = new Color(183, 235, 143);
		private static final Color TEXT_INSERT = new Color(34, 134, 58);

		private static final Color COLOR_UPDATE = new Color(255, 251, 230);
		private static final Color BORDER_UPDATE = new Color(255, 229, 143);
		private static final Color TEXT_UPDATE = new Color(212, 107, 8);

		private static final Color COLOR_DELETE = new Color(255, 241, 240);
		private static final Color BORDER_DELETE = new Color(255, 163, 158);
		private static final Color TEXT_DELETE = new Color(207, 34, 46);

		private final JPanel panel = new JPanel(new GridBagLayout());
		private final JLabel label = new JLabel();

		public StatementTypeRenderer() {
			label.setOpaque(true);
			label.setFont(label.getFont().deriveFont(Font.BOLD, 11f));
			panel.add(label);
		}

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
																									 boolean isSelected, boolean hasFocus, int row, int column) {

			if (isSelected) {
				panel.setBackground(table.getSelectionBackground());
				panel.setForeground(table.getSelectionForeground());
			} else {
				panel.setBackground(table.getBackground());
				panel.setForeground(table.getForeground());
			}

			if (value == null || value.toString().trim().isEmpty()) {
				label.setText("");
				label.setOpaque(false);
				label.setBorder(BorderFactory.createEmptyBorder());
				return panel;
			}

			String type = value.toString().toUpperCase();
			label.setText(type);
			label.setOpaque(true);

			switch (type) {
				case "SELECT":
					label.setBackground(COLOR_SELECT);
					label.setForeground(TEXT_SELECT);
					label.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createLineBorder(BORDER_SELECT, 1, true),
						BorderFactory.createEmptyBorder(2, 6, 2, 6)));
					break;
				case "INSERT":
					label.setBackground(COLOR_INSERT);
					label.setForeground(TEXT_INSERT);
					label.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createLineBorder(BORDER_INSERT, 1, true),
						BorderFactory.createEmptyBorder(2, 6, 2, 6)));
					break;
				case "UPDATE":
					label.setBackground(COLOR_UPDATE);
					label.setForeground(TEXT_UPDATE);
					label.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createLineBorder(BORDER_UPDATE, 1, true),
						BorderFactory.createEmptyBorder(2, 6, 2, 6)));
					break;
				case "DELETE":
					label.setBackground(COLOR_DELETE);
					label.setForeground(TEXT_DELETE);
					label.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createLineBorder(BORDER_DELETE, 1, true),
						BorderFactory.createEmptyBorder(2, 6, 2, 6)));
					break;
				default:
					label.setOpaque(false);
					label.setForeground(table.getForeground());
					label.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
					break;
			}
			return panel;
		}
	}
}
