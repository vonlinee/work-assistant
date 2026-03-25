package org.assistant.tools.fileannotate.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * 过滤配置 - 支持按文件类型、文件名、文件大小过滤
 */
@Data
public class FilterConfig {

	/**
	 * 文件扩展名过滤（如 .class, .log）
	 */
	private List<String> excludedExtensions = new ArrayList<>();

	/**
	 * 文件名正则过滤
	 */
	private List<String> excludedNamePatterns = new ArrayList<>();

	/**
	 * 目录名正则过滤
	 */
	private List<String> excludedDirPatterns = new ArrayList<>();

	/**
	 * 最小文件大小（字节），小于此值的文件被忽略，-1 表示不限制
	 */
	private long minFileSize = -1;

	/**
	 * 最大文件大小（字节），大于此值的文件被忽略，-1 表示不限制
	 */
	private long maxFileSize = -1;

	/**
	 * 是否忽略隐藏文件
	 */
	private boolean ignoreHiddenFiles = true;

	/**
	 * 是否忽略隐藏目录
	 */
	private boolean ignoreHiddenDirs = true;

	// 编译后的正则模式缓存
	private transient List<Pattern> compiledNamePatterns;
	private transient List<Pattern> compiledDirPatterns;

	/**
	 * 添加排除的扩展名
	 */
	public void addExcludedExtension(String extension) {
		String ext = extension.startsWith(".") ? extension.toLowerCase() : "." + extension.toLowerCase();
		if (!excludedExtensions.contains(ext)) {
			excludedExtensions.add(ext);
		}
	}

	/**
	 * 添加文件名排除正则
	 */
	public void addExcludedNamePattern(String pattern) {
		if (pattern != null && !pattern.trim().isEmpty()) {
			excludedNamePatterns.add(pattern);
			compiledNamePatterns = null; // 清除缓存
		}
	}

	/**
	 * 添加目录名排除正则
	 */
	public void addExcludedDirPattern(String pattern) {
		if (pattern != null && !pattern.trim().isEmpty()) {
			excludedDirPatterns.add(pattern);
			compiledDirPatterns = null; // 清除缓存
		}
	}

	/**
	 * 检查文件是否应该被排除
	 */
	public boolean shouldExcludeFile(FileNode node) {
		if (node.isDirectory()) {
			return shouldExcludeDirectory(node);
		}

		String fileName = node.getName();

		// 检查隐藏文件
		if (ignoreHiddenFiles && fileName.startsWith(".")) {
			return true;
		}

		// 检查扩展名
		String lowerName = fileName.toLowerCase();
		for (String ext : excludedExtensions) {
			if (lowerName.endsWith(ext)) {
				return true;
			}
		}

		// 检查文件名正则
		List<Pattern> patterns = getCompiledNamePatterns();
		for (Pattern pattern : patterns) {
			if (pattern.matcher(fileName).matches()) {
				return true;
			}
		}

		// 检查文件大小
		long size = node.getSize();
		if (minFileSize >= 0 && size < minFileSize) {
			return true;
		}
		if (maxFileSize >= 0 && size > maxFileSize) {
			return true;
		}

		return false;
	}

	/**
	 * 检查目录是否应该被排除（不扫描其子内容）
	 */
	public boolean shouldExcludeDirectory(FileNode node) {
		String dirName = node.getName();

		// 检查隐藏目录
		if (ignoreHiddenDirs && dirName.startsWith(".")) {
			return true;
		}

		// 检查目录名正则
		List<Pattern> patterns = getCompiledDirPatterns();
		for (Pattern pattern : patterns) {
			if (pattern.matcher(dirName).matches()) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 获取编译后的文件名正则
	 */
	private List<Pattern> getCompiledNamePatterns() {
		if (compiledNamePatterns == null) {
			compiledNamePatterns = new ArrayList<>();
			for (String regex : excludedNamePatterns) {
				try {
					compiledNamePatterns.add(Pattern.compile(regex));
				} catch (PatternSyntaxException e) {
					// 忽略无效的正则
				}
			}
		}
		return compiledNamePatterns;
	}

	/**
	 * 获取编译后的目录名正则
	 */
	private List<Pattern> getCompiledDirPatterns() {
		if (compiledDirPatterns == null) {
			compiledDirPatterns = new ArrayList<>();
			for (String regex : excludedDirPatterns) {
				try {
					compiledDirPatterns.add(Pattern.compile(regex));
				} catch (PatternSyntaxException e) {
					// 忽略无效的正则
				}
			}
		}
		return compiledDirPatterns;
	}

	/**
	 * 清除所有过滤条件
	 */
	public void clear() {
		excludedExtensions.clear();
		excludedNamePatterns.clear();
		excludedDirPatterns.clear();
		minFileSize = -1;
		maxFileSize = -1;
		compiledNamePatterns = null;
		compiledDirPatterns = null;
	}

	/**
	 * 是否有任何过滤条件
	 */
	public boolean hasFilters() {
		return !excludedExtensions.isEmpty() ||
			   !excludedNamePatterns.isEmpty() ||
			   !excludedDirPatterns.isEmpty() ||
			   minFileSize >= 0 ||
			   maxFileSize >= 0;
	}

	/**
	 * 格式化文件大小
	 */
	public static String formatSize(long bytes) {
		if (bytes < 1024) {
			return bytes + " B";
		} else if (bytes < 1024 * 1024) {
			return String.format("%.2f KB", bytes / 1024.0);
		} else if (bytes < 1024 * 1024 * 1024) {
			return String.format("%.2f MB", bytes / (1024.0 * 1024));
		} else {
			return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
		}
	}

	/**
	 * 解析文件大小字符串（如 "10KB", "5MB", "1GB"）
	 */
	public static long parseSize(String sizeStr) {
		if (sizeStr == null || sizeStr.trim().isEmpty()) {
			return -1;
		}
		sizeStr = sizeStr.trim().toUpperCase();
		try {
			if (sizeStr.endsWith("GB")) {
				return (long) (Double.parseDouble(sizeStr.replace("GB", "")) * 1024 * 1024 * 1024);
			} else if (sizeStr.endsWith("MB")) {
				return (long) (Double.parseDouble(sizeStr.replace("MB", "")) * 1024 * 1024);
			} else if (sizeStr.endsWith("KB")) {
				return (long) (Double.parseDouble(sizeStr.replace("KB", "")) * 1024);
			} else if (sizeStr.endsWith("B")) {
				return Long.parseLong(sizeStr.replace("B", ""));
			} else {
				return Long.parseLong(sizeStr);
			}
		} catch (NumberFormatException e) {
			return -1;
		}
	}
}
