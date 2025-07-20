package org.assistant.util;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class AdvancedHtmlTextExtractor {

	/**
	 * 分段提取文本，保留段落结构
	 *
	 * @param html HTML 字符串
	 * @return 分段文本列表
	 */
	public static List<String> extractTextByParagraphs(String html) {
		List<String> paragraphs = new ArrayList<>();

		if (html == null || html.trim().isEmpty()) {
			return paragraphs;
		}

		try {
			Document doc = Jsoup.parse(html);

			// 移除不需要的标签
			doc.select("script, style, head, meta, link, nav, footer, header").remove();

			// 按段落标签提取
			Elements paragraphElements = doc.select("p, div, section, article, h1, h2, h3, h4, h5, h6");

			for (Element element : paragraphElements) {
				String text = element.text().trim();
				if (!text.isEmpty()) {
					paragraphs.add(text);
				}
			}

			// 如果没有找到段落标签，则提取所有文本
			if (paragraphs.isEmpty()) {
				String fullText = doc.body().text().trim();
				if (!fullText.isEmpty()) {
					paragraphs.add(fullText);
				}
			}

		} catch (Exception e) {
			// 备用方案
			String cleanText = html.replaceAll("<[^>]*>", "").trim();
			if (!cleanText.isEmpty()) {
				paragraphs.add(cleanText);
			}
		}

		return paragraphs;
	}

	/**
	 * 提取标题和主要内容
	 *
	 * @param html HTML 字符串
	 * @return 包含标题和内容的文本
	 */
	public static String extractTitleAndContent(String html) {
		if (html == null || html.trim().isEmpty()) {
			return "";
		}

		try {
			Document doc = Jsoup.parse(html);
			StringBuilder result = new StringBuilder();

			// 提取标题
			String title = doc.title();
			if (!title.isEmpty()) {
				result.append("标题: ").append(title).append("\n\n");
			}

			// 提取 h1-h6 标题
			Elements headings = doc.select("h1, h2, h3, h4, h5, h6");
			for (Element heading : headings) {
				String headingText = heading.text().trim();
				if (!headingText.isEmpty()) {
					result.append(headingText).append("\n");
				}
			}

			// 提取段落文本
			Elements paragraphs = doc.select("p");
			for (Element paragraph : paragraphs) {
				String paragraphText = paragraph.text().trim();
				if (!paragraphText.isEmpty()) {
					result.append(paragraphText).append("\n");
				}
			}

			return result.toString().trim();

		} catch (Exception e) {
			return simpleHtmlClean(html);
		}
	}

	private static String simpleHtmlClean(String html) {
		return html.replaceAll("<[^>]*>", "").trim();
	}
}