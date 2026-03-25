package org.assistant.tools.prompt.model;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 提示词模型
 */
@Data
public class Prompt {

	/**
	 * 唯一标识
	 */
	private String id;

	/**
	 * 提示词名称
	 */
	private String name;

	/**
	 * 提示词分类/标签
	 */
	private String category;

	/**
	 * 提示词内容
	 */
	private String content;

	/**
	 * 提示词描述
	 */
	private String description;

	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;

	/**
	 * 更新时间
	 */
	private LocalDateTime updateTime;

	/**
	 * 是否收藏
	 */
	private boolean favorite;

	public Prompt() {
		this.createTime = LocalDateTime.now();
		this.updateTime = LocalDateTime.now();
	}

	public Prompt(String id, String name, String category, String content, String description) {
		this();
		this.id = id;
		this.name = name;
		this.category = category;
		this.content = content;
		this.description = description;
	}

	public void updateTime() {
		this.updateTime = LocalDateTime.now();
	}
}
