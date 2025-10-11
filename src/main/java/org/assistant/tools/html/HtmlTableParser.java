package org.assistant.tools.html;

import org.assistant.tools.excel.ExcelUtils;
import org.assistant.tools.excel.TableData;
import org.assistant.tools.excel.TableHeader;
import org.assistant.util.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTML表格解析工具类
 */
public class HtmlTableParser {

	public static void main(String[] args) throws IOException {
		String s = FileUtils.readDesktopFileToString("1.txt");
		TableData tableData = HtmlTableParser.parseTable(s);
		File file1 = FileUtils.getDesktopFile("1.xlsx");
		ExcelUtils.writeXlsx(tableData, file1);
	}

	/**
	 * 解析HTML表格数据
	 *
	 * @param html HTML字符串，根元素为 {@code <table></table>}
	 * @return 表格数据, 包含表头，表数据行
	 */
	public static TableData parseTable(String html) {
		TableData tableData = new TableData();
		try {
			Document doc = Jsoup.parse(html);
			Element table = doc.select("table").first();
			if (table == null) {
				throw new IllegalArgumentException("未找到表格元素");
			}

			// 提取表头 <thead>
			Elements tableHeads = table.select("thead");
			if (!tableHeads.isEmpty()) {
				Element thead = tableHeads.get(0);
				Elements th = thead.select("th");
				if (!th.isEmpty()) {
					List<TableHeader> headers = new ArrayList<>();
					for (int i = 0; i < th.size(); i++) {
						TableHeader header = new TableHeader();
						header.setColumnNum(i);
						header.setTitle(th.get(i).text());
						headers.add(header);
					}
					tableData.setHeaders(headers);
				}
			}

			// 处理表格数据行 <tbody>
			List<Map<Integer, Object>> dataList = new ArrayList<>();

			Elements tbodyElements = table.select("tbody");
			if (!tbodyElements.isEmpty()) {
				Element tbodyElement = tbodyElements.get(0);
				// 获取所有行
				Elements trs = tbodyElement.select("tr");
				for (Element row : trs) {
					// 获取单元格数据
					Map<Integer, Object> rowData = new HashMap<>();
					Elements cells = row.select("td");
					for (int i = 0; i < cells.size(); i++) {
						String trim = cells.get(i).text().trim();
						rowData.put(i, trim);
					}
					dataList.add(rowData);
				}
			}
			tableData.setRows(dataList);
		} catch (Exception e) {
			throw new RuntimeException("解析HTML表格失败: " + e.getMessage(), e);
		}
		return tableData;
	}

	/**
	 * 解析动态表格（支持动态列）
	 *
	 * @param html HTML字符串
	 * @return 数据列表，每个元素是一个字符串数组
	 */
	public static List<String[]> parseDynamicTable(String html) {
		List<String[]> dataList = new ArrayList<>();

		try {
			Document doc = Jsoup.parse(html);
			Element table = doc.select("table").first();
			if (table == null) {
				throw new IllegalArgumentException("未找到表格元素");
			}
			// 处理所有行
			Elements rows = table.select("tr");
			for (Element row : rows) {
				Elements cells = row.select("td, th");
				String[] rowData = new String[cells.size()];

				for (int i = 0; i < cells.size(); i++) {
					rowData[i] = cells.get(i).text().trim();
				}

				dataList.add(rowData);
			}

		} catch (Exception e) {
			throw new RuntimeException("解析HTML表格失败: " + e.getMessage(), e);
		}

		return dataList;
	}

	/**
	 * 获取表头信息
	 *
	 * @param html HTML字符串
	 * @return 表头数组
	 */
	public static String[] getTableHeaders(String html) {
		try {
			Document doc = Jsoup.parse(html);
			Element table = doc.select("table").first();

			if (table == null) {
				return new String[0];
			}

			// 获取第一行作为表头
			Element firstRow = table.select("tr").first();
			if (firstRow == null) {
				return new String[0];
			}

			Elements headerCells = firstRow.select("th, td");
			String[] headers = new String[headerCells.size()];

			for (int i = 0; i < headerCells.size(); i++) {
				headers[i] = headerCells.get(i).text().trim();
			}

			return headers;

		} catch (Exception e) {
			throw new RuntimeException("获取表头失败: " + e.getMessage(), e);
		}
	}
}