package io.devpl.fxui.plugins;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.Interface;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;

/**
 * MyBatis-Plus支持插件
 */
public class MyBatisPlusPlugin extends PluginAdapter {

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addAnnotation("@TableName(value = \"" + introspectedTable.getTableConfiguration()
                .getTableName() + "\")");
        return true;
    }

    @Override
    public boolean clientGenerated(Interface interfaceData, IntrospectedTable introspectedTable) {
        interfaceData.addAnnotation("@Mapper");
        return true;
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        final String colName = introspectedColumn.getActualColumnName();
        if (isPrimaryKey(introspectedColumn)) {
            topLevelClass.addImportedType("com.baomidou.mybatisplus.annotation.TableId");
            field.addAnnotation("@TableId(value = \"" + colName + "\", type = IdType.AUTO)");
        } else {
            topLevelClass.addImportedType("com.baomidou.mybatisplus.annotation.TableField");
            field.addAnnotation("@TableField(value = \"" + colName + "\", insertStrategy = FieldStrategy.IGNORED)");
        }
        return true;
    }

    /**
     * 该列是否为主键
     * @param column 列
     * @return 是否为主键
     */
    private boolean isPrimaryKey(IntrospectedColumn column) {
        return column.getIntrospectedTable()
                .getPrimaryKeyColumns()
                .contains(column);
    }
}
