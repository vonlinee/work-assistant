package io.devpl.fxui.model;

import java.util.ArrayList;
import java.util.List;

public class FieldNode {

    /**
     * 名称
     */
    private String name;

    /**
     * 描述信息
     */
    private String description;

    /**
     * 数据类型
     */
    private String dataType;

    private List<FieldNode> children;

    public FieldNode() {
        this("Unknown");
    }

    public FieldNode(String name) {
        this.name = name;
        this.description = name;
    }

    public FieldNode(String name, String description, String dataType) {
        this.name = name;
        this.description = description;
        this.dataType = dataType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public List<FieldNode> getChildren() {
        return children;
    }

    public void setChildren(List<FieldNode> children) {
        this.children = children;
    }

    @Override
    public String toString() {
        return "FieldNode{" + "name='" + name + '\'' + ", description='" + description + '\'' + ", dataType='" + dataType + '\'' + '}';
    }

    public void addChild(FieldNode child) {
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
    }

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }
}
