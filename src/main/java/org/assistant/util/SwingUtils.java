package org.assistant.util;

import javax.swing.*;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

public final class SwingUtils {

	public static void copyToSystemClipboard(ClipboardOwner owner, String text) {
		// 获得系统剪贴板
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		// 用拷贝文本框文本实例化StringSelection对象
		StringSelection contents = new StringSelection(text);
		// 设置系统剪贴板内容
		clipboard.setContents(contents, owner);
	}

	public static void showFile(String path) {
		try {
			Desktop.getDesktop().browse(new File(path).toURI());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void alignToCenter(Window window) {
		if (window == null) {
			return;
		}
		window.setLocationRelativeTo(null);
	}

	public static Dimension getScreenSize() {
		return Toolkit.getDefaultToolkit().getScreenSize();
	}

	public static void setScreenRatioSize(Window window, double ratio) {
		Dimension screenSize = getScreenSize();
		window.setSize((int) (screenSize.getWidth() * ratio), (int) (screenSize.getHeight() * ratio));
	}

	public static void expandTree(JTree tree) {
		TreeNode root = (TreeNode) tree.getModel().getRoot();
		expandAll(tree, new TreePath(root), true);
	}

	private static void expandAll(JTree tree, TreePath parent, boolean expand) {
		// Traverse children
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		if (node.getChildCount() >= 0) {
			for (Enumeration<? extends TreeNode> e = node.children(); e.hasMoreElements(); ) {
				TreeNode n = e.nextElement();
				TreePath path = parent.pathByAddingChild(n);
				expandAll(tree, path, expand);
			}
		}
		// Expansion or collapse must be done bottom-up
		if (expand) {
			tree.expandPath(parent);
		} else {
			tree.collapsePath(parent);
		}
	}

	public static void expandAll(JTree tree) {
		for (int i = 0; i < tree.getRowCount(); i++) {
			tree.expandRow(i);
		}
	}
}
