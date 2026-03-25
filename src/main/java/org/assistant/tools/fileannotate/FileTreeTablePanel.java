package org.assistant.tools.fileannotate;

import org.assistant.tools.fileannotate.model.FileNode;
import org.assistant.tools.fileannotate.model.FilterConfig;
import org.assistant.tools.fileannotate.service.FileAnnotationService;
import org.assistant.tools.fileannotate.service.FileScanner;
import org.assistant.ui.ExceptionDialog;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

/**
 * 文件树表格面板 - 支持文件标注
 */
public class FileTreeTablePanel extends JPanel {

	private final FileScanner fileScanner;
	private final FileAnnotationService annotationService;

	// UI Components
	private JXTreeTable treeTable;
	private FileTreeTableModel treeTableModel;
	private JTextField pathField;
	private JTextArea outputArea;
	private JComboBox<String> alignmentComboBox;

	// Data
	private FileNode rootNode;
	private FilterConfig filterConfig = new FilterConfig();

	public FileTreeTablePanel() {
		this.fileScanner = new FileScanner();
		this.annotationService = new FileAnnotationService();
		initUI();
	}

	private void initUI() {
		setLayout(new BorderLayout(10, 10));
		setBorder(new EmptyBorder(10, 10, 10, 10));

		// Top panel - Path selection
		add(createTopPanel(), BorderLayout.NORTH);

		// Center panel - Split pane with tree table and output
		add(createCenterPanel(), BorderLayout.CENTER);

		// Bottom panel - Action buttons
		add(createBottomPanel(), BorderLayout.SOUTH);
	}

	private JPanel createTopPanel() {
		JPanel panel = new JPanel(new BorderLayout(5, 5));

		pathField = new JTextField(30);
		pathField.setEditable(false);

		JButton browseButton = new JButton("Browse...");
		browseButton.addActionListener(e -> {
			getSelectedDir().ifPresent(this::consumeFile);
		});

		JButton filterButton = new JButton("Filter Config...");
		filterButton.setToolTipText("Configure file filters");
		filterButton.addActionListener(e -> showFilterConfigDialog());

		JPanel pathPanel = new JPanel(new BorderLayout(5, 5));
		pathPanel.add(new JLabel("Directory:"), BorderLayout.WEST);
		pathPanel.add(pathField, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
		buttonPanel.add(filterButton);
		buttonPanel.add(browseButton);

		pathPanel.add(buttonPanel, BorderLayout.EAST);

		panel.add(pathPanel, BorderLayout.CENTER);
		return panel;
	}

	/**
	 * 显示过滤配置对话框
	 */
	private void showFilterConfigDialog() {
		JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Filter Configuration", true);
		dialog.setSize(500, 450);
		dialog.setLocationRelativeTo(this);
		dialog.setLayout(new BorderLayout(10, 10));

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		// File extensions
		JPanel extPanel = createFilterListPanel("Excluded Extensions (e.g., .class, .log):",
			filterConfig.getExcludedExtensions(),
			e -> {
				String input = JOptionPane.showInputDialog(dialog, "Enter extension (e.g., .class):");
				if (input != null && !input.trim().isEmpty()) {
					filterConfig.addExcludedExtension(input.trim());
				}
			},
			e -> {
				filterConfig.getExcludedExtensions().clear();
			});
		contentPanel.add(extPanel);
		contentPanel.add(Box.createVerticalStrut(10));

		// File name patterns
		JPanel namePanel = createFilterListPanel("Excluded Filename Patterns (regex):",
			filterConfig.getExcludedNamePatterns(),
			e -> {
				String input = JOptionPane.showInputDialog(dialog, "Enter regex pattern:");
				if (input != null && !input.trim().isEmpty()) {
					filterConfig.addExcludedNamePattern(input.trim());
				}
			},
			e -> {
				filterConfig.getExcludedNamePatterns().clear();
			});
		contentPanel.add(namePanel);
		contentPanel.add(Box.createVerticalStrut(10));

		// Directory patterns
		JPanel dirPanel = createFilterListPanel("Excluded Directory Patterns (regex):",
			filterConfig.getExcludedDirPatterns(),
			e -> {
				String input = JOptionPane.showInputDialog(dialog, "Enter regex pattern:");
				if (input != null && !input.trim().isEmpty()) {
					filterConfig.addExcludedDirPattern(input.trim());
				}
			},
			e -> {
				filterConfig.getExcludedDirPatterns().clear();
			});
		contentPanel.add(dirPanel);
		contentPanel.add(Box.createVerticalStrut(10));

		// File size filters
		JPanel sizePanel = new JPanel(new GridLayout(2, 2, 5, 5));
		sizePanel.setBorder(BorderFactory.createTitledBorder("File Size Filters"));

		JTextField minSizeField = new JTextField(filterConfig.getMinFileSize() > 0 ?
			FilterConfig.formatSize(filterConfig.getMinFileSize()) : "");
		JTextField maxSizeField = new JTextField(filterConfig.getMaxFileSize() > 0 ?
			FilterConfig.formatSize(filterConfig.getMaxFileSize()) : "");

		sizePanel.add(new JLabel("Min Size (e.g., 1KB, 1MB):"));
		sizePanel.add(minSizeField);
		sizePanel.add(new JLabel("Max Size (e.g., 100MB, 1GB):"));
		sizePanel.add(maxSizeField);

		contentPanel.add(sizePanel);
		contentPanel.add(Box.createVerticalStrut(10));

		// Hidden files option
		JCheckBox ignoreHiddenCheckBox = new JCheckBox("Ignore hidden files/directories",
			filterConfig.isIgnoreHiddenFiles());
		contentPanel.add(ignoreHiddenCheckBox);

		JScrollPane scrollPane = new JScrollPane(contentPanel);
		dialog.add(scrollPane, BorderLayout.CENTER);

		// Button panel
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

		JButton applyButton = new JButton("Apply");
		applyButton.addActionListener(e -> {
			// Update file size filters
			filterConfig.setMinFileSize(FilterConfig.parseSize(minSizeField.getText()));
			filterConfig.setMaxFileSize(FilterConfig.parseSize(maxSizeField.getText()));
			filterConfig.setIgnoreHiddenFiles(ignoreHiddenCheckBox.isSelected());
			filterConfig.setIgnoreHiddenDirs(ignoreHiddenCheckBox.isSelected());

			// Reload directory if one is selected
			if (rootNode != null) {
				File currentDir = new File(rootNode.getPath());
				if (currentDir.exists()) {
					consumeFile(currentDir);
				}
			}

			dialog.dispose();
		});

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> dialog.dispose());

		JButton clearButton = new JButton("Clear All");
		clearButton.addActionListener(e -> {
			filterConfig.clear();
			minSizeField.setText("");
			maxSizeField.setText("");
			ignoreHiddenCheckBox.setSelected(true);
		});

		buttonPanel.add(applyButton);
		buttonPanel.add(clearButton);
		buttonPanel.add(cancelButton);

		dialog.add(buttonPanel, BorderLayout.SOUTH);
		dialog.setVisible(true);
	}

	/**
	 * 创建过滤列表面板
	 */
	private JPanel createFilterListPanel(String title, List<String> items,
										 java.awt.event.ActionListener addListener,
										 java.awt.event.ActionListener clearListener) {
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBorder(BorderFactory.createTitledBorder(title));

		DefaultListModel<String> listModel = new DefaultListModel<>();
		for (String item : items) {
			listModel.addElement(item);
		}

		JList<String> list = new JList<>(listModel);
		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setPreferredSize(new Dimension(300, 80));
		panel.add(scrollPane, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
		JButton addButton = new JButton("Add");
		addButton.addActionListener(addListener);
		JButton removeButton = new JButton("Remove");
		removeButton.addActionListener(e -> {
			int selectedIndex = list.getSelectedIndex();
			if (selectedIndex >= 0) {
				listModel.remove(selectedIndex);
				items.remove(selectedIndex);
			}
		});

		buttonPanel.add(addButton);
		buttonPanel.add(removeButton);
		panel.add(buttonPanel, BorderLayout.SOUTH);

		return panel;
	}

	private Optional<File> getSelectedDir() {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		int result = chooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File selectedDir = chooser.getSelectedFile();
			return Optional.of(selectedDir);
		}
		return Optional.empty();
	}

	private void consumeFile(File selectedDir) {
		pathField.setText(selectedDir.getAbsolutePath());
		ExceptionDialog.run(this, () -> loadDirectory(selectedDir));
	}

	private JSplitPane createCenterPanel() {
		// Left side - Tree table
		JPanel leftPanel = createTreeTablePanel();
		// Right side - Output
		JPanel rightPanel = createOutputPanel();
		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
		splitPane.setResizeWeight(0.6);
		return splitPane;
	}

	private JPanel createTreeTablePanel() {
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBorder(BorderFactory.createTitledBorder("File Tree"));

		// Create tree table model with empty root initially
		FileNode emptyRoot = new FileNode("", "", true);
		treeTableModel = new FileTreeTableModel(emptyRoot);
		treeTable = new JXTreeTable(treeTableModel);
		treeTable.setShowGrid(true);
		treeTable.setEditable(true);

		// Set column widths
		treeTable.getColumnModel().getColumn(0).setPreferredWidth(300);
		treeTable.getColumnModel().getColumn(1).setPreferredWidth(80);
		treeTable.getColumnModel().getColumn(2).setPreferredWidth(50);
		treeTable.getColumnModel().getColumn(3).setPreferredWidth(200);

		JScrollPane scrollPane = new JScrollPane(treeTable);
		panel.add(scrollPane, BorderLayout.CENTER);

		return panel;
	}

	private JPanel createOutputPanel() {
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBorder(BorderFactory.createTitledBorder("Output Preview"));

		outputArea = new JTextArea(20, 40);
		outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		outputArea.setLineWrap(false);
		outputArea.setEditable(false);

		JScrollPane scrollPane = new JScrollPane(outputArea);
		panel.add(scrollPane, BorderLayout.CENTER);

		return panel;
	}

	private JPanel createBottomPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));

		// Alignment options
		panel.add(new JLabel("Annotation Align:"));
		alignmentComboBox = new JComboBox<>(new String[]{"Right", "Left", "Inline"});
		alignmentComboBox.setSelectedItem("Right");
		panel.add(alignmentComboBox);

		JButton generateButton = new JButton("Generate Tree");
		generateButton.addActionListener(e -> generateTreeOutput());
		panel.add(generateButton);

		JButton copyButton = new JButton("Copy to Clipboard");
		copyButton.addActionListener(e -> copyToClipboard());
		panel.add(copyButton);

		JButton selectAllButton = new JButton("Select All");
		selectAllButton.addActionListener(e -> selectAll(true));
		panel.add(selectAllButton);

		JButton deselectAllButton = new JButton("Deselect All");
		deselectAllButton.addActionListener(e -> selectAll(false));
		panel.add(deselectAllButton);

		JButton invertButton = new JButton("Invert Selection");
		invertButton.setToolTipText("Invert current selection");
		invertButton.addActionListener(e -> invertSelection());
		panel.add(invertButton);

		JButton batchSelectButton = new JButton("Batch Select...");
		batchSelectButton.setToolTipText("Select files by criteria");
		batchSelectButton.addActionListener(e -> showBatchSelectionDialog());
		panel.add(batchSelectButton);

		JButton reconstructButton = new JButton("Reconstruct Directory");
		reconstructButton.addActionListener(e -> reconstructDirectory());
		panel.add(reconstructButton);

		return panel;
	}

	/**
	 * 反转选择状态
	 */
	private void invertSelection() {
		if (rootNode == null) return;

		for (FileNode node : rootNode.getAllNodes()) {
			node.setSelected(!node.isSelected());
		}
		treeTable.updateUI();
	}

	/**
	 * 显示批量选择对话框
	 */
	private void showBatchSelectionDialog() {
		if (rootNode == null) {
			JOptionPane.showMessageDialog(this, "Please select a directory first", "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}

		JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Batch Selection", true);
		dialog.setSize(450, 400);
		dialog.setLocationRelativeTo(this);
		dialog.setLayout(new BorderLayout(10, 10));

		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

		// Selection type
		JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		ButtonGroup typeGroup = new ButtonGroup();
		JRadioButton selectRadio = new JRadioButton("Select", true);
		JRadioButton deselectRadio = new JRadioButton("Deselect");
		typeGroup.add(selectRadio);
		typeGroup.add(deselectRadio);
		typePanel.add(new JLabel("Action:"));
		typePanel.add(selectRadio);
		typePanel.add(deselectRadio);
		contentPanel.add(typePanel);
		contentPanel.add(Box.createVerticalStrut(10));

		// Criteria tabs
		JTabbedPane tabbedPane = new JTabbedPane();

		// Tab 1: Filename pattern
		JPanel filenamePanel = new JPanel(new BorderLayout(5, 5));
		filenamePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		JTextField filenamePatternField = new JTextField(20);
		filenamePanel.add(new JLabel("Pattern (regex or * wildcard):"), BorderLayout.NORTH);
		filenamePanel.add(filenamePatternField, BorderLayout.CENTER);
		JCheckBox filenameCaseSensitive = new JCheckBox("Case sensitive");
		filenamePanel.add(filenameCaseSensitive, BorderLayout.SOUTH);
		tabbedPane.addTab("Filename", filenamePanel);

		// Tab 2: File extension
		JPanel extPanel = new JPanel(new BorderLayout(5, 5));
		extPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		JTextField extField = new JTextField(20);
		extPanel.add(new JLabel("Extensions (comma separated, e.g., java,txt,xml):"), BorderLayout.NORTH);
		extPanel.add(extField, BorderLayout.CENTER);
		tabbedPane.addTab("Extension", extPanel);

		// Tab 3: File size
		JPanel sizePanel = new JPanel(new GridLayout(3, 2, 5, 5));
		sizePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		JComboBox<String> sizeConditionCombo = new JComboBox<>(new String[]{"Greater than", "Less than", "Equal to"});
		JTextField sizeValueField = new JTextField(10);
		JComboBox<String> sizeUnitCombo = new JComboBox<>(new String[]{"B", "KB", "MB", "GB"});
		sizePanel.add(new JLabel("Condition:"));
		sizePanel.add(sizeConditionCombo);
		sizePanel.add(new JLabel("Size:"));
		JPanel sizeInputPanel = new JPanel(new BorderLayout(5, 0));
		sizeInputPanel.add(sizeValueField, BorderLayout.CENTER);
		sizeInputPanel.add(sizeUnitCombo, BorderLayout.EAST);
		sizePanel.add(sizeInputPanel);
		sizePanel.add(new JLabel(""));
		sizePanel.add(new JLabel(""));
		tabbedPane.addTab("File Size", sizePanel);

		// Tab 4: File type (file/directory)
		JPanel typeFilterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		typeFilterPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		JCheckBox filesCheckBox = new JCheckBox("Files", true);
		JCheckBox dirsCheckBox = new JCheckBox("Directories", true);
		typeFilterPanel.add(filesCheckBox);
		typeFilterPanel.add(dirsCheckBox);
		tabbedPane.addTab("Type", typeFilterPanel);

		contentPanel.add(tabbedPane);
		contentPanel.add(Box.createVerticalStrut(10));

		// Apply to children option
		JCheckBox applyToChildrenCheckBox = new JCheckBox("Apply to all descendants", true);
		contentPanel.add(applyToChildrenCheckBox);

		dialog.add(contentPanel, BorderLayout.CENTER);

		// Button panel
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

		JButton applyButton = new JButton("Apply");
		applyButton.addActionListener(e -> {
			boolean select = selectRadio.isSelected();
			boolean applyToChildren = applyToChildrenCheckBox.isSelected();

			switch (tabbedPane.getSelectedIndex()) {
				case 0 -> applyFilenameSelection(filenamePatternField.getText().trim(),
					filenameCaseSensitive.isSelected(), select, applyToChildren);
				case 1 -> applyExtensionSelection(extField.getText().trim(), select, applyToChildren);
				case 2 -> applySizeSelection(sizeConditionCombo.getSelectedIndex(),
					sizeValueField.getText().trim(), (String) sizeUnitCombo.getSelectedItem(),
					select, applyToChildren);
				case 3 -> applyTypeSelection(filesCheckBox.isSelected(), dirsCheckBox.isSelected(),
					select, applyToChildren);
			}

			treeTable.updateUI();
			dialog.dispose();
		});

		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(e -> dialog.dispose());

		buttonPanel.add(applyButton);
		buttonPanel.add(cancelButton);

		dialog.add(buttonPanel, BorderLayout.SOUTH);
		dialog.setVisible(true);
	}

	/**
	 * 应用文件名选择
	 */
	private void applyFilenameSelection(String pattern, boolean caseSensitive, boolean select, boolean applyToChildren) {
		if (pattern.isEmpty()) return;

		// Convert wildcard to regex
		String regex = pattern.replace(".", "\\.").replace("*", ".*").replace("?", ".");
		if (!caseSensitive) {
			regex = "(?i)" + regex;
		}

		java.util.regex.Pattern compiledPattern = java.util.regex.Pattern.compile(regex);

		for (FileNode node : rootNode.getAllNodes()) {
			if (compiledPattern.matcher(node.getName()).matches()) {
				node.setSelected(select);
				if (applyToChildren) {
					setChildrenSelected(node, select);
				}
			}
		}
	}

	/**
	 * 应用扩展名选择
	 */
	private void applyExtensionSelection(String extensions, boolean select, boolean applyToChildren) {
		if (extensions.isEmpty()) return;

		String[] exts = extensions.toLowerCase().split(",");
		for (int i = 0; i < exts.length; i++) {
			exts[i] = exts[i].trim().toLowerCase();
			if (!exts[i].startsWith(".")) {
				exts[i] = "." + exts[i];
			}
		}

		for (FileNode node : rootNode.getAllNodes()) {
			if (!node.isDirectory()) {
				String name = node.getName().toLowerCase();
				for (String ext : exts) {
					if (name.endsWith(ext)) {
						node.setSelected(select);
						if (applyToChildren) {
							setChildrenSelected(node, select);
						}
						break;
					}
				}
			}
		}
	}

	/**
	 * 应用文件大小选择
	 */
	private void applySizeSelection(int condition, String sizeValue, String unit, boolean select, boolean applyToChildren) {
		if (sizeValue.isEmpty()) return;

		long targetSize;
		try {
			targetSize = Long.parseLong(sizeValue);
		} catch (NumberFormatException e) {
			return;
		}

		// Convert to bytes
		switch (unit) {
			case "KB" -> targetSize *= 1024;
			case "MB" -> targetSize *= 1024 * 1024;
			case "GB" -> targetSize *= 1024 * 1024 * 1024;
		}

		long finalTargetSize = targetSize;
		for (FileNode node : rootNode.getAllNodes()) {
			if (!node.isDirectory()) {
				boolean match = switch (condition) {
					case 0 -> node.getSize() > finalTargetSize; // Greater than
					case 1 -> node.getSize() < finalTargetSize; // Less than
					case 2 -> node.getSize() == finalTargetSize; // Equal to
					default -> false;
				};

				if (match) {
					node.setSelected(select);
					if (applyToChildren) {
						setChildrenSelected(node, select);
					}
				}
			}
		}
	}

	/**
	 * 应用类型选择（文件/目录）
	 */
	private void applyTypeSelection(boolean includeFiles, boolean includeDirs, boolean select, boolean applyToChildren) {
		for (FileNode node : rootNode.getAllNodes()) {
			boolean match = (node.isDirectory() && includeDirs) || (!node.isDirectory() && includeFiles);
			if (match) {
				node.setSelected(select);
				if (applyToChildren) {
					setChildrenSelected(node, select);
				}
			}
		}
	}

	/**
	 * 设置所有子节点的选择状态
	 */
	private void setChildrenSelected(FileNode parent, boolean selected) {
		for (FileNode child : parent.getChildren()) {
			child.setSelected(selected);
			setChildrenSelected(child, selected);
		}
	}

	private void loadDirectory(File dir) {
		rootNode = fileScanner.scanDirectory(dir, filterConfig);
		annotationService.applyAnnotations(rootNode);
		treeTableModel.setRoot(rootNode);
		treeTable.updateUI();
		// Expand root node after UI update
		SwingUtilities.invokeLater(() -> {
			if (rootNode != null) {
				treeTable.expandRow(0);
			}
		});
	}

	private void generateTreeOutput() {
		if (rootNode == null) {
			JOptionPane.showMessageDialog(this, "Please select a directory first", "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}

		String alignment = (String) alignmentComboBox.getSelectedItem();
		StringBuilder sb = new StringBuilder();
		generateTreeRecursive(rootNode, sb, "", true, alignment);

		outputArea.setText(sb.toString());
	}

	private void generateTreeRecursive(FileNode node, StringBuilder sb, String prefix, boolean isLast, String alignment) {
		if (!node.isSelected()) {
			return;
		}

		// Build tree connector
		String connector = isLast ? "└── " : "├── ";
		String line = prefix + connector + node.getName();

		// Add annotation based on alignment
		if (node.getAnnotation() != null && !node.getAnnotation().isEmpty()) {
			switch (alignment) {
				case "Right" -> {
					int padding = Math.max(50 - line.length(), 5);
					line += " ".repeat(padding) + "# " + node.getAnnotation();
				}
				case "Left" -> line = "# " + node.getAnnotation() + " | " + line;
				case "Inline" -> line += "  # " + node.getAnnotation();
			}
		}

		sb.append(line).append("\n");

		// Process children
		List<FileNode> children = node.getChildren();
		String childPrefix = prefix + (isLast ? "    " : "│   ");

		for (int i = 0; i < children.size(); i++) {
			FileNode child = children.get(i);
			boolean lastChild = (i == children.size() - 1);
			generateTreeRecursive(child, sb, childPrefix, lastChild, alignment);
		}
	}

	private void copyToClipboard() {
		String text = outputArea.getText();
		if (text.isEmpty()) {
			JOptionPane.showMessageDialog(this, "No content to copy", "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}

		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(new StringSelection(text), null);
		JOptionPane.showMessageDialog(this, "Copied to clipboard", "Info", JOptionPane.INFORMATION_MESSAGE);
	}

	private void selectAll(boolean selected) {
		if (rootNode == null) return;

		for (FileNode node : rootNode.getAllNodes()) {
			node.setSelected(selected);
		}
		treeTable.updateUI();
	}

	private void reconstructDirectory() {
		if (rootNode == null) {
			JOptionPane.showMessageDialog(this, "Please select a directory first", "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}

		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setDialogTitle("Select target directory for reconstruction");
		chooser.setCurrentDirectory(new File(System.getProperty("user.home")));

		int result = chooser.showSaveDialog(this);
		if (result != JFileChooser.APPROVE_OPTION) {
			return;
		}

		File targetDir = chooser.getSelectedFile();

		// Confirm if directory exists
		if (targetDir.exists() && targetDir.list() != null && targetDir.list().length > 0) {
			int confirm = JOptionPane.showConfirmDialog(this,
				"Target directory is not empty. Continue?",
				"Confirm",
				JOptionPane.YES_NO_OPTION);
			if (confirm != JOptionPane.YES_OPTION) {
				return;
			}
		}

		// Create root directory
		File rootTarget = new File(targetDir, rootNode.getName());
		reconstructNode(rootNode, rootTarget);

		JOptionPane.showMessageDialog(this,
			"Directory reconstructed successfully at:\n" + rootTarget.getAbsolutePath(),
			"Success",
			JOptionPane.INFORMATION_MESSAGE);
	}

	private void reconstructNode(FileNode node, File target) {
		if (!node.isSelected()) {
			return;
		}

		if (node.isDirectory()) {
			target.mkdirs();
			for (FileNode child : node.getChildren()) {
				File childTarget = new File(target, child.getName());
				reconstructNode(child, childTarget);
			}
		} else {
			try {
				target.getParentFile().mkdirs();
				Files.createFile(target.toPath());
			} catch (IOException e) {
				System.err.println("Failed to create file: " + target);
			}
		}
	}

	/**
	 * 树表格模型
	 */
	private static class FileTreeTableModel extends AbstractTreeTableModel {

		private static final String[] COLUMNS = {"Name", "Size", "Selected", "Annotation"};

		public FileTreeTableModel(FileNode root) {
			super(root);
		}

		public void setRoot(FileNode root) {
			this.root = root;
			// Notify that the root has changed
			if (root != null) {
				modelSupport.fireNewRoot();
			}
		}

		@Override
		public int getColumnCount() {
			return COLUMNS.length;
		}

		@Override
		public String getColumnName(int column) {
			return COLUMNS[column];
		}

		@Override
		public Object getValueAt(Object node, int column) {
			if (!(node instanceof FileNode fileNode)) {
				return null;
			}

			return switch (column) {
				case 0 -> fileNode.getName();
				case 1 -> fileNode.getSizeDisplay();
				case 2 -> fileNode.isSelected();
				case 3 -> fileNode.getAnnotation();
				default -> null;
			};
		}

		@Override
		public void setValueAt(Object value, Object node, int column) {
			if (!(node instanceof FileNode fileNode)) {
				return;
			}

			if (column == 2 && value instanceof Boolean) {
				fileNode.setSelected((Boolean) value);
			} else if (column == 3 && value instanceof String) {
				fileNode.setAnnotation((String) value);
			}
		}

		@Override
		public boolean isCellEditable(Object node, int column) {
			return column == 2 || column == 3; // Selected and Annotation columns
		}

		@Override
		public Class<?> getColumnClass(int column) {
			return switch (column) {
				case 0 -> String.class;
				case 1 -> String.class;
				case 2 -> Boolean.class;
				case 3 -> String.class;
				default -> Object.class;
			};
		}

		@Override
		public Object getChild(Object parent, int index) {
			if (parent instanceof FileNode fileNode) {
				return fileNode.getChildren().get(index);
			}
			return null;
		}

		@Override
		public int getChildCount(Object parent) {
			if (parent instanceof FileNode fileNode) {
				return fileNode.getChildren().size();
			}
			return 0;
		}

		@Override
		public int getIndexOfChild(Object parent, Object child) {
			if (parent instanceof FileNode parentNode && child instanceof FileNode childNode) {
				return parentNode.getChildren().indexOf(childNode);
			}
			return -1;
		}

		@Override
		public boolean isLeaf(Object node) {
			if (node instanceof FileNode fileNode) {
				// A node is a leaf if it has no children
				return fileNode.getChildren().isEmpty() && !fileNode.isDirectory();
			}
			return true;
		}
	}
}
