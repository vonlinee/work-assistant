package io.devpl.common.interfaces.impl;

import io.devpl.common.exception.FieldParseException;

import java.util.ArrayList;
import java.util.List;

public class HtmlTableContentFieldParser extends MappingFieldParserAdapter {

    public HtmlTableContentFieldParser(String[] columnMapping) {
        setColumnMapping(columnMapping);
    }

    /**
     * @param content 直接复制浏览器页面上的表格内容，复制的结果是纯文本（暂时未发现有什么特定的规则）
     * @return 字段信息
     */
    @Override
    public List<String[]> parseRows(String content) throws FieldParseException {
        String[] lines = content.split("\n");

        // 第一行为标题
        List<String[]> rows = new ArrayList<>();

        String[] titleColumns = getTitleRowsOfTableContent(lines[0]);
        rows.add(titleColumns);

        List<String> mergedLines = new ArrayList<>(lines.length);

        // 合并行
        int curLineNum = 1;
        String curLine, nextLine;
        while (true) {
            curLine = lines[curLineNum];
            if (curLineNum + 1 >= lines.length) {
                mergedLines.add(lines[curLineNum]);
                break;
            }
            int index = curLineNum + 1;
            StringBuilder line = new StringBuilder(curLine);
            while (index < lines.length) {
                nextLine = lines[index];
                String[] columns = nextLine.split("\t");
                if (columns.length < titleColumns.length) {
                    line.append(nextLine);
                } else {
                    mergedLines.add(line.toString());
                    curLineNum = index;
                    break;
                }
                index++;
            }
        }

        for (String line : mergedLines) {
            String[] columnsOfRow = getTitleRowsOfTableContent(line);
            rows.add(columnsOfRow);
        }
        return rows;
    }

}
