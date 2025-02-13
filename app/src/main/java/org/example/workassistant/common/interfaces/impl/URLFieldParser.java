package org.example.workassistant.common.interfaces.impl;

import org.example.workassistant.common.exception.FieldParseException;
import org.example.workassistant.common.interfaces.FieldParser;
import org.example.workassistant.common.utils.Utils;
import org.springframework.util.MultiValueMap;
import org.springframework.util.MultiValueMapAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 从URL解析字段
 */
public class URLFieldParser implements FieldParser {
    @Override
    public List<Map<String, Object>> parse(String content) throws FieldParseException {
        content = Utils.removeInvisibleCharacters(content);

        // 兼容不以http开头的输入，例如/aa/bb/cc?arg1=1&arg2=10...
        if (content.startsWith("/")) {
            content = "http://localhost:8088" + content;
        } else if (!content.startsWith("http")) {
            content = "http://localhost:8088/" + content;
        }

        List<Map<String, Object>> result = new ArrayList<>();
        try {


            MultiValueMap<String, String> queryParams = new MultiValueMapAdapter<>(new HashMap<>());

            for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
                FieldInfoMap fieldInfo = new FieldInfoMap();
                fieldInfo.setFieldKey(entry.getKey());
                fieldInfo.setFieldName(entry.getKey());
                fieldInfo.setFieldDescription(entry.getKey());
                if (entry.getValue() == null || entry.getValue().isEmpty() || entry.getValue().size() == 1) {
                    fieldInfo.setFieldDataType("String");
                    fieldInfo.setFieldValue(entry.getValue().get(0));
                } else {
                    fieldInfo.setFieldDataType("Collection");
                    fieldInfo.setFieldValue(String.join(",", entry.getValue()));
                }
                result.add(fieldInfo.asMap());
            }

        } catch (Exception exception) {
            throw new FieldParseException("不是有效的URL路径");
        }
        return result;
    }
}
