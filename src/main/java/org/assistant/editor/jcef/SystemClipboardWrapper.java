package org.assistant.editor.jcef;

import java.awt.datatransfer.Clipboard;
import java.awt.event.KeyEvent;

// 假设你的剪贴板处理类（适配Swing KeyEvent）
class SystemClipboardWrapper {
	private final Clipboard systemClipboard;

	public SystemClipboardWrapper(Clipboard systemClipboard) {
		this.systemClipboard = systemClipboard;
	}

	// 处理复制/剪切按键事件（适配Swing KeyEvent）
	public void handleCopyCutKeyEvent(KeyEvent event, Object content) {
		// 示例逻辑：判断按键（Ctrl+C复制，Ctrl+X剪切）
		if (event.isControlDown()) {
			if (event.getKeyCode() == KeyEvent.VK_C) {
				// 复制逻辑：将content写入剪贴板
				copyToClipboard(content);
				event.consume(); // 消费事件，避免默认行为
			} else if (event.getKeyCode() == KeyEvent.VK_X) {
				// 剪切逻辑
				cutToClipboard(content);
				event.consume();
			}
		}
	}

	// 复制到剪贴板（Swing版实现）
	private void copyToClipboard(Object content) {
		if (content == null) return;
		// 将内容转换为Swing支持的Transferable
		String text = content.toString();
		java.awt.datatransfer.StringSelection selection = new java.awt.datatransfer.StringSelection(text);
		systemClipboard.setContents(selection, null);
	}

	// 剪切逻辑（按需实现）
	private void cutToClipboard(Object content) {
		copyToClipboard(content);
		// 额外添加：删除原内容的逻辑（需调用JS实现）
		// cefBrowser.executeJavaScript("editorView.getModel().deleteSelection()", ...);
	}

	// 单独的复制方法（适配KeyBinding）
	public void handleCopy(Object content) {
		copyToClipboard(content);
	}
}