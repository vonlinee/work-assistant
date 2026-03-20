package org.assistant.editor;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import java.awt.*;

/**
 * 基于RSyntaxTextArea实现CodeEditor接口，适配多语言语法高亮
 */
public class RSyntaxCodeEditor extends JPanel implements CodeEditor {

	// 核心编辑组件
	private final RSyntaxTextArea textArea;
	// 当前选中的语言
	private Language currentLanguage;

	// 构造函数：初始化编辑器默认配置
	public RSyntaxCodeEditor() {
		this.textArea = new RSyntaxTextArea();

		setLayout(new BorderLayout());

		textArea.setSize(500, 300);

		// 基础配置（代码编辑最优实践）
		this.textArea.setLineWrap(false);          // 关闭自动换行（代码编辑推荐）
		this.textArea.setCodeFoldingEnabled(true); // 启用代码折叠（支持多语言）
		this.textArea.setTabSize(4);               // Tab缩进4个空格
		this.textArea.setFont(new Font("Consolas", Font.PLAIN, 14)); // 等宽字体
		this.textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE); // 默认无高亮

		add(textArea, BorderLayout.CENTER);
		// 默认语言：无（可改为JAVA）
		this.currentLanguage = Language.JAVA;
	}

	/**
	 * 设置编辑器语言（核心：映射RSyntax的语法常量）
	 *
	 * @param language 目标语言
	 * @return 设置后的语言（链式调用）
	 */
	@Override
	public Language setLanguage(Language language) {
		// 空值处理：重置为无高亮
		if (language == null) {
			this.currentLanguage = null;
			textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
			return null;
		}

		// 语言枚举 -> RSyntax语法常量 映射
		String syntaxStyle = switch (language) {
			case JAVA -> SyntaxConstants.SYNTAX_STYLE_JAVA;
			case PYTHON -> SyntaxConstants.SYNTAX_STYLE_PYTHON;
			case JAVASCRIPT -> SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT;
			case PHP -> SyntaxConstants.SYNTAX_STYLE_PHP;
			case C -> SyntaxConstants.SYNTAX_STYLE_C;
			case CPP -> SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS;
			case C_SHARP -> SyntaxConstants.SYNTAX_STYLE_CSHARP;
			case RUBY -> SyntaxConstants.SYNTAX_STYLE_RUBY;
			case GO -> SyntaxConstants.SYNTAX_STYLE_GO;
			case KOTLIN -> SyntaxConstants.SYNTAX_STYLE_KOTLIN;
			case SCALA -> SyntaxConstants.SYNTAX_STYLE_SCALA;
			case DART -> SyntaxConstants.SYNTAX_STYLE_DART;
			case LUA -> SyntaxConstants.SYNTAX_STYLE_LUA;
		};

		// 更新当前语言并设置高亮
		this.currentLanguage = language;
		textArea.setSyntaxEditingStyle(syntaxStyle);

		// 链式调用：返回设置后的语言
		return this.currentLanguage;
	}

	/**
	 * 获取当前编辑器的语言
	 *
	 * @return 当前选中的Language（null表示无高亮）
	 */
	@Override
	public Language getCurrentLanguage() {
		return this.currentLanguage;
	}

	@Override
	public void setSize(int width, int height) {
		super.setSize(width, height);
	}

	/**
	 * 获取编辑器完整文本
	 *
	 * @return 文本内容（null时返回空字符串）
	 */
	@Override
	public String getText() {
		String text = textArea.getText();
		return text == null ? "" : text;
	}

	/**
	 * 设置编辑器文本内容
	 *
	 * @param text 要设置的文本
	 * @return 设置后的实际文本（符合接口契约）
	 */
	@Override
	public String setText(String text) {
		// 空值处理：设置为空字符串
		String targetText = text == null ? "" : text;
		// 设置文本并清空编辑历史（避免撤销干扰）
		textArea.setText(targetText);
		textArea.discardAllEdits();
		// 返回实际设置的文本（确保和编辑器内容一致）
		return textArea.getText();
	}

	// ---------------------- 扩展方法（增强实用性） ----------------------

	/**
	 * 获取底层RSyntaxTextArea组件（用于嵌入Swing窗口、添加行号等）
	 */
	public RSyntaxTextArea getTextArea() {
		return textArea;
	}

	/**
	 * 启用/禁用代码折叠
	 */
	public void setCodeFoldingEnabled(boolean enabled) {
		textArea.setCodeFoldingEnabled(enabled);
	}

	/**
	 * 设置编辑器字体
	 */
	@Override
	public void setFont(Font font) {
		if (textArea == null) {
			// TODO 启动时会调用此方法，但此时textArea为null
			return;
		}
		textArea.setFont(font);
	}

	/**
	 * 设置是否显示行号（需配合RTextScrollPane使用）
	 * 注：行号由RTextScrollPane控制，此处为便捷封装
	 */
	public void setLineNumbersEnabled(boolean enabled, RTextScrollPane scrollPane) {
		if (scrollPane != null) {
			scrollPane.setLineNumbersEnabled(enabled);
		}
	}
}