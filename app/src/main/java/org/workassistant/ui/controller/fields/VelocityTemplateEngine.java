package org.workassistant.ui.controller.fields;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.RuntimeInstance;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;

/**
 * Velocity 模板引擎实现文件输出
 * <a href="https://velocity.apache.org/engine/1.7/user-guide.html">Velocity 1.7</a>
 * <a href="https://velocity.apache.org/engine/devel/getting-started.html">...</a>
 * <a href="https://velocity.apache.org/engine/devel/user-guide.html">...</a>
 * <p>
 * org/apache/velocity/runtime/defaults/velocity.properties
 * <p>
 * <a href="https://blog.51cto.com/zhangzhixi/3241138">...</a>
 */
public class VelocityTemplateEngine implements TemplateEngine {

    /**
     * 字符串模板日志tag
     */
    static final String ST_LOG_TAG = "StringTemplate";

    private final VelocityEngine engine;
    private final StringResourceRepository stringTemplates = new VelocityStringResourceRepository();

    public VelocityTemplateEngine() {
        engine = createEngine();
    }

    private VelocityEngine createEngine() {
        Properties properties = new Properties();
        // 当前类的同级目录下的 velocity.properties 文件
        try (InputStream is = this.getClass().getResourceAsStream("velocity.properties")) {
            properties.load(is);
        } catch (IOException e) {
            throw new RuntimeException("Can't load custom velocity config file from classpath", e);
        }
        VelocityEngine engine = new VelocityEngine(properties);
        /**
         * 按StringResourceLoader的javadoc文档配置无效，手动添加添加，这里手动添加
         * 通过下面方式获取:
         * VelocityStringResourceRepository repository = (VelocityStringResourceRepository) engine.getApplicationAttribute("devpl");
         */
        engine.setApplicationAttribute("devpl", stringTemplates);
        // 模板资源管理器
        engine.setProperty(RuntimeConstants.RESOURCE_MANAGER_CLASS, VelocityResourceManager.class.getName());

        registerDirectives();
        return engine;
    }

    /**
     * 注册自定义指令
     */
    private void registerDirectives() {
        registerDirective(new CamelCaseDirective());
    }

    /**
     * @param template  模板内容，不能为null或者空
     * @param arguments 模板参数
     * @param writer    渲染结果
     * @see RuntimeInstance
     */
    @Override
    @SuppressWarnings("unchecked")
    public void evaluate(String template, Object arguments, Writer writer) {
        try {
            VelocityContext context = new VelocityContext((Map<String, Object>) transfer(arguments));
            RuntimeSingleton.getRuntimeServices().evaluate(context, writer, ST_LOG_TAG, new StringReader(template));
        } catch (Exception exception) {
            throw TemplateException.wrap(exception);
        }
    }

    /**
     * 渲染模板
     * 通过文件模板进行查找，如果找不到则找字符串模板
     * 使用Velocity.evaluate无法使用自定义指令功能
     *
     * @param template     模板类型
     * @param arguments    模板参数
     * @param outputStream 输出位置，不会关闭流
     * @see org.apache.velocity.Template#merge(Context, Writer)  可使用自定义指令功能
     * @see VelocityStringResourceRepository
     */
    @Override
    public void render(Template template, Object arguments, OutputStream outputStream) throws TemplateException {
        // 渲染字符串模板
        if (template.isStringTemplate() || template instanceof StringTemplate) {
            String result = evaluate(template.getContent(), arguments);
            try {
                outputStream.write(result.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new TemplateException(e);
            }
            return;
        }
        template = template.getSource();
        if (template instanceof VelocityTemplate) {
            try (Writer writer = new OutputStreamWriter(outputStream)) {
                render(template, arguments, writer);
            } catch (IOException e) {
                throw new TemplateException(e);
            }
        }
    }

    @Override
    public  String getTemplateFileExtension() {
        return ".vm";
    }

    /**
     * 此方法未验证nameOrTemplate和stringTemplate参数实际值是否一致
     *
     * @param nameOrTemplate 模板名称或者字符串模板
     * @param stringTemplate nameOrTemplate参数是否是模板内容, 为true，则将nameOrTemplate参数视为模板文本，为false则将nameOrTemplate参数视为模板名称
     * @return 模板
     * @see Template#getName()
     * @see VelocityEngine#getTemplate(String, String)
     */
    @Override
    public  Template getTemplate(String nameOrTemplate, boolean stringTemplate) {
        if (stringTemplate) {
            return new StringTemplate(nameOrTemplate);
        }
        org.apache.velocity.Template template = engine.getTemplate(nameOrTemplate, StandardCharsets.UTF_8.name());
        return new VelocityTemplate(template);
    }

    /**
     * 可在RUNTIME_DEFAULT_DIRECTIVES.properties配置文件中配置默认指令
     *
     * @param directive 指令实现
     * @return 是否注册成功
     * @see org.apache.velocity.runtime.parser.node.ASTDirective#init(InternalContextAdapter, Object) 根据指令名称初始化ASTDirective指令对象
     * @see org.apache.velocity.runtime.directive.RuntimeMacro
     */
    @Override
    public <D extends TemplateDirective> boolean registerDirective(D directive) {
        RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
        if (runtimeServices instanceof RuntimeInstance ri) {
            if (directive instanceof Directive td) {
                ri.addDirective(td);
                return true;
            }

            // TODO Velocity 针对每个 Directive 都是反射创建对象
            // 无法包装 TemplateDirective 子类
            // 能注册的实际运行的指定必须继承 Directive 类

            // TODO 这样做会不会有问题 ?
            // 在 init 方法里获取到注册的 TemplateDirective 实例
            // 匿名内部类没有构造函数，不能反射创建

            // 必须继承 Directive 类
            ri.addDirective(new VelocityTemplateDirectiveWrapper(directive));
            return true;
        }
        return false;
    }
}
