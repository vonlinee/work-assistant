package org.example.workassistant.fxui.view;

import org.example.workassistant.fxui.components.table.TableViewColumn;
import org.example.workassistant.fxui.components.table.TableViewModel;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableViewModel
public class TypeMappingItem {

    @TableViewColumn(title = "JSON")
    private String jsonType;

    @TableViewColumn(title = "JDBC")
    private String jdbcType;

    @TableViewColumn(title = "JAVA")
    private String javaType;
}
