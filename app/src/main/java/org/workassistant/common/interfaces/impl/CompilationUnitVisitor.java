package org.workassistant.common.interfaces.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.expr.Name;

public interface CompilationUnitVisitor<T> {

    T visit(CompilationUnit cu);

    /**
     * 获取包名
     *
     * @param cu 单个类
     * @return 包名
     */
    default String getPackageName(CompilationUnit cu) {
        return cu.getPackageDeclaration().map(PackageDeclaration::getName).map(Name::asString).orElse(null);
    }
}
