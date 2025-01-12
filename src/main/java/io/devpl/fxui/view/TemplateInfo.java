package io.devpl.fxui.view;

import io.devpl.fxui.components.table.TableViewColumn;
import io.devpl.fxui.components.table.TableViewModel;
import lombok.Getter;
import lombok.Setter;

/**
 * 模板信息
 */
@Getter
@Setter
@TableViewModel
public class TemplateInfo {

    /**
     * 模板名称：唯一
     */
    @TableViewColumn(title = "模板名称")
    private String templateName;

    /**
     * 模板内容
     */
    private String content;

    /**
     * 备注信息
     */
    @TableViewColumn(title = "备注信息")
    private String remark;
}

