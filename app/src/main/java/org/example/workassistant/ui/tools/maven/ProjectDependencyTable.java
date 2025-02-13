package org.example.workassistant.ui.tools.maven;

import io.fxtras.scene.control.enhanced.SimpleCrudTableView;
import io.fxtras.scene.control.table.LambdaCellValueFactory;
import io.fxtras.scene.control.table.SimpleBeanTableColumn;
import javafx.scene.control.TableColumn;

import java.util.Arrays;
import java.util.List;

/**
 * 项目依赖信息表
 */
public class ProjectDependencyTable extends SimpleCrudTableView<LocalJarDependency> {

    public ProjectDependencyTable() {

    }

    @Override
    @SuppressWarnings("unchecked")
    protected <C extends TableColumn<? extends LocalJarDependency, ?>> List<C> createColumns() {
        SimpleBeanTableColumn<LocalJarDependency, String> nameColumn = new SimpleBeanTableColumn<>("Name");
        SimpleBeanTableColumn<LocalJarDependency, Boolean> modularColumn = new SimpleBeanTableColumn<>("模块化");
        nameColumn.setCellValueFactory(new LambdaCellValueFactory<>(LocalJarDependency::toString));
        modularColumn.setCellValueFactory(new LambdaCellValueFactory<>(LocalJarDependency::getModular));
        return (List<C>) Arrays.asList(nameColumn, modularColumn);
    }
}
