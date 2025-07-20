package org.assistant.control;

import org.assistant.ui.controls.Button;
import org.assistant.ui.pane.BorderPane;
import org.assistant.ui.pane.HBox;
import org.assistant.util.SwingUtils;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class TreeDemo2 {
	public static void main(String[] args) {

		// 创建没有父节点和子节点、但允许有子节点的树节点，并使用指定的用户对象对它进行初始化。
		// public DefaultMutableTreeNode(Object userObject)
		DefaultMutableTreeNode node1 = new DefaultMutableTreeNode("软件部");
		node1.add(new DefaultMutableTreeNode(new User("小花")));
		node1.add(new DefaultMutableTreeNode(new User("小虎")));
		node1.add(new DefaultMutableTreeNode(new User("小龙")));

		DefaultMutableTreeNode node2 = new DefaultMutableTreeNode("销售部");
		node2.add(new DefaultMutableTreeNode(new User("小叶")));
		node2.add(new DefaultMutableTreeNode(new User("小雯")));
		node2.add(new DefaultMutableTreeNode(new User("小夏")));

		DefaultMutableTreeNode top = new DefaultMutableTreeNode("职员管理");

		top.add(new DefaultMutableTreeNode(new User("总经理")));
		top.add(node1);
		top.add(node2);

		BorderPane borderPane = new BorderPane();

		final JTree tree = new JTree(top);

		tree.setPreferredSize(new Dimension(400, 300));
		JFrame f = new JFrame("JTreeDemo");

		Button btn = new Button("Add");
		btn.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
				DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
				for (int i = 0; i < 10; i++) {
					root.add(new DefaultMutableTreeNode(new User("Child" + i)));
				}
				System.out.println(root.getChildCount());
				model.nodeStructureChanged(root);
			}
		});

		Button btn1 = new Button("Expand");
		btn1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				for (int i = 0; i < tree.getRowCount(); i++) {
					tree.expandRow(i);
				}
			}
		});

		Button btn2 = new Button("Delete");
		btn2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				TreePath selectedPath = tree.getSelectionPath();
				if (selectedPath == null) {
					return;
				}
				MutableTreeNode leafNode = (MutableTreeNode) selectedPath.getLastPathComponent();
				DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
				model.removeNodeFromParent(leafNode);
			}
		});

		HBox hBox = new HBox();
		hBox.add(btn);
		hBox.add(btn1);
		hBox.add(btn2);
		borderPane.setTop(hBox);
		f.add(borderPane);
		borderPane.setCenter(tree);
		f.setSize(300, 300);
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// 添加选择事件
		tree.addTreeSelectionListener(new TreeSelectionListener() {

			@Override
			public void valueChanged(TreeSelectionEvent e) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
					.getLastSelectedPathComponent();

				if (node == null)
					return;

				Object object = node.getUserObject();
				if (node.isLeaf()) {
					User user = (User) object;
					System.out.println("你选择了：" + user.toString());
				}

			}
		});

		SwingUtils.alignToCenter(f);
	}
}

class User {
	private String name;

	public User(String n) {
		name = n;
	}

	// 重点在toString，节点的显示文本就是toString
	public String toString() {
		return name;
	}
}