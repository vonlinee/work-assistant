package org.example.workassistant.ui.view;

import org.example.workassistant.ui.components.table.TableViewColumn;
import org.example.workassistant.ui.components.table.TableViewModel;
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
