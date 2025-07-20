package org.assistant.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigurableHtmlTextExtractor {

    private List<String> excludeTags;
    private List<String> includeTags;
    private boolean preserveLineBreaks;

    public ConfigurableHtmlTextExtractor() {
        // 默认排除的标签
        this.excludeTags = Arrays.asList("script", "style", "head", "meta", "link", "noscript");
        // 默认包含的标签
        this.includeTags = Arrays.asList("p", "div", "h1", "h2", "h3", "h4", "h5", "h6", "span", "article", "section");
        this.preserveLineBreaks = true;
    }

    public ConfigurableHtmlTextExtractor setExcludeTags(List<String> excludeTags) {
        this.excludeTags = excludeTags;
        return this;
    }

    public ConfigurableHtmlTextExtractor setIncludeTags(List<String> includeTags) {
        this.includeTags = includeTags;
        return this;
    }

    public ConfigurableHtmlTextExtractor setPreserveLineBreaks(boolean preserveLineBreaks) {
        this.preserveLineBreaks = preserveLineBreaks;
        return this;
    }

    /**
     * 提取文本内容
     */
    public String extractText(String html) {
        if (html == null || html.trim().isEmpty()) {
            return "";
        }

        try {
            Document doc = Jsoup.parse(html);

            // 移除排除的标签
            if (excludeTags != null && !excludeTags.isEmpty()) {
                String excludeSelector = String.join(", ", excludeTags);
                doc.select(excludeSelector).remove();
            }

            // 如果指定了包含的标签，则只提取这些标签的内容
            if (includeTags != null && !includeTags.isEmpty()) {
                String includeSelector = String.join(", ", includeTags);
                Elements elements = doc.select(includeSelector);

                return elements.stream()
                    .map(Element::text)
                    .map(String::trim)
                    .filter(text -> !text.isEmpty())
                    .collect(Collectors.joining(preserveLineBreaks ? "\n" : " "));
            } else {
                // 提取所有文本
                return doc.text();
            }

        } catch (Exception e) {
            return simpleHtmlClean(html);
        }
    }

    /**
     * 提取文本并保留基本的格式
     */
    public String extractFormattedText(String html) {
        if (html == null || html.trim().isEmpty()) {
            return "";
        }

        try {
            Document doc = Jsoup.parse(html);

            // 移除不需要的标签
            doc.select("script, style, head, meta, link").remove();

            // 处理换行
            doc.select("br").append("\\n");
            doc.select("p, div").append("\\n\\n");

            String text = doc.text();

            // 还原换行符
            text = text.replace("\\n", "\n");

            // 清理多余的空行
            text = text.replaceAll("\n\\s+\n", "\n\n");

            return text.trim();

        } catch (Exception e) {
            return simpleHtmlClean(html);
        }
    }

    private String simpleHtmlClean(String html) {
        return html.replaceAll("<[^>]*>", "").trim();
    }
}