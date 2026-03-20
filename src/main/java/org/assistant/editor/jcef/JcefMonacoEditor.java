package org.assistant.editor.jcef;

import org.assistant.editor.CodeEditor;
import org.assistant.editor.Language;
import org.assistant.jcef.JcefBootstrap;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

public class JcefMonacoEditor extends JPanel implements CodeEditor {

	private final static String EDITOR_HTML_RESOURCE_LOCATION = "/monaco-editor-0.20.0/index.html";

	private Language language;

	CefApp cefApp;
	CefClient cefClient;
	CefBrowser cefBrowser;

	public JcefMonacoEditor() {
		String url = getClass().getResource(EDITOR_HTML_RESOURCE_LOCATION).toExternalForm();
		this.setLayout(new BorderLayout());
		url = "file:///D:/Develop/Code/work-assistant/target/classes/monaco-editor-0.20.0/index.html";

		System.out.println(new File(url).exists());

		// 2. 初始化CefApp（全局唯一，必须先启动）
		cefApp = JcefBootstrap.setup();
		// 3. 创建CefClient（浏览器客户端，处理事件）
		cefClient = cefApp.createClient();
		// 4. 创建浏览器实例（参数：是否透明、初始URL）
		// 初始URL可以是本地文件（file:///D:/test.html）、网络地址、或空白（about:blank）
		cefBrowser = cefClient.createBrowser(url, false, false);
		// 5. 获取浏览器的Swing组件，嵌入窗口
		Component browserComponent = cefBrowser.getUIComponent();
		add(browserComponent, BorderLayout.CENTER);

		// 2. 系统剪贴板（Swing版）
		Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		// 3. 你的剪贴板处理工具类（需适配Swing的Clipboard）
		SystemClipboardWrapper swingClipboardWrapper = new SystemClipboardWrapper(systemClipboard);

		// --------------------------
		// Swing版：按键按下事件监听（对应JavaFX的KEY_PRESSED）
		// --------------------------
		// 1. 方式1：直接添加KeyListener（简单直观，适合基础场景）
		browserComponent.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				try {
					// 步骤1：执行JS脚本获取选中内容（JCEF版）
					// 替换为JCEF执行JS的API（区别于JavaFX的engine.executeScript）
					Object selectedText = executeJavascriptInJCEF(cefBrowser,
						"editorView.getModel().getValueInRange(editorView.getSelection())");

					// 步骤2：处理复制/剪切按键事件（适配Swing的KeyEvent）
					swingClipboardWrapper.handleCopyCutKeyEvent(e, selectedText);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		// 2. 方式2：KeyBinding（推荐，Swing最佳实践，支持焦点/快捷键）
		// 适用于需要绑定特定快捷键（如Ctrl+C）的场景
		int COPY_KEY = KeyEvent.VK_C;
		InputMap inputMap = ((JComponent) browserComponent).getInputMap(JComponent.WHEN_FOCUSED);
		ActionMap actionMap = ((JComponent) browserComponent).getActionMap();

		// 绑定Ctrl+C
		inputMap.put(KeyStroke.getKeyStroke(COPY_KEY, KeyEvent.CTRL_DOWN_MASK), "copyAction");
		actionMap.put("copyAction", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Object selectedText = executeJavascriptInJCEF(cefBrowser,
					"editorView.getModel().getValueInRange(editorView.getSelection())");
				swingClipboardWrapper.handleCopy(selectedText); // 单独处理复制逻辑
			}
		});
	}

	@Override
	public Language setLanguage(Language language) {
		Language oldLanguage = this.language;
		this.language = language;
		return oldLanguage;
	}

	@Override
	public Language getCurrentLanguage() {
		return language;
	}

	@Override
	public String getText() {
		return "";
	}

	@Override
	public String setText(String text) {
		return "";
	}

	// --------------------------
// 核心工具方法：JCEF执行JS脚本（替代JavaFX的engine.executeScript）
// --------------------------
	private Object executeJavascriptInJCEF(CefBrowser browser, String jsCode) {
		// JCEF执行JS的异步/同步方式（根据需求选择）
		// 方式1：同步执行（简单，适合短脚本）
		final Object[] result = new Object[1];
		browser.executeJavaScript(
			jsCode,          // 要执行的JS代码
			browser.getURL(),// 脚本执行的上下文URL
			0                // 起始行号
		);

		// 注意：JCEF executeJavaScript是异步的，如果需要同步结果，需短暂等待（或用CountDownLatch）
		try {
			Thread.sleep(100); // 按需调整等待时间（根据脚本执行耗时）
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return result[0];
	}
}
