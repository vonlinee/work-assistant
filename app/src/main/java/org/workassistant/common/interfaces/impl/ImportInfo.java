package org.workassistant.common.interfaces.impl;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.SimpleName;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * 存放导入信息
 */
@Getter
@Setter
public class ImportInfo {

    private String packageName;

    /**
     * Key:导入语句中的类型名称，比如：java.util.List，则为List
     * Value:类型全类名，如java.util.List
     */
    private Map<String, String> importedIdentifierMap = new HashMap<>();

    public static ImportInfo extract(NodeList<ImportDeclaration> importDeclarations) {
        final ImportInfo importInfo = new ImportInfo();
        for (ImportDeclaration importDeclaration : importDeclarations) {
            Name name = importDeclaration.getName();
            importInfo.getImportedIdentifierMap().put(name.getIdentifier(), name.asString());
        }
        return importInfo;
    }

    private Map<String, String> getImportedIdentifierMap() {
        return importedIdentifierMap;
    }

    /**
     * 是否导入了该类型
     *
     * @param typeName SimpleName
     * @return 是否导入了该类型
     */
    public boolean containsType(SimpleName typeName) {
        return importedIdentifierMap.containsKey(typeName.getIdentifier());
    }

    /**
     * 为null时，如果该文件语法正确，那么则在同一包下，不用显示import
     *
     * @param typeName 类型名称
     * @return 类型名称
     */
    public String get(SimpleName typeName) {
        String fullTypeName = importedIdentifierMap.get(typeName.getIdentifier());
        if (fullTypeName == null) {
            fullTypeName = packageName + "." + typeName.getIdentifier();
        }
        return fullTypeName;
    }
}
