package io.devpl.common;

/**
 * 提取模板中的所有变量
 *
 * @param <T> 模板类，每种模板引擎都有一个编译后的AST类
 */
public interface TemplateVariableAnalyzer<T> {

    boolean support(Class<?> templateClass);

    void analyze(T template);
}
