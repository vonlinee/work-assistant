package io.devpl.fxui.tools.navigation.tree;

/**
 * TreeView的泛型
 */
public interface TreeItemObject {

    /**
     * 对象类型常量，由使用方定义
     *
     * @return 对象类型常量
     */
    int getCellType();

    /**
     * 设置展示的值
     *
     * @param value 展示的值
     */
    void setDisplayValue(String value);

    /**
     * 获取展示的值
     *
     * @return 对象展示的值
     */
    String getDisplayValue();
}
