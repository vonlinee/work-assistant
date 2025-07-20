package org.assistant.util;

import java.util.List;

public class HtmlTextExtractorExample {
    public static void main(String[] args) {
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>示例页面</title>
                <style>body { color: red; }</style>
                <script>console.log('hello');</script>
            </head>
            <body>
                <h1>这是一个标题</h1>
                <p>这是一个段落文本。</p>
                <div>这是div中的文本</div>
                <script>这是脚本内容，应该被过滤</script>
                <p>另一个段落</p>
            </body>
            </html>
            """;

        // 使用基础方法
        System.out.println("=== 基础方法 ===");
        System.out.println(HtmlTextExtractor.extractAllText(html));
        System.out.println();

        System.out.println("=== 主体文本 ===");
        System.out.println(HtmlTextExtractor.extractBodyText(html));
        System.out.println();

        // 使用高级方法
        System.out.println("=== 分段提取 ===");
        List<String> paragraphs = AdvancedHtmlTextExtractor.extractTextByParagraphs(html);
        paragraphs.forEach(System.out::println);
        System.out.println();

        // 使用可配置提取器
        System.out.println("=== 可配置提取器 ===");
        ConfigurableHtmlTextExtractor extractor = new ConfigurableHtmlTextExtractor();
        String text = extractor.extractText(html);
        System.out.println(text);
        System.out.println();

        System.out.println("=== 格式化文本 ===");
        String formattedText = extractor.extractFormattedText(html);
        System.out.println(formattedText);
    }
}