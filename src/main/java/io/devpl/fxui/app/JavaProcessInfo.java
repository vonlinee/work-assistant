package io.devpl.fxui.app;

import io.devpl.fxui.components.table.TableViewColumn;
import io.devpl.fxui.components.table.TableViewModel;
import lombok.Data;

@Data
@TableViewModel
public class JavaProcessInfo {

    @TableViewColumn(field = "pid", title = "PID")
    private String pid;

    @TableViewColumn(field = "mainClassName", title = "主类名")
    private String mainClassName;
}
