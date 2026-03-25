package org.assistant.tools.fileannotate.service;

import org.assistant.tools.fileannotate.model.FileNode;
import org.assistant.tools.fileannotate.model.FilterConfig;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * 文件扫描器 - 扫描目录生成文件树
 */
public class FileScanner {

	/**
	 * 扫描目录
	 * @param rootDir 根目录
	 * @return 文件树根节点
	 */
	public FileNode scanDirectory(File rootDir) {
		if (!rootDir.exists() || !rootDir.isDirectory()) {
			throw new IllegalArgumentException("Invalid directory: " + rootDir);
		}
		FileNode root = new FileNode(rootDir, 0);
		scanRecursive(rootDir, root, 0);
		return root;
	}

	/**
	 * 递归扫描
	 */
	private void scanRecursive(File dir, FileNode parentNode, int depth) {
		File[] files = dir.listFiles();
		if (files == null) {
			return;
		}
		// 排序：目录在前，文件在后，按名称排序
		Arrays.sort(files, (f1, f2) -> {
			if (f1.isDirectory() && !f2.isDirectory()) {
				return -1;
			}
			if (!f1.isDirectory() && f2.isDirectory()) {
				return 1;
			}
			return f1.getName().compareToIgnoreCase(f2.getName());
		});

		for (File file : files) {
			// 跳过隐藏文件（可选）
			if (file.isHidden()) {
				continue;
			}

			FileNode node = new FileNode(file, depth + 1);
			parentNode.addChild(node);

			if (file.isDirectory()) {
				scanRecursive(file, node, depth + 1);
			}
		}
	}

	/**
	 * 扫描目录（带过滤配置）
	 * @param rootDir 根目录
	 * @param filterConfig 过滤配置
	 * @return 文件树根节点
	 */
	public FileNode scanDirectory(File rootDir, FilterConfig filterConfig) {
		if (!rootDir.exists() || !rootDir.isDirectory()) {
			throw new IllegalArgumentException("Invalid directory: " + rootDir);
		}

		FileNode root = new FileNode(rootDir, 0);
		if (filterConfig != null) {
			root.setExcluded(filterConfig.shouldExcludeDirectory(root));
		}
		scanRecursive(rootDir, root, 0, filterConfig);
		return root;
	}

	/**
	 * 递归扫描（带过滤配置）
	 */
	private void scanRecursive(File dir, FileNode parentNode, int depth, FilterConfig filterConfig) {
		File[] files = dir.listFiles();
		if (files == null) {
			return;
		}

		// 排序
		Arrays.sort(files, (f1, f2) -> {
			if (f1.isDirectory() && !f2.isDirectory()) {
				return -1;
			}
			if (!f1.isDirectory() && f2.isDirectory()) {
				return 1;
			}
			return f1.getName().compareToIgnoreCase(f2.getName());
		});

		for (File file : files) {
			FileNode node = new FileNode(file, depth + 1);

			// 应用过滤配置
			if (filterConfig != null) {
				if (file.isDirectory()) {
					if (filterConfig.shouldExcludeDirectory(node)) {
						continue; // 跳过整个目录
					}
				} else {
					if (filterConfig.shouldExcludeFile(node)) {
						continue; // 跳过文件
					}
				}
			} else {
				// 默认过滤隐藏文件
				if (file.isHidden()) {
					continue;
				}
			}

			parentNode.addChild(node);

			if (file.isDirectory()) {
				scanRecursive(file, node, depth + 1, filterConfig);
			}
		}
	}
}
