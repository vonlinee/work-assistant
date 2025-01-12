package io.devpl.fxui.view;

import io.devpl.fxui.components.table.TableViewColumn;
import io.devpl.fxui.components.table.TableViewModel;
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
