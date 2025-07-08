package org.workassistant.ui.view;

import io.fxtras.scene.table.TableViewColumn;
import io.fxtras.scene.table.TableViewModel;
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
