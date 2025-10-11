package org.assistant.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HtmlTextExtractor {

    /**
     * 提取 HTML 中的所有文本内容（包含样式和脚本文本）
     * @param html HTML 字符串
     * @return 纯文本内容
     */
    public static String extractAllText(String html) {
        if (html == null || html.trim().isEmpty()) {
            return "";
        }
        try {
            Document doc = Jsoup.parse(html);
            return doc.text();
        } catch (Exception e) {
            // 如果解析失败，尝试简单清理 HTML 标签
            return simpleHtmlClean(html);
        }
    }

    /**
     * 提取主体内容文本（排除 head、script、style 等标签）
     * @param html HTML 字符串
     * @return 主体文本内容
     */
    public static String extractBodyText(String html) {
        if (html == null || html.trim().isEmpty()) {
            return "";
        }
        try {
            Document doc = Jsoup.parse(html);
            // 移除不需要的标签
            doc.select("script, style, head, meta, link").remove();
            return doc.body().text();
        } catch (Exception e) {
            return simpleHtmlClean(html);
        }
    }

    /**
     * 简单的 HTML 标签清理（备用方法）
     */
    private static String simpleHtmlClean(String html) {
        return html.replaceAll("<[^>]*>", "").trim();
    }
}