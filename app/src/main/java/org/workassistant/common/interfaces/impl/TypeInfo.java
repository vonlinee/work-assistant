package org.workassistant.common.interfaces.impl;

import com.github.javaparser.ast.body.TypeDeclaration;
import lombok.Getter;
import lombok.Setter;

/**
 * 类型信息
 */
@Getter
@Setter
public class TypeInfo {

    // 简单类名
    private String simpleName;

    // 所在文件的路径
    private String path;

    // 全类名
    private String fullName;

    // 是否是顶级类型
    private int level;

    public static TypeInfo of(TypeDeclaration<?> typeDeclaration) {
        TypeInfo typeInfo = new TypeInfo();
        typeInfo.setSimpleName(typeDeclaration.getName().getIdentifier());
        typeInfo.setFullName(typeDeclaration.getFullyQualifiedName().orElse(""));
        return typeInfo;
    }
}
