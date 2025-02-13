package org.example.workassistant.ui.controller.fields;

import cn.hutool.extra.template.TemplateException;
import org.jetbrains.annotations.Nullable;

import java.io.Writer;

/**
 * 为了统一不同的模板引擎的实现，将各个模板引擎的类进行包装
 */
public interface Template {

    /**
     * 模板未找到时返回此值
     */
    Template UNKNOWN = new Template() {
        @Override
        public  String getName() {
            return "Template Not Found";
        }

        @Override
        public boolean exists() {
            return false;
        }

        @Override
        public void render(@Nullable TemplateEngine engine, Object dataModel, Writer writer) {
            try {
                writer.write(getName());
            } catch (Exception ignored) {

            }
        }
    };

    /**
     * 模板名称
     *
     * @return 模板名称，通过此名称来检索已存在的模板
     */
    
    String getName();

    /**
     * 设置模板名称
     */
    default void setName(String templateName) {
    }

    /**
     * @return 获取实际的模板
     * @see StringTemplate
     * @see FileTemplate
     */
    default Template getSource() {
        return this;
    }

    /**
     * @return 模板内容
     */
    default String getContent() {
        return "";
    }

    /**
     * 设置模板的内容
     *
     * @param content 模板内容
     */
    default void setContent(String content) {
    }

    /**
     * 是否是字符串模板
     *
     * @return 如果是字符串模板，则获取模板内容时调用getContent方法
     * @see Template#getContent()
     */
    default boolean isStringTemplate() {
        return false;
    }

    /**
     * 此模板是否真实存在
     *
     * @return 模板是否存在
     */
    default boolean exists() {
        return true;
    }

    /**
     * 渲染模板
     *
     * @param engine    对应模板的模板引擎，用于渲染模板字符串 {@link StringTemplate}
     * @param dataModel 此模板渲染的参数数据模型，Map或者普通javabean，或者TemplateArgumentsMap
     * @param writer    输出位置
     */
    void render(@Nullable TemplateEngine engine, Object dataModel, Writer writer) throws TemplateException;
}
