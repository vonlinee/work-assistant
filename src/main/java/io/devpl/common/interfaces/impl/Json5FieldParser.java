package io.devpl.common.interfaces.impl;

import de.marhali.json5.*;
import io.devpl.common.exception.FieldParseException;
import io.devpl.common.interfaces.FieldParser;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * 用于JSON5格式的JSON内容解析
 */
@Slf4j
public class Json5FieldParser implements FieldParser {

    private final Json5 json5 = new Json5(Json5Options.builder().build().remainComment(true));

    /**
     * 仅支持单层JSON对象
     *
     * @param content JSON字符串，支持JSON5格式
     * @return 字段列表
     */
    @Override
    public List<Map<String, Object>> parse(String content) throws FieldParseException {
        try {
            List<Map<String, Object>> res = new ArrayList<>();
            Json5Element root = json5.parse(content);
            if (root.isJson5Object()) {
                Json5Object obj = root.getAsJson5Object();
                for (Map.Entry<String, Json5Element> entry : obj.entrySet()) {
                    Map<String, Object> fieldInfo = new HashMap<>();
                    fieldInfo.put(FIELD_NAME, entry.getKey());

                    // 类型推断
                    Json5Element value = entry.getValue();
                    if (value.isJson5Primitive()) {
                        Json5Primitive primitive = value.getAsJson5Primitive();
                        if (primitive.isBoolean()) {
                            fieldInfo.put(FIELD_TYPE, "Boolean");
                        } else if (primitive.isString()) {
                            fieldInfo.put(FIELD_TYPE, "String");
                        } else if (primitive.isNumber()) {
                            fieldInfo.put(FIELD_TYPE, "Number");
                        }
                    }

                    fieldInfo.put(FIELD_VALUE, String.valueOf(value));

                    Json5Comment comment = obj.getComment(entry.getKey());
                    if (comment != null) {
                        fieldInfo.put(FIELD_DESCRIPTION, comment.getText());
                    }
                    res.add(fieldInfo);
                }
            }
            return res;
        } catch (Exception exception) {
            log.error("[字段解析 JSON5] 解析失败", exception);
        }
        return Collections.emptyList();
    }
}
