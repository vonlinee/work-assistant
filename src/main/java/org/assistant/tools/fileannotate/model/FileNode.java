package org.assistant.tools.fileannotate.model;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 文件节点模型 - 用于树形结构展示
 */
@Setter
@Getter
public class FileNode {

	private String id = UUID.randomUUID().toString();

	/**
	 * 文件/目录名称
	 */
	private String name;

	/**
	 * 完整路径
	 */
	private String path;

	/**
	 * 是否是目录
	 */
	private boolean directory;

	/**
	 * 文件大小（字节）
	 */
	private long size;

	/**
	 * 注释说明
	 */
	private String annotation;

	/**
	 * 是否选中（用于输出控制）
	 */
	private boolean selected = true;

	/**
	 * 是否被过滤排除（不显示在树中，也不输出）
	 */
	private boolean excluded = false;

	/**
	 * 父节点
	 */
	private FileNode parent;

	/**
	 * 子节点列表
	 */
	private List<FileNode> children = new ArrayList<>();

	/**
	 * 层级深度
	 */
	private int depth;

	public FileNode() {
	}

	public FileNode(String name, String path, boolean directory) {
		this.name = name;
		this.path = path;
		this.directory = directory;
	}

	public FileNode(File file, int depth) {
		this.name = file.getName();
		this.path = file.getAbsolutePath();
		this.directory = file.isDirectory();
		this.size = file.isFile() ? file.length() : 0;
		this.depth = depth;
	}

	/**
	 * 添加子节点
	 */
	public void addChild(FileNode child) {
		child.setParent(this);
		child.setDepth(this.depth + 1);
		children.add(child);
	}

	/**
	 * 是否有子节点
	 */
	public boolean hasChildren() {
		return !children.isEmpty();
	}

	/**
	 * 获取显示名称（带注释）
	 */
	public String getDisplayName() {
		return name;
	}

	/**
	 * 获取文件大小显示
	 */
	public String getSizeDisplay() {
		if (directory) {
			return "<DIR>";
		}
		return formatFileSize(size);
	}

	/**
	 * 格式化文件大小
	 */
	private String formatFileSize(long size) {
		if (size < 1024) {
			return size + " B";
		} else if (size < 1024 * 1024) {
			return String.format("%.2f KB", size / 1024.0);
		} else if (size < 1024 * 1024 * 1024) {
			return String.format("%.2f MB", size / (1024.0 * 1024));
		} else {
			return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
		}
	}

	/**
	 * 递归获取所有选中的节点
	 */
	public List<FileNode> getSelectedNodes() {
		List<FileNode> result = new ArrayList<>();
		if (selected) {
			result.add(this);
		}
		for (FileNode child : children) {
			result.addAll(child.getSelectedNodes());
		}
		return result;
	}

	/**
	 * 递归获取所有节点
	 */
	public List<FileNode> getAllNodes() {
		List<FileNode> result = new ArrayList<>();
		result.add(this);
		for (FileNode child : children) {
			result.addAll(child.getAllNodes());
		}
		return result;
	}

	/**
	 * 根据路径查找节点
	 */
	public FileNode findByPath(String targetPath) {
		if (this.path.equals(targetPath)) {
			return this;
		}
		for (FileNode child : children) {
			FileNode found = child.findByPath(targetPath);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object object) {
		if (object == null || getClass() != object.getClass()) return false;
		FileNode fileNode = (FileNode) object;
		return Objects.equals(id, fileNode.id);
	}
}
