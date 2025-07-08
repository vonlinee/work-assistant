package org.workassistant.tools.excel;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.read.listener.ReadListener;
import com.alibaba.excel.util.ConverterUtils;

import java.util.*;

/**
 * 使用easy-excel进行excel读取
 *
 * <a href="https://easyexcel.opensource.alibaba.com/docs/current/">easy excel</a>
 */
public class ExcelUtils {

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
        // LinkedHashMap, key是有序的
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
