package org.assistant.util;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;

public final class SwingUtils {

	public static void copyToSystemClipboard(ClipboardOwner owner, String text) {
		// 获得系统剪贴板
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		// 用拷贝文本框文本实例化StringSelection对象
		StringSelection contents = new StringSelection(text);
		// 设置系统剪贴板内容
		clipboard.setContents(contents, owner);
	}

	public static void showFile(String path) {
		try {
			Desktop.getDesktop().browse(new File(path).toURI());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
