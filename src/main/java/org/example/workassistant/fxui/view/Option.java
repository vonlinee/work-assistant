package org.example.workassistant.fxui.view;

import org.example.workassistant.fxui.components.table.TableViewColumn;
import org.example.workassistant.fxui.components.table.TableViewModel;
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
