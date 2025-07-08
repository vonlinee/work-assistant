package org.workassistant.ui.view;

import io.fxtras.scene.table.TableViewColumn;
import io.fxtras.scene.table.TableViewModel;
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
