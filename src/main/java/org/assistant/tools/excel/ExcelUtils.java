package org.assistant.tools.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ConverterUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 使用easy-excel进行excel读取
 *
 * <a href="https://easyexcel.opensource.alibaba.com/docs/current/">easy excel</a>
 */
public class ExcelUtils {

	/**
	 * 工具方法，用于将索引转换为 Excel 中的列编号。Excel 的列编号是从 1 开始的，例如 A = 1, B = 2, ..., Z = 26, AA = 27，等等。
	 *
	 * @param index index
	 * @return 编号
	 */
	public static String indexToExcelColumn(int index) {
		StringBuilder columnName = new StringBuilder();
		index--; // 转换为 0 基索引
		while (index >= 0) {
			int remainder = index % 26;
			columnName.insert(0, (char) ('A' + remainder));
			index = index / 26 - 1; // 继续进行下一个字符计算
		}
		return columnName.toString();
	}


	public static List<Map<Integer, Object>> readXlsxByColumnIndex(String path) {
		List<Map<Integer, Object>> list = new ArrayList<>();
		EasyExcel.read(path, new ReadListener<Map<Integer, Object>>() {

			@Override
			public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
				ReadListener.super.invokeHead(headMap, context);
				Map<Integer, Object> heads = new HashMap<>(ConverterUtils.convertToStringMap(headMap, context));
				list.add(heads);
			}

			@Override
			public void invoke(Map<Integer, Object> data, AnalysisContext context) {
				list.add(data);
			}

			@Override
			public void doAfterAllAnalysed(AnalysisContext context) {
				// 读取完成后的处理
			}
		}).sheet().doRead();
		return list;
	}

	public static List<Map<String, Object>> readXlsxByColumnTitle(String path, Integer sheetNo, String sheetName, int headRowNumber) {
		List<Map<String, Object>> list = new ArrayList<>();
		EasyExcel.read(path, new ReadListener<Map<Integer, Object>>() {

			Map<Integer, String> heads;

			@Override
			public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
				ReadListener.super.invokeHead(headMap, context);
				heads = ConverterUtils.convertToStringMap(headMap, context);
			}

			@Override
			public void invoke(Map<Integer, Object> data, AnalysisContext context) {
				LinkedHashMap<String, Object> map = new LinkedHashMap<>();
				for (Map.Entry<Integer, Object> entry : data.entrySet()) {
					map.put(heads.get(entry.getKey()), entry.getValue());
				}
				list.add(map);
			}

			@Override
			public void doAfterAllAnalysed(AnalysisContext context) {
				// 读取完成后的处理
			}
		}).sheet(sheetNo, sheetName).headRowNumber(headRowNumber).doRead();
		return list;
	}

	public static List<Map<String, Object>> readXlsxByColumnTitle(String path, Integer sheetNo, String sheetName) {
		return readXlsxByColumnTitle(path, sheetNo, sheetName, 0);
	}

	public static List<Map<String, Object>> readXlsxByColumnTitle(String path, Integer sheetNo) {
		return readXlsxByColumnTitle(path, sheetNo, null, 0);
	}

	public static List<Map<String, Object>> readXlsxByColumnTitle(String path) {
		return readXlsxByColumnTitle(path, null, null, 0);
	}

	public static void gatherColumnIndex(List<Map<Integer, Object>> dataList) {
		Set<Integer> columnIndexes = dataList.get(0).keySet();
		int min = 0, max = 0;
		for (Integer columnIndex : columnIndexes) {
			min = Math.min(min, columnIndex);
			max = Math.max(max, columnIndex);
		}
	}

	public static void write(String path) {

	}
}
