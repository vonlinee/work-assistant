package io.devpl.common.interfaces.impl;

import io.devpl.common.interfaces.FieldParser;
import io.devpl.common.exception.FieldParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * F12打开选中table元素，右键复制 -> 复制元素或者复制outerHTML
 */
public class HtmlTableDomFieldParser implements FieldParser {

    /**
     * 按索引顺序依次为：名称，数据类型，描述信息
     */
    private String[] columns;

    public HtmlTableDomFieldParser(String[] columns) {
        this.columns = columns;
    }

    @Override
    public List<Map<String, Object>> parse(String content) throws FieldParseException {
        Document document = Jsoup.parse(content);

        Elements tableElements = document.getElementsByTag("table");

        // 包括标题行
        List<String[]> rows = new ArrayList<>();

        for (int i = 0; i < tableElements.size(); i++) {
            Element tableElement = tableElements.get(i);
            // 标题行
            Elements theadElement = tableElement.getElementsByTag("th");

            List<String> headerRow = new ArrayList<>();
            for (int colNum = 0; colNum < theadElement.size(); colNum++) {
                headerRow.add(theadElement.get(colNum).html());
            }
            rows.add(headerRow.toArray(new String[0]));

            Elements tbodyElement = tableElement.getElementsByTag("tbody");

            Elements trElements = tbodyElement.get(0).getElementsByTag("tr");
            for (int rowNum = 0; rowNum < trElements.size(); rowNum++) {
                Elements tdElements = trElements.get(rowNum).getElementsByTag("td");

                List<String> row = new ArrayList<>();

                for (int colNum = 0; colNum < tdElements.size(); colNum++) {
                    Element tdElement = tdElements.get(colNum);

                    row.add(tdElement.html());
                }

                rows.add(row.toArray(new String[0]));
            }
        }

        String[] titleRow = rows.get(0);

        List<Map<String, Object>> fields = new ArrayList<>();
        for (int i = 1; i < rows.size(); i++) {

            Map<String, Object> map = new HashMap<>();
            map.put(FIELD_NAME, rows.get(i)[0]);
            map.put(FIELD_TYPE, rows.get(i)[1]);
            map.put(FIELD_DESCRIPTION, rows.get(i)[2]);
            fields.add(map);
        }
        return fields;
    }
}
