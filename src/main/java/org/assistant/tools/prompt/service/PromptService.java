package org.assistant.tools.prompt.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.assistant.tools.prompt.model.Prompt;
import org.assistant.util.ULID;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 提示词服务类 - 提供增删改查功能
 */
public class PromptService {

	private static final String DATA_FILE = System.getProperty("user.home") + File.separator + ".work-assistant" + File.separator + "prompts.json";
	private static final Gson GSON = new GsonBuilder()
		.setPrettyPrinting()
		.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
		.create();

	private List<Prompt> prompts;
	private final File dataFile;

	public PromptService() {
		this.dataFile = new File(DATA_FILE);
		loadPrompts();
	}

	/**
	 * 加载所有提示词
	 */
	private void loadPrompts() {
		if (!dataFile.exists()) {
			prompts = new ArrayList<>();
			savePrompts();
			return;
		}

		try (FileReader reader = new FileReader(dataFile, StandardCharsets.UTF_8)) {
			Type listType = new TypeToken<List<Prompt>>() {}.getType();
			prompts = GSON.fromJson(reader, listType);
			if (prompts == null) {
				prompts = new ArrayList<>();
			}
		} catch (IOException e) {
			prompts = new ArrayList<>();
		}
	}

	/**
	 * 保存所有提示词到文件
	 */
	private void savePrompts() {
		try {
			File parentDir = dataFile.getParentFile();
			if (!parentDir.exists()) {
				parentDir.mkdirs();
			}

			try (FileWriter writer = new FileWriter(dataFile, StandardCharsets.UTF_8)) {
				GSON.toJson(prompts, writer);
			}
		} catch (IOException e) {
			throw new RuntimeException("Failed to save prompts", e);
		}
	}

	/**
	 * 创建新提示词
	 */
	public Prompt createPrompt(String name, String category, String content, String description) {
		Prompt prompt = new Prompt();
		prompt.setId(ULID.randomULID());
		prompt.setName(name);
		prompt.setCategory(category);
		prompt.setContent(content);
		prompt.setDescription(description);
		prompt.setCreateTime(LocalDateTime.now());
		prompt.setUpdateTime(LocalDateTime.now());

		prompts.add(prompt);
		savePrompts();
		return prompt;
	}

	/**
	 * 更新提示词
	 */
	public boolean updatePrompt(String id, String name, String category, String content, String description) {
		Optional<Prompt> optional = prompts.stream()
			.filter(p -> p.getId().equals(id))
			.findFirst();

		if (optional.isPresent()) {
			Prompt prompt = optional.get();
			prompt.setName(name);
			prompt.setCategory(category);
			prompt.setContent(content);
			prompt.setDescription(description);
			prompt.setUpdateTime(LocalDateTime.now());
			savePrompts();
			return true;
		}
		return false;
	}

	/**
	 * 删除提示词
	 */
	public boolean deletePrompt(String id) {
		boolean removed = prompts.removeIf(p -> p.getId().equals(id));
		if (removed) {
			savePrompts();
		}
		return removed;
	}

	/**
	 * 根据ID获取提示词
	 */
	public Optional<Prompt> getPromptById(String id) {
		return prompts.stream()
			.filter(p -> p.getId().equals(id))
			.findFirst();
	}

	/**
	 * 获取所有提示词
	 */
	public List<Prompt> getAllPrompts() {
		return new ArrayList<>(prompts);
	}

	/**
	 * 根据分类获取提示词
	 */
	public List<Prompt> getPromptsByCategory(String category) {
		return prompts.stream()
			.filter(p -> p.getCategory() != null && p.getCategory().equals(category))
			.collect(Collectors.toList());
	}

	/**
	 * 搜索提示词
	 */
	public List<Prompt> searchPrompts(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return getAllPrompts();
		}

		String lowerKeyword = keyword.toLowerCase();
		return prompts.stream()
			.filter(p -> (p.getName() != null && p.getName().toLowerCase().contains(lowerKeyword)) ||
						 (p.getContent() != null && p.getContent().toLowerCase().contains(lowerKeyword)) ||
						 (p.getDescription() != null && p.getDescription().toLowerCase().contains(lowerKeyword)) ||
						 (p.getCategory() != null && p.getCategory().toLowerCase().contains(lowerKeyword)))
			.collect(Collectors.toList());
	}

	/**
	 * 获取所有分类
	 */
	public List<String> getAllCategories() {
		return prompts.stream()
			.map(Prompt::getCategory)
			.filter(c -> c != null && !c.isEmpty())
			.distinct()
			.sorted()
			.collect(Collectors.toList());
	}

	/**
	 * 切换收藏状态
	 */
	public boolean toggleFavorite(String id) {
		Optional<Prompt> optional = prompts.stream()
			.filter(p -> p.getId().equals(id))
			.findFirst();

		if (optional.isPresent()) {
			Prompt prompt = optional.get();
			prompt.setFavorite(!prompt.isFavorite());
			prompt.setUpdateTime(LocalDateTime.now());
			savePrompts();
			return true;
		}
		return false;
	}

	/**
	 * 获取收藏的提示词
	 */
	public List<Prompt> getFavoritePrompts() {
		return prompts.stream()
			.filter(Prompt::isFavorite)
			.collect(Collectors.toList());
	}

	/**
	 * 添加新分类（仅用于验证，实际分类是从提示词中提取的）
	 */
	public void addCategory(String category) {
		// 分类是动态从提示词中提取的，此方法仅用于验证分类是否已存在
		// 实际添加分类是通过创建/更新提示词时指定新分类实现的
	}

	/**
	 * 重命名分类
	 */
	public void renameCategory(String oldName, String newName) {
		boolean updated = false;
		for (Prompt prompt : prompts) {
			if (oldName.equals(prompt.getCategory())) {
				prompt.setCategory(newName);
				prompt.setUpdateTime(LocalDateTime.now());
				updated = true;
			}
		}
		if (updated) {
			savePrompts();
		}
	}

	/**
	 * 删除分类（将该分类下的所有提示词设为无分类）
	 */
	public void deleteCategory(String category) {
		boolean updated = false;
		for (Prompt prompt : prompts) {
			if (category.equals(prompt.getCategory())) {
				prompt.setCategory(null);
				prompt.setUpdateTime(LocalDateTime.now());
				updated = true;
			}
		}
		if (updated) {
			savePrompts();
		}
	}
}
