package org.assistant.tools.mybatis;

import org.apache.ibatis.mapping.MappedStatement;
import org.assistant.tools.ToolProvider;

import org.assistant.ui.controls.table.SwingTreeTable;
import org.assistant.ui.pane.BorderPane;
import org.assistant.util.Messages;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.jdesktop.swingx.JXTreeTable;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
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
		treeTable = new SwingTreeTable(treeTableModel);
		treeTable.setRowHeight(24);

		paramTable = new ParamTable();

		sqlTextArea = new RSyntaxTextArea(20, 60);
		sqlTextArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_SQL);
		sqlTextArea.setCodeFoldingEnabled(true);

		parsedContentArea = new RSyntaxTextArea(20, 60);
		parsedContentArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_XML);
		parsedContentArea.setCodeFoldingEnabled(true);
		parsedContentArea.setEditable(false);

		renderButton = new JButton("Render SQL");
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
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		buttonPanel.add(renderButton);
		rightPanel.add(buttonPanel, BorderLayout.SOUTH);

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

		renderButton.addActionListener(e -> renderSql());
	}

	private void scanProject() {
		String path = pathField.getText();
		if (path == null || path.trim().isEmpty()) {
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
					List<ParamNode> children = paramTable.getRootNode().getChildren();
					if (children != null) {
						for (ParamNode pNode : children) {
							if (pNode != null && pNode.getDataType() != null) {
								try {
									Object val = pNode.getDataType().parseObject(pNode.getValue(), null);
									paramMap.put(pNode.getKey(), val);
								} catch (Exception ignored) {
								}
							}
						}
					}
					org.apache.ibatis.mapping.BoundSql boundSql = ms.getBoundSql(paramMap);
					String sql = boundSql.getSql();
					// Format SQL with literal parameters if possible (basic replacement for now)
					if (children != null) {
						for (ParamNode pNode : children) {
							if (pNode != null && pNode.getValue() != null && !pNode.getValue().isEmpty()) {
								String val = pNode.getDataType().quote(pNode.getValue());
								sql = sql.replaceFirst("\\?",
										val != null ? java.util.regex.Matcher.quoteReplacement(val) : "null");
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
}
