package org.assistant.tools.mybatis;

import org.apache.ibatis.mapping.MappedStatement;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jdesktop.swingx.treetable.TreeTableNode;

import javax.swing.tree.DefaultMutableTreeNode;

public class MyBatisNode extends DefaultMutableTreeTableNode {

    // For TreeTable Structure
    private String idOrNamespace;
    private String type;
    private String sourceFile;

    private MappedStatement mappedStatement; // Only present on leaf nodes

    public MyBatisNode(String idOrNamespace, String type, String sourceFile) {
        super(idOrNamespace);
        this.idOrNamespace = idOrNamespace;
        this.type = type;
        this.sourceFile = sourceFile;
    }

    public boolean hasChildren() {
        return getChildCount() > 0;
    }

    public void addChild(MyBatisNode child) {
        this.add(child);
    }

    public String getIdOrNamespace() {
        return idOrNamespace;
    }

    public void setIdOrNamespace(String idOrNamespace) {
        this.idOrNamespace = idOrNamespace;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(String sourceFile) {
        this.sourceFile = sourceFile;
    }

    public MappedStatement getMappedStatement() {
        return mappedStatement;
    }

    public void setMappedStatement(MappedStatement mappedStatement) {
        this.mappedStatement = mappedStatement;
    }
}
