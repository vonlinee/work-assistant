package org.example.workassistant.fxui.tools.mybatis;

import org.example.workassistant.sdk.util.Visitable;
import org.example.workassistant.sdk.util.Visitor;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * 树节点
 * LinkedHashSet保持插入顺序
 *
 * @param <T> 树节点的数据类型
 */
public class TreeNode<T> implements Visitable<T> {

    /**
     * children不为null，ElementPlus Table会认为还是有子项
     */
    private List<TreeNode<T>> children;

    /**
     * 节点携带的数据项
     */
    private T data;

    /**
     * 父节点
     */
    private TreeNode<T> parent;

    public TreeNode(T data) {
        this.data = data;
    }

    @Override
    public void accept(Visitor<T> visitor) {
        // 当前节点
        visitor.visitData(this, data);
        // 子节点
        visitChildren(visitor);
    }

    protected void visitChildren(Visitor<T> visitor) {
        for (TreeNode<T> child : children) {
            // 遍历完一颗子树，产生一个新的Visitor
            Visitor<T> newVisitor = visitor.visitTree(child);
            child.accept(newVisitor);
        }
    }

    public TreeNode<T> addChild(T data) {
        if (children == null) {
            children = new LinkedList<>();
        }
        for (TreeNode<T> child : children) {
            if (Objects.equals(child.data, data)) {
                return child;
            }
        }
        return addChild(new TreeNode<>(data));
    }

    public TreeNode<T> addChild(TreeNode<T> child) {
        if (children == null) {
            children = new LinkedList<>();
        }
        children.add(child);
        return child;
    }

    /**
     * 如果不是懒加载的话，后端不要设置hasChildren 这个属性，要不然不能树形显示；如果是懒加载，则需要设置hasChildren字段。
     *
     * @return 是否有子元素
     */
    public boolean hasChildren() {
        if (children == null) {
            return false;
        }
        return !this.children.isEmpty();
    }

    public List<TreeNode<T>> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNode<T>> children) {
        this.children = children;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public TreeNode<T> getParent() {
        return parent;
    }

    public void setParent(TreeNode<T> parent) {
        this.parent = parent;
    }
}
