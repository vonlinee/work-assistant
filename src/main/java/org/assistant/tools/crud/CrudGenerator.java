package org.assistant.tools.crud;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.assistant.tools.util.TemplateManager;

import java.io.StringWriter;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Core CRUD code generation engine.
 *
 * <p>
 * For each selected table it:
 * <ol>
 * <li>Loads column metadata via {@link TableSchemaLoader}.</li>
 * <li>Builds a Velocity context with table / column / config data.</li>
 * <li>Renders all seven {@code crud-*.vm} templates.</li>
 * <li>Returns a map of relative file path → generated file content.</li>
 * </ol>
 */
public class CrudGenerator {

    private static class TemplateInfo {
        String name;
        String pathPattern;

        TemplateInfo(String name, String pathPattern) {
            this.name = name;
            this.pathPattern = pathPattern;
        }
    }

    private final CrudGeneratorConfig config;
    private final TableSchemaLoader loader;

    public CrudGenerator(CrudGeneratorConfig config, TableSchemaLoader loader) {
        this.config = config;
        this.loader = loader;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Generates CRUD files for each table in {@code tableNames}.
     *
     * @return ordered map of &quot;relative/path/to/File.java&quot; → file content
     */
    public Map<String, String> generate(List<String> tableNames) throws Exception {
        Map<String, String> results = new LinkedHashMap<>();
        VelocityEngine ve = buildVelocityEngine();

        for (String tableName : tableNames) {
            List<TableColumnInfo> columns = loader.loadColumns(tableName);
            String className = config.tableNameToClassName(tableName);
            String pkg = config.effectivePackage();
            String subDir = pkg.replace('.', '/');

            VelocityContext ctx = buildContext(tableName, className, pkg, columns);

            for (TemplateInfo entry : getTemplates()) {
                String templateName = entry.name;
                String pathPattern = entry.pathPattern;

                String templateContent;
                try {
                    templateContent = TemplateManager.readTemplate(templateName);
                } catch (Exception e) {
                    // Template missing – skip
                    continue;
                }
                if (templateContent == null || templateContent.isBlank())
                    continue;

                String rendered = render(ve, templateName, templateContent, ctx);
                String filePath = String.format(pathPattern, subDir, className);
                results.put(filePath, rendered);
            }
        }
        return results;
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private List<TemplateInfo> getTemplates() {
        return List.of(
                new TemplateInfo(config.getEntityTemplate(), "%s/%s.java"),
                new TemplateInfo(config.getMapperJavaTemplate(), "%s/mapper/%sMapper.java"),
                new TemplateInfo(config.getMapperXmlTemplate(), "%s/mapper/%sMapper.xml"),
                new TemplateInfo(config.getServiceTemplate(), "%s/service/I%sService.java"),
                new TemplateInfo(config.getServiceImplTemplate(), "%s/service/impl/%sServiceImpl.java"),
                new TemplateInfo(config.getControllerTemplate(), "%s/controller/%sController.java"),
                new TemplateInfo(config.getSqlTemplate(), "%s/sql/%s.sql"));
    }

    private VelocityContext buildContext(String tableName, String className,
            String pkg, List<TableColumnInfo> columns) {
        VelocityContext ctx = new VelocityContext();
        ctx.put("tableName", tableName);
        ctx.put("className", className);
        ctx.put("classNameLC", Character.toLowerCase(className.charAt(0)) + className.substring(1));
        ctx.put("package", pkg);
        ctx.put("author", config.getAuthor());
        ctx.put("date", LocalDate.now().toString());
        ctx.put("columns", columns);
        ctx.put("useLombok", config.isUseLombok());
        ctx.put("useSwagger", config.isUseSwagger());
        ctx.put("entitySuper", config.getEntitySuperClass());
        ctx.put("ctrlSuper", config.getControllerSuperClass());
        // primary-key column (first PK found, or first column)
        TableColumnInfo pk = columns.stream()
                .filter(TableColumnInfo::isPrimaryKey)
                .findFirst()
                .orElse(columns.isEmpty() ? null : columns.get(0));
        ctx.put("pk", pk);
        return ctx;
    }

    /** Renders a single Velocity template string with the given context. */
    private String render(VelocityEngine ve, String templateName,
            String templateContent, VelocityContext ctx) throws Exception {
        // Register / overwrite the template in the string-resource repository
        StringResourceRepository repo = StringResourceLoader.getRepository();
        repo.putStringResource(templateName, templateContent);

        Template template = ve.getTemplate(templateName, "UTF-8");
        StringWriter sw = new StringWriter();
        template.merge(ctx, sw);
        return sw.toString();
    }

    private VelocityEngine buildVelocityEngine() {
        Properties props = new Properties();
        props.setProperty(RuntimeConstants.RESOURCE_LOADERS, "string");
        props.setProperty("resource.loader.string.class",
                StringResourceLoader.class.getName());
        props.setProperty("resource.loader.string.repository.static", "true");
        VelocityEngine ve = new VelocityEngine();
        ve.init(props);
        return ve;
    }
}
