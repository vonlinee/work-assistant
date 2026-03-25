package org.assistant.tools.fileannotate.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.assistant.tools.fileannotate.model.FileNode;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文件注释服务 - 管理文件注释的持久化
 */
public class FileAnnotationService {

	private static final String ANNOTATION_FILE = System.getProperty("user.home") + File.separator + ".work-assistant" + File.separator + "file-annotations.json";
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	private Map<String, String> annotations;
	private final File dataFile;

	public FileAnnotationService() {
		this.dataFile = new File(ANNOTATION_FILE);
		loadAnnotations();
	}

	/**
	 * 加载注释
	 */
	private void loadAnnotations() {
		if (!dataFile.exists()) {
			annotations = new HashMap<>();
			saveAnnotations();
			return;
		}

		try (FileReader reader = new FileReader(dataFile, StandardCharsets.UTF_8)) {
			Type type = new TypeToken<Map<String, String>>() {}.getType();
			annotations = GSON.fromJson(reader, type);
			if (annotations == null) {
				annotations = new HashMap<>();
			}
		} catch (IOException e) {
			annotations = new HashMap<>();
		}
	}

	/**
	 * 保存注释
	 */
	private void saveAnnotations() {
		try {
			File parentDir = dataFile.getParentFile();
			if (!parentDir.exists()) {
				parentDir.mkdirs();
			}

			try (FileWriter writer = new FileWriter(dataFile, StandardCharsets.UTF_8)) {
				GSON.toJson(annotations, writer);
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to save annotations", e);
		}
	}

	/**
	 * 获取注释
	 */
	public String getAnnotation(String path) {
		return annotations.get(path);
	}

	/**
	 * 设置注释
	 */
	public void setAnnotation(String path, String annotation) {
		if (annotation == null || annotation.trim().isEmpty()) {
			annotations.remove(path);
		} else {
			annotations.put(path, annotation.trim());
		}
		saveAnnotations();
	}

	/**
	 * 删除注释
	 */
	public void removeAnnotation(String path) {
		annotations.remove(path);
		saveAnnotations();
	}

	/**
	 * 应用注释到文件树
	 */
	public void applyAnnotations(FileNode root) {
		List<FileNode> allNodes = root.getAllNodes();
		for (FileNode node : allNodes) {
			String annotation = annotations.get(node.getPath());
			if (annotation != null) {
				node.setAnnotation(annotation);
			}
		}
	}

	/**
	 * 从文件树收集注释
	 */
	public void collectAnnotations(FileNode root) {
		List<FileNode> allNodes = root.getAllNodes();
		for (FileNode node : allNodes) {
			if (node.getAnnotation() != null && !node.getAnnotation().trim().isEmpty()) {
				annotations.put(node.getPath(), node.getAnnotation().trim());
			} else {
				annotations.remove(node.getPath());
			}
		}
		saveAnnotations();
	}

	/**
	 * 清除所有注释
	 */
	public void clearAllAnnotations() {
		annotations.clear();
		saveAnnotations();
	}
}
