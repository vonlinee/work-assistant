package org.example.workassistant.ui.view;

import org.example.workassistant.ui.components.table.TableViewColumn;
import org.example.workassistant.ui.components.table.TableViewModel;
import lombok.Getter;
import lombok.Setter;

/**
 * 值生成器配置项
 */
@Setter
@Getter
@TableViewModel
public class Option {

    @TableViewColumn(title = "名称")
    private String name;

    @TableViewColumn(title = "值")
    private Object value;
}
