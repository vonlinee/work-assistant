package io.devpl.common.interfaces.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * 单个字段每行
 * <p>
 * 字段名称	字段属性  字段类型
 * <p>
 * 示例文本：
 * 标题	title
 * 单据号	documentNo
 * 申请人	requestName
 * 申请部门	applierDeptname
 * 申请日期	applyDate
 * 所属公司	companyId
 * 归填类别	recipientType
 * 归填对象	recipientName
 * 发票总额	amount
 * 摘要	note
 * 附件	attachmentLink
 */
public class FieldPerRowFieldParser extends MappingFieldParserAdapter {

    @Override
    public List<String[]> parseRows(String content) {
        String[] rows = content.split("\n");
        List<String[]> ans = new ArrayList<>();
        for (String row : rows) {
            // 从Excel中复制出的文本，每列之间是以\t分割
            String[] props = row.split("\t");
            ans.add(props);
        }
        return ans;
    }
}
