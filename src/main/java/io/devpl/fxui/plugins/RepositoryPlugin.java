package io.devpl.fxui.plugins;

import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.Interface;

import java.util.List;

public class RepositoryPlugin extends PluginAdapter {

    private FullyQualifiedJavaType annotationRepository;
    private String annotation = "@Mapper";

    public RepositoryPlugin() {
        annotationRepository = new FullyQualifiedJavaType("org.springframework.stereotype.Repository");
    }

    @Override
    public boolean validate(List<String> list) {
        return true;
    }

    // TODO FIX 确定方法是否存在
    // @Override
    // public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
    //     interfaze.addImportedType(annotationRepository);
    //     interfaze.addAnnotation(annotation);
    //     return true;
    // }

    @Override
    public boolean clientGenerated(Interface interfaze, IntrospectedTable introspectedTable) {
        interfaze.addImportedType(annotationRepository);
        interfaze.addAnnotation(annotation);
        return true;
    }
}
