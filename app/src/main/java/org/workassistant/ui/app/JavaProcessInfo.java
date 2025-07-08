package org.workassistant.ui.app;

import io.fxtras.scene.table.TableViewColumn;
import io.fxtras.scene.table.TableViewModel;
import lombok.Data;

@Data
@TableViewModel
public class JavaProcessInfo {

    @TableViewColumn(field = "pid", title = "PID")
    private String pid;

    @TableViewColumn(field = "mainClassName", title = "主类名")
    private String mainClassName;
}
