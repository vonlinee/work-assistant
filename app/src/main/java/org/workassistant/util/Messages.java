package org.workassistant.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 从配置文件中加载所有的提示文本
 */
public class Messages {

    private Messages() {
    }

    private static final Properties messages = new Properties();

    static {
        try (InputStream inputStream = Messages.class.getResourceAsStream("messages/message.properties")) {
            messages.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getString(String id) {
        final Object text = messages.get(id);
        if (text == null) {
            return "";
        }
        return String.valueOf(text);
    }
}
