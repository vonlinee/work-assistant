package org.example.workassistant.common.utils;

import org.example.workassistant.utils.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Utils {

    private static final Logger log = LoggerFactory.getLogger(Utils.class);
    static Pattern pattern = Pattern.compile("\\s*|\\t|\\r|\\n");

    /**
     * 打开指定输出文件目录
     *
     * @param outDir 输出文件目录
     * @throws IOException IO错误
     */
    public static void openDirectory(String outDir) throws IOException {
        String osName = System.getProperty("os.name");
        if (osName != null) {
            if (osName.contains("Mac")) {
                Runtime.getRuntime().exec("open " + outDir);
            } else if (osName.contains("Windows")) {
                Runtime.getRuntime().exec(MessageFormat.format("cmd /c start \"\" \"{0}\"", outDir));
            } else {
                log.debug("文件输出目录:{}", outDir);
            }
        } else {
            log.warn("读取操作系统失败");
        }
    }

    public static void trimFields(Object obj) {
        if (obj == null) {
            return;
        }
        for (Field declaredField : obj.getClass().getDeclaredFields()) {
            if (!declaredField.canAccess(obj)) {
                declaredField.setAccessible(true);
            }
            Object value = null;
            try {
                value = declaredField.get(obj);
                if (value instanceof String str) {
                    declaredField.set(obj, StringUtils.trim(str));
                }
            } catch (Exception exception) {
                log.error("faile to trim value {}", value, exception);
            }
        }
    }

    /**
     * 去除不可见字符
     *
     * @param str 字符串
     * @return 输入为空，返回空字符串
     */
    
    public static String removeNonPrintableCharacters(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        // https://stackoverflow.com/questions/6198986/how-can-i-replace-non-printable-unicode-characters-in-java#
        return str.replace("\\p{C}", "");
    }

    /**
     * 去除不可见字符 空格\t、回车\n、换行符\r、制表符\t
     * 笨方法：String s = "你要去除的字符串";
     * 1.去除空格：s = s.replace('\\s','');
     * 2.去除回车：s = s.replace('\n','');
     * 这样也可以把空格和回车去掉，其他也可以照这样做。
     * 注：
     * \n 回车(\u000a)
     * \t 水平制表符(\u0009)
     * \s 空格(\u0008)
     * \r 换行(\u000d)
     *
     * @param str 字符串
     * @return 输入为空，返回空字符串
     */
    
    public static String removeInvisibleCharacters(String str) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        Matcher m = pattern.matcher(str);
        return m.replaceAll("");
    }
}
