package io.devpl.sdk.util;

public class HtmlUtils {
    public static final String RE_HTML_MARK = "(<[^<]*?>)|(<\\s*?/[^<]*?>)|(<[^<]*?/\\s*?>)";

    /**
     * 清除所有HTML标签，但是不删除标签内的内容
     *
     * @param content 文本
     * @return 清除标签后的文本
     */
    public static String cleanHtmlTag(String content) {
        return content.replaceAll(RE_HTML_MARK, "");
    }
}
