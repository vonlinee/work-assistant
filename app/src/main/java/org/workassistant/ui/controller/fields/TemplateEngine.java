package org.workassistant.ui.controller.fields;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

/**
 * 模板引擎实现
 *
 * @see Template
 * @see TemplateException 所有异常通过TemplateException进行抛出
 */
public interface TemplateEngine {

    /**
     * 设置属性
     *
     * @param properties 配置参数
     */
    default void setProperties(Properties properties) {
    }

    /**
     * 渲染字符串模板
     *
     * @param template  模板内容，不能为null或者空
     * @param arguments 模板参数
     * @return 渲染结果
     */
    default String evaluate(String template, Object arguments) throws TemplateException {
        try (Writer writer = new StringWriter()) {
            evaluate(template, arguments, writer);
            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 渲染字符串模板
     *
     * @param template  模板内容，不能为null或者空
     * @param arguments 模板参数
     * @param writer    渲染结果
     */
    void evaluate(String template, Object arguments, Writer writer) throws TemplateException;

    /**
     * 渲染并输出到指定位置
     *
     * @param template     模板
     * @param arguments    模板参数
     * @param outputStream 输出位置
     * @throws TemplateException 渲染失败
     */
    void render(Template template, Object arguments, OutputStream outputStream) throws TemplateException;

    /**
     * 渲染并输出到指定位置
     * 实际上调用的是Template#render方法
     *
     * @param template  模板
     * @param arguments 模板参数
     * @param writer    输出位置
     * @throws TemplateException 渲染失败
     * @see Template#render(TemplateEngine, Object, Writer)
     */
    default void render( Template template,  Object arguments,  Writer writer) throws TemplateException {
        template.render(this, arguments, writer);
    }

    /**
     * 同render方法
     *
     * @param name      模板名称，通过模板名称加载模板
     * @param arguments 模板参数
     * @param fos       输出位置
     * @throws TemplateException 渲染出错
     */
    default void render(String name, Map<String, Object> arguments, OutputStream fos) throws TemplateException {
        Template template = getTemplate(name, false);
        if (template.exists()) {
            template.render(this, arguments, new OutputStreamWriter(fos));
        } else {
            try {
                fos.write(template.getName().getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw TemplateException.wrap(e);
            }
        }
    }

    default void render(String name, TemplateArguments arguments, OutputStream fos) throws TemplateException {
        render(getTemplate(name, false), arguments, fos);
    }

    /**
     * 渲染模板，输出为字符串
     *
     * @param template  模板
     * @param arguments 模板参数
     * @return 渲染结果
     */
    default String render(Template template, TemplateArguments arguments) throws TemplateException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            render(template, arguments, os);
            return os.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new TemplateException(e);
        }
    }

    /**
     * 转换数据模型
     *
     * @param dataModel 数据模型
     * @return 转换后的数据模型
     */
    default Object transfer(Object dataModel) {
        if (dataModel instanceof Map<?, ?> map) {
            return map;
        }
        if (dataModel instanceof TemplateArgumentsMap tam) {
            return tam.asMap();
        }
        return dataModel;
    }

    /**
     * TODO 限制此方法不能传入模板字符串
     *
     * @param template  模板名称
     * @param arguments 模板渲染参数
     * @return 渲染结果
     * @throws TemplateException 模板异常
     */
    default String render(String template, TemplateArguments arguments) throws TemplateException {
        Template ts = getTemplate(template, false);
        if (!ts.exists()) {
            return ts.getName();
        }
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            render(ts, arguments, os);
            return os.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new TemplateException(e);
        }
    }

    /**
     * 注册自定义指令
     *
     * @param directive 指令实现
     * @return 是否成功
     */
    default <D extends TemplateDirective> boolean registerDirective(D directive) {
        return false;
    }

    /**
     * 获取模板文件后缀名
     *
     * @return 模板文件后缀名，不为空
     */
    
    String getTemplateFileExtension();

    /**
     * 获取模板
     *
     * @param nameOrTemplate 模板名称或者字符串模板
     * @param st             是否是字符串模板, 为true，则将nameOrTemplate参数视为模板文本，为false则将nameOrTemplate参数视为模板名称，不用程序自己来判断是否是模板名称还是模板字符串
     * @return 模板实例，如果不存在，返回 {@link Template#UNKNOWN}
     * @see Template
     */
    
    default Template getTemplate(String nameOrTemplate, boolean st) throws TemplateException {
        return Template.UNKNOWN;
    }
}
