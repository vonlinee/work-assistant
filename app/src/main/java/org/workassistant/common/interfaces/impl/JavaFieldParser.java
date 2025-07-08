package org.workassistant.common.interfaces.impl;

import org.workassistant.common.interfaces.FieldParser;
import org.workassistant.common.exception.FieldParseException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 支持单文件多个类型定义
 */
@Slf4j
public class JavaFieldParser implements FieldParser {

    /**
     * 类型解析加上包名
     *
     * @param content 文本内容，Java类
     * @return 字段列表
     */
    @Override
    public List<Map<String, Object>> parse(String content) throws FieldParseException {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            List<MetaField> metaFields = JavaASTUtils.parseFields(content);
            for (MetaField metaField : metaFields) {
                FieldInfoMap fieldInfo = new FieldInfoMap();
                fieldInfo.setFieldKey(metaField.getIdentifier());
                fieldInfo.setFieldName(metaField.getName());
                fieldInfo.setFieldDescription(metaField.getDescription());
                fieldInfo.setFieldDataType(metaField.getDataType());
                result.add(fieldInfo.asMap());
            }
        } catch (IOException e) {
            log.error("[字段解析 JAVA] 解析失败", e);
            throw new FieldParseException(e);
        }
        return result;
    }
}
