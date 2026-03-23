package org.assistant.tools.mybatis;

import org.jdesktop.swingx.treetable.DefaultTreeTableModel;
import org.jdesktop.swingx.treetable.MutableTreeTableNode;

import java.util.List;

class SwingX {

	/**
	 * 替换 ParamNode 根节点的所有子节点
	 * @param rootNode 待替换子节点的根节点（不能为空）
	 * @param newChildNodes 新的子节点列表（可为空，空则清空根节点）
	 * @param treeModel 树形组件的 Model（用于刷新界面，如 TreeTable 的 TreeModel）
	 */
	public static void replaceRootChildNodes(ParamNode rootNode,
																					 List<ParamNode> newChildNodes,
																					 DefaultTreeTableModel treeModel) {
		// 1. 校验参数
		if (rootNode == null) {
			throw new IllegalArgumentException("根节点 rootNode 不能为空");
		}
		if (treeModel == null) {
			throw new IllegalArgumentException("树形模型 treeModel 不能为空（需传递 TreeTable/Tree 的 Model）");
		}

		// 2. 清空根节点原有子节点（核心：先移除所有旧节点）
		clearRootChildNodes(rootNode, treeModel);

		// 3. 批量添加新子节点
		if (newChildNodes != null && !newChildNodes.isEmpty()) {
			for (ParamNode newChild : newChildNodes) {
				rootNode.addChild(newChild);
			}
		}
	}

	/**
	 * 清空根节点的所有子节点（独立方法，可单独调用）
	 */
	public static void clearRootChildNodes(MutableTreeTableNode rootNode, DefaultTreeTableModel treeModel) {
		if (rootNode == null || treeModel == null) {
			return;
		}
		// 反向遍历移除子节点（避免索引错乱）
		while (rootNode.getChildCount() > 0) {
			ParamNode oldChild = (ParamNode) rootNode.getChildAt(0);
			// 从根节点移除子节点
			rootNode.remove(oldChild);
		}
	}
}
