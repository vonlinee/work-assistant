package org.assistant.util;

import java.util.Locale;
import java.util.ResourceBundle;

public class Messages {

	private static final ResourceBundle resourceBundle = ResourceBundle.getBundle("message/message", Locale.getDefault());

	public static String getString(String msg) {
		return resourceBundle.getString(msg);
	}
}
