package org.assistant.util;

import org.jdesktop.swingx.JXTreeTable;

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

	public static void expandNode(JXTreeTable treeTable, TreePath parent, boolean recursive) {
		if (recursive) {
			expandAll(treeTable, parent, true);
		} else {
			treeTable.expandPath(parent);
		}
	}

	public static void collapseNode(JXTreeTable treeTable, TreePath parent, boolean recursive) {
		if (recursive) {
			expandAll(treeTable, parent, false);
		} else {
			treeTable.collapsePath(parent);
		}
	}

	private static void expandAll(JXTreeTable treeTable, TreePath parent, boolean expand) {
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		if (node.getChildCount() > 0) {
			for (Enumeration<? extends TreeNode> e = node.children(); e.hasMoreElements(); ) {
				TreeNode n = e.nextElement();
				TreePath path = parent.pathByAddingChild(n);
				expandAll(treeTable, path, expand);
			}
		}
		if (expand) {
			treeTable.expandPath(parent);
		} else {
			treeTable.collapsePath(parent);
		}
	}

	/**
	 * 从JComponent向上遍历父容器，获取其所属的Frame（JFrame）
	 *
	 * @param component 目标组件（不能为null）
	 * @return 所属的Frame（可能为null，如组件未添加到任何Frame）
	 */
	public static Frame getParentFrame(JComponent component) {
		if (component == null) {
			throw new IllegalArgumentException("组件不能为null");
		}
		// 1. 先获取组件的最顶层父容器（Window）
		Window window = SwingUtilities.getWindowAncestor(component);
		// 2. 判断Window是否为Frame/JFrame
		if (window instanceof Frame) {
			return (Frame) window;
		}
		// 3. 特殊情况：Window是Dialog，获取其所属的Owner Frame
		if (window instanceof Dialog dialog) {
			Window owner = dialog.getOwner();
			if (owner instanceof Frame) {
				return (Frame) owner;
			}
		}
		// 4. 无所属Frame（如组件未添加到任何窗口）
		return null;
	}

	/**
	 * 简化版：直接获取JComponent所属的JFrame（强制转换）
	 *
	 * @param component 目标组件
	 * @return 所属的JFrame（null表示无）
	 */
	public static JFrame getParentJFrame(JComponent component) {
		Frame frame = getParentFrame(component);
		return (frame instanceof JFrame) ? (JFrame) frame : null;
	}

	/**
	 * 安全获取：如果组件无所属Frame，返回默认Frame（避免null）
	 *
	 * @param component    目标组件
	 * @param defaultFrame 默认Frame
	 * @return 所属Frame或默认Frame
	 */
	public static Frame getParentFrameOrElse(JComponent component, Frame defaultFrame) {
		Frame frame = getParentFrame(component);
		return frame != null ? frame : defaultFrame;
	}
}
