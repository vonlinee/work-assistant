package io.devpl.common.interfaces.impl;

import io.devpl.common.exception.FieldParseException;
import io.devpl.common.interfaces.FieldParser;
import io.devpl.common.utils.JSONUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
public class JsonFieldParser implements FieldParser {

    /**
     * 仅支持单层(最外层)JSON对象
     *
     * @param content JSON字符串，支持JSON5格式
     * @return 字段列表
     */
    @Override
    public List<Map<String, Object>> parse(String content) throws FieldParseException {
        try {
            List<Map<String, Object>> res = new ArrayList<>();
            Map<String, Object> map = JSONUtils.toMap(content);

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                FieldInfoMap fieldInfoMap = new FieldInfoMap();
                fieldInfoMap.setFieldKey(entry.getKey());
                fieldInfoMap.setFieldName(entry.getKey());

                Object value = entry.getValue();
                if (value instanceof String) {
                    fieldInfoMap.setFieldDataType("String");
                } else if (value instanceof Number) {
                    fieldInfoMap.setFieldDataType("Numeric");
                } else if (value instanceof Boolean) {
                    fieldInfoMap.setFieldDataType("Boolean");
                } else {
                    fieldInfoMap.setFieldDataType("String");
                }

                fieldInfoMap.setFieldValue(String.valueOf(entry.getValue()));
                fieldInfoMap.setFieldDescription("");
                res.add(fieldInfoMap.asMap());
            }

            return res;
        } catch (Exception exception) {
            log.error("[字段解析 JSON] 解析失败", exception);
        }
        return Collections.emptyList();
    }
}
