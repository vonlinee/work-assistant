package org.assistant.tools.prompt;

import org.assistant.tools.prompt.model.Prompt;
import org.assistant.tools.prompt.service.PromptService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

/**
 * 提示词管理面板
 */
public class PromptManagerPanel extends JPanel {

	private final PromptService promptService;

	// UI Components
	private JList<Prompt> promptList;
	private DefaultListModel<Prompt> listModel;
	private JTextField searchField;
	private JTextField nameField;
	private JComboBox<String> categoryComboBox;
	private DefaultComboBoxModel<String> categoryComboModel;
	private JTextArea descriptionArea;
	private JTextArea contentArea;
	private JCheckBox favoriteCheckBox;
	private JLabel createTimeLabel;
	private JLabel updateTimeLabel;

	// Buttons
	private JButton addButton;
	private JButton editButton;
	private JButton deleteButton;
	private JButton saveButton;
	private JButton cancelButton;
	private JButton copyButton;
	private JButton manageCategoryButton;

	// State
	private Prompt selectedPrompt;
	private boolean isEditing = false;
	private boolean isCreating = false;

	public PromptManagerPanel() {
		this.promptService = new PromptService();
		initUI();
		loadPrompts();
	}

	private void initUI() {
		setLayout(new BorderLayout(10, 10));
		setBorder(new EmptyBorder(10, 10, 10, 10));

		// Top panel - Search and filters
		add(createTopPanel(), BorderLayout.NORTH);

		// Center panel - Split pane with list and detail
		add(createCenterPanel(), BorderLayout.CENTER);

		// Bottom panel - Action buttons
		add(createBottomPanel(), BorderLayout.SOUTH);
	}

	private JPanel createTopPanel() {
		JPanel panel = new JPanel(new BorderLayout(5, 5));

		// Search field
		searchField = new JTextField(20);
		searchField.setToolTipText("Search prompts by name, content, category or description");
		searchField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				search();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				search();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				search();
			}
		});

		JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
		searchPanel.add(new JLabel("Search:"), BorderLayout.WEST);
		searchPanel.add(searchField, BorderLayout.CENTER);

		panel.add(searchPanel, BorderLayout.CENTER);
		return panel;
	}

	private JSplitPane createCenterPanel() {
		// Left side - Prompt list
		JPanel leftPanel = createListPanel();

		// Right side - Prompt detail
		JPanel rightPanel = createDetailPanel();

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
		splitPane.setResizeWeight(0.35);
		splitPane.setDividerLocation(0.35);

		return splitPane;
	}

	private JPanel createListPanel() {
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBorder(BorderFactory.createTitledBorder("Prompt List"));

		// List model and list
		listModel = new DefaultListModel<>();
		promptList = new JList<>(listModel);
		promptList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		promptList.setCellRenderer(new PromptListCellRenderer());

		promptList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					promptSelected();
				}
			}
		});

		promptList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					copySelectedPrompt();
				}
			}
		});

		JScrollPane scrollPane = new JScrollPane(promptList);
		panel.add(scrollPane, BorderLayout.CENTER);

		return panel;
	}

	private JPanel createDetailPanel() {
		JPanel panel = new JPanel(new BorderLayout(5, 5));
		panel.setBorder(BorderFactory.createTitledBorder("Prompt Details"));

		// Form panel
		JPanel formPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		// Name
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		formPanel.add(new JLabel("Name:"), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1;
		nameField = new JTextField(20);
		formPanel.add(nameField, gbc);

		// Category
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0;
		formPanel.add(new JLabel("Category:"), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1;
		JPanel categoryPanel = new JPanel(new BorderLayout(5, 0));
		categoryComboModel = new DefaultComboBoxModel<>();
		categoryComboBox = new JComboBox<>(categoryComboModel);
		categoryComboBox.setEditable(true);
		categoryPanel.add(categoryComboBox, BorderLayout.CENTER);

		manageCategoryButton = new JButton("...");
		manageCategoryButton.setToolTipText("Manage categories");
		manageCategoryButton.addActionListener(e -> showCategoryManagerDialog());
		categoryPanel.add(manageCategoryButton, BorderLayout.EAST);

		formPanel.add(categoryPanel, gbc);

		// Favorite
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.weightx = 0;
		formPanel.add(new JLabel("Favorite:"), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1;
		favoriteCheckBox = new JCheckBox();
		formPanel.add(favoriteCheckBox, gbc);

		// Description
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.weightx = 0;
		gbc.anchor = GridBagConstraints.NORTHWEST;
		formPanel.add(new JLabel("Description:"), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1;
		gbc.weighty = 0.3;
		gbc.fill = GridBagConstraints.BOTH;
		descriptionArea = new JTextArea(3, 20);
		descriptionArea.setLineWrap(true);
		descriptionArea.setWrapStyleWord(true);
		JScrollPane descScroll = new JScrollPane(descriptionArea);
		formPanel.add(descScroll, gbc);

		// Content
		gbc.gridx = 0;
		gbc.gridy = 4;
		gbc.weightx = 0;
		gbc.weighty = 0.7;
		gbc.fill = GridBagConstraints.BOTH;
		formPanel.add(new JLabel("Content:"), gbc);

		gbc.gridx = 1;
		gbc.weightx = 1;
		gbc.weighty = 0.7;
		contentArea = new JTextArea(10, 20);
		contentArea.setLineWrap(true);
		contentArea.setWrapStyleWord(true);
		JScrollPane contentScroll = new JScrollPane(contentArea);
		formPanel.add(contentScroll, gbc);

		// Time labels
		JPanel timePanel = new JPanel(new GridLayout(2, 2, 5, 5));
		timePanel.add(new JLabel("Created:"));
		createTimeLabel = new JLabel("-");
		timePanel.add(createTimeLabel);
		timePanel.add(new JLabel("Updated:"));
		updateTimeLabel = new JLabel("-");
		timePanel.add(updateTimeLabel);

		gbc.gridx = 0;
		gbc.gridy = 5;
		gbc.gridwidth = 2;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		formPanel.add(timePanel, gbc);

		panel.add(formPanel, BorderLayout.CENTER);

		// Set fields editable initially false
		setFieldsEditable(false);

		return panel;
	}

	private JPanel createBottomPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));

		addButton = new JButton("Add");
		addButton.setToolTipText("Create a new prompt");
		addButton.addActionListener(e -> addPrompt());

		editButton = new JButton("Edit");
		editButton.setToolTipText("Edit selected prompt");
		editButton.addActionListener(e -> editPrompt());

		deleteButton = new JButton("Delete");
		deleteButton.setToolTipText("Delete selected prompt");
		deleteButton.addActionListener(e -> deletePrompt());

		saveButton = new JButton("Save");
		saveButton.setToolTipText("Save changes");
		saveButton.addActionListener(e -> savePrompt());
		saveButton.setVisible(false);

		cancelButton = new JButton("Cancel");
		cancelButton.setToolTipText("Cancel editing");
		cancelButton.addActionListener(e -> cancelEdit());
		cancelButton.setVisible(false);

		copyButton = new JButton("Copy Content");
		copyButton.setToolTipText("Copy prompt content to clipboard");
		copyButton.addActionListener(e -> copySelectedPrompt());

		panel.add(addButton);
		panel.add(editButton);
		panel.add(deleteButton);
		panel.add(saveButton);
		panel.add(cancelButton);
		panel.add(copyButton);

		return panel;
	}

	private void loadPrompts() {
		listModel.clear();
		List<Prompt> prompts = promptService.getAllPrompts();
		for (Prompt prompt : prompts) {
			listModel.addElement(prompt);
		}
		loadCategories();
	}

	/**
	 * 加载所有分类到下拉框
	 */
	private void loadCategories() {
		String currentSelection = categoryComboBox.getSelectedItem() != null ? categoryComboBox.getSelectedItem().toString() : null;
		categoryComboModel.removeAllElements();
		categoryComboModel.addElement(""); // Empty option
		List<String> categories = promptService.getAllCategories();
		for (String category : categories) {
			categoryComboModel.addElement(category);
		}
		if (currentSelection != null) {
			categoryComboBox.setSelectedItem(currentSelection);
		}
	}

	/**
	 * 显示分类管理对话框
	 */
	private void showCategoryManagerDialog() {
		JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Manage Categories", true);
		dialog.setSize(400, 300);
		dialog.setLocationRelativeTo(this);
		dialog.setLayout(new BorderLayout(10, 10));

		// Category list
		DefaultListModel<String> categoryListModel = new DefaultListModel<>();
		List<String> categories = promptService.getAllCategories();
		for (String category : categories) {
			categoryListModel.addElement(category);
		}
		JList<String> categoryList = new JList<>(categoryListModel);
		JScrollPane scrollPane = new JScrollPane(categoryList);
		scrollPane.setBorder(BorderFactory.createTitledBorder("Categories"));
		dialog.add(scrollPane, BorderLayout.CENTER);

		// Button panel
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));

		JButton addButton = new JButton("Add");
		addButton.addActionListener(e -> {
			String newCategory = JOptionPane.showInputDialog(dialog, "Enter new category name:");
			if (newCategory != null && !newCategory.trim().isEmpty()) {
				newCategory = newCategory.trim();
				if (!categoryListModel.contains(newCategory)) {
					categoryListModel.addElement(newCategory);
					promptService.addCategory(newCategory);
					loadCategories();
				} else {
					JOptionPane.showMessageDialog(dialog, "Category already exists!", "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		JButton editButton = new JButton("Edit");
		editButton.addActionListener(e -> {
			String selected = categoryList.getSelectedValue();
			if (selected == null) {
				JOptionPane.showMessageDialog(dialog, "Please select a category to edit", "Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}
			String newName = JOptionPane.showInputDialog(dialog, "Enter new name for category '" + selected + "':", selected);
			if (newName != null && !newName.trim().isEmpty()) {
				newName = newName.trim();
				if (newName.equals(selected)) {
					return;
				}
				if (categoryListModel.contains(newName)) {
					JOptionPane.showMessageDialog(dialog, "Category already exists!", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				// Update category in all prompts
				promptService.renameCategory(selected, newName);
				// Refresh UI
				int index = categoryListModel.indexOf(selected);
				categoryListModel.setElementAt(newName, index);
				loadCategories();
				// Refresh prompt list to show updated categories
				search();
			}
		});

		JButton deleteButton = new JButton("Delete");
		deleteButton.addActionListener(e -> {
			String selected = categoryList.getSelectedValue();
			if (selected == null) {
				JOptionPane.showMessageDialog(dialog, "Please select a category to delete", "Warning", JOptionPane.WARNING_MESSAGE);
				return;
			}
			int result = JOptionPane.showConfirmDialog(dialog,
				"Are you sure you want to delete category '" + selected + "'?\nPrompts in this category will become uncategorized.",
				"Confirm Delete",
				JOptionPane.YES_NO_OPTION,
				JOptionPane.WARNING_MESSAGE);
			if (result == JOptionPane.YES_OPTION) {
				promptService.deleteCategory(selected);
				categoryListModel.removeElement(selected);
				loadCategories();
				search();
			}
		});

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(e -> dialog.dispose());

		buttonPanel.add(addButton);
		buttonPanel.add(editButton);
		buttonPanel.add(deleteButton);
		buttonPanel.add(closeButton);

		dialog.add(buttonPanel, BorderLayout.SOUTH);
		dialog.setVisible(true);
	}

	private void search() {
		String keyword = searchField.getText().trim();
		listModel.clear();
		List<Prompt> prompts = promptService.searchPrompts(keyword);
		for (Prompt prompt : prompts) {
			listModel.addElement(prompt);
		}
	}

	private void promptSelected() {
		// Don't update fields if in edit mode
		if (isCreating || isEditing) {
			return;
		}
		selectedPrompt = promptList.getSelectedValue();
		if (selectedPrompt != null) {
			displayPrompt(selectedPrompt);
		} else {
			clearFields();
		}
	}

	private void displayPrompt(Prompt prompt) {
		nameField.setText(prompt.getName());
		categoryComboBox.setSelectedItem(prompt.getCategory());
		descriptionArea.setText(prompt.getDescription());
		contentArea.setText(prompt.getContent());
		favoriteCheckBox.setSelected(prompt.isFavorite());
		createTimeLabel.setText(prompt.getCreateTime() != null ? prompt.getCreateTime().toString() : "-");
		updateTimeLabel.setText(prompt.getUpdateTime() != null ? prompt.getUpdateTime().toString() : "-");
	}

	private void clearFields() {
		nameField.setText("");
		categoryComboBox.setSelectedItem(null);
		descriptionArea.setText("");
		contentArea.setText("");
		favoriteCheckBox.setSelected(false);
		createTimeLabel.setText("-");
		updateTimeLabel.setText("-");
	}

	private void setFieldsEditable(boolean editable) {
		nameField.setEditable(editable);
		categoryComboBox.setEnabled(editable);
		manageCategoryButton.setEnabled(editable);
		descriptionArea.setEditable(editable);
		contentArea.setEditable(editable);
		favoriteCheckBox.setEnabled(editable);
	}

	private void addPrompt() {
		isCreating = true;
		isEditing = false;
		selectedPrompt = null;
		promptList.clearSelection();
		clearFields();
		updateButtonState();
		setFieldsEditable(true);
		nameField.requestFocus();
	}

	private void editPrompt() {
		if (selectedPrompt == null) {
			JOptionPane.showMessageDialog(this, "Please select a prompt to edit", "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}
		isEditing = true;
		isCreating = false;
		updateButtonState();
		setFieldsEditable(true);
		nameField.requestFocus();
	}

	private void deletePrompt() {
		if (selectedPrompt == null) {
			JOptionPane.showMessageDialog(this, "Please select a prompt to delete", "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}

		int result = JOptionPane.showConfirmDialog(this,
			"Are you sure you want to delete prompt '" + selectedPrompt.getName() + "'?",
			"Confirm Delete",
			JOptionPane.YES_NO_OPTION,
			JOptionPane.WARNING_MESSAGE);

		if (result == JOptionPane.YES_OPTION) {
			promptService.deletePrompt(selectedPrompt.getId());
			loadPrompts();
			clearFields();
			selectedPrompt = null;
		}
	}

	private void savePrompt() {
		String name = nameField.getText().trim();
		String category = categoryComboBox.getSelectedItem() != null ? categoryComboBox.getSelectedItem().toString().trim() : "";
		String description = descriptionArea.getText().trim();
		String content = contentArea.getText().trim();

		if (name.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Name is required", "Warning", JOptionPane.WARNING_MESSAGE);
			nameField.requestFocus();
			return;
		}

		if (content.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Content is required", "Warning", JOptionPane.WARNING_MESSAGE);
			contentArea.requestFocus();
			return;
		}

		if (isCreating) {
			Prompt newPrompt = promptService.createPrompt(name, category, content, description);
			listModel.addElement(newPrompt);
			promptList.setSelectedValue(newPrompt, true);
		} else if (isEditing && selectedPrompt != null) {
			promptService.updatePrompt(selectedPrompt.getId(), name, category, content, description);
			if (favoriteCheckBox.isSelected() != selectedPrompt.isFavorite()) {
				promptService.toggleFavorite(selectedPrompt.getId());
			}
			// Refresh the list to show updated data
			int selectedIndex = promptList.getSelectedIndex();
			search();
			if (selectedIndex >= 0 && selectedIndex < listModel.size()) {
				promptList.setSelectedIndex(selectedIndex);
			}
		}

		isCreating = false;
		isEditing = false;
		setFieldsEditable(false);
		updateButtonState();
	}

	private void cancelEdit() {
		isCreating = false;
		isEditing = false;
		setFieldsEditable(false);
		if (selectedPrompt != null) {
			displayPrompt(selectedPrompt);
		} else {
			clearFields();
		}
		updateButtonState();
	}

	private void copySelectedPrompt() {
		if (selectedPrompt == null) {
			JOptionPane.showMessageDialog(this, "Please select a prompt to copy", "Warning", JOptionPane.WARNING_MESSAGE);
			return;
		}

		String content = selectedPrompt.getContent();
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(new StringSelection(content), null);

		JOptionPane.showMessageDialog(this, "Prompt content copied to clipboard", "Info", JOptionPane.INFORMATION_MESSAGE);
	}

	private void updateButtonState() {
		boolean inEditMode = isCreating || isEditing;
		addButton.setVisible(!inEditMode);
		editButton.setVisible(!inEditMode);
		deleteButton.setVisible(!inEditMode);
		copyButton.setVisible(!inEditMode);
		saveButton.setVisible(inEditMode);
		cancelButton.setVisible(inEditMode);
		searchField.setEnabled(!inEditMode);
		promptList.setEnabled(!inEditMode);
	}

	/**
	 * Custom list cell renderer for prompts
	 */
	private static class PromptListCellRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index,
												  boolean isSelected, boolean cellHasFocus) {
			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if (value instanceof Prompt prompt) {
				String text = prompt.getName();
				if (prompt.getCategory() != null && !prompt.getCategory().isEmpty()) {
					text += " [" + prompt.getCategory() + "]";
				}
				if (prompt.isFavorite()) {
					text = "★ " + text;
				}
				setText(text);
				setToolTipText("Double-click to copy content");
			}
			return c;
		}
	}
}

