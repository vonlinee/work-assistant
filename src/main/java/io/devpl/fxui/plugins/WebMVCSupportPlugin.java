package io.devpl.fxui.plugins;

import io.devpl.fxui.common.StringKey;
import io.devpl.fxui.utils.Helper;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.FullyQualifiedJavaType;
import org.mybatis.generator.api.dom.java.TopLevelClass;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.TableConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 创建MVC三层的代码, 包含
 * 1.控制器
 * 2.Service
 * 3.和DAO层Mapper
 * @since created on 2022年8月5日
 */
public class WebMVCSupportPlugin extends PluginAdapter {

    private String parentPackage;

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);
        this.parentPackage = context.getProperty(StringKey.PARENT_PACKAGE);
    }

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
    }

    /**
     * 添加其他文件
     * @return 生成Java文件
     */
    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        List<GeneratedJavaFile> mvcFiles = new ArrayList<>(prepareServiceFiles());
        mvcFiles.addAll(prepareControllerFiles());
        mvcFiles.addAll(prepareServiceFiles());
        return mvcFiles;
    }

    private List<GeneratedJavaFile> prepareControllerFiles() {
        List<GeneratedJavaFile> controllerFiles = new ArrayList<>();
        final List<IntrospectedTable> introspectedTables = context.getIntrospectedTables();
        for (IntrospectedTable introspectedTable : introspectedTables) {

            final String tableName = introspectedTable.getFullyQualifiedTableNameAtRuntime();
            final String className = Helper.underlineToCamel(tableName);
            String name = parentPackage + ".controller." + className + "Controller";
            final FullyQualifiedJavaType type = new FullyQualifiedJavaType(name);

            final TopLevelClass controllerClass = new TopLevelClass(type);

            new GeneratedJavaFile(controllerClass, "", null);
        }
        return controllerFiles;
    }

    /**
     * 生成Service文件
     * @return Service文件
     */
    private List<GeneratedJavaFile> prepareServiceFiles() {
        List<GeneratedJavaFile> serviceFiles = new ArrayList<>();
        final List<IntrospectedTable> introspectedTables = context.getIntrospectedTables();
        for (IntrospectedTable introspectedTable : introspectedTables) {
            TableConfiguration tableConfiguration = introspectedTable.getTableConfiguration();

            // final Interface serviceInterface = new Interface();
        }
        return serviceFiles;
    }
}
