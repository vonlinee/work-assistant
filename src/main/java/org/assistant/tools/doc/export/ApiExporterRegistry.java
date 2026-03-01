package org.assistant.tools.doc.export;

import java.util.*;

/**
 * Registry of available {@link ApiExporter} implementations.
 * <p>
 * Provides a central place to discover all supported export formats.
 * Built-in exporters are registered automatically. Custom exporters
 * can be added at runtime via {@link #register(ApiExporter)}.
 * </p>
 */
public class ApiExporterRegistry {

    private static final ApiExporterRegistry INSTANCE = new ApiExporterRegistry();

    private final Map<String, ApiExporter> exporters = new LinkedHashMap<>();

    private ApiExporterRegistry() {
        // Register built-in exporters
        register(new JsonExporter());
        register(new YamlExporter());
        register(new ExcelExporter());
        register(new DocxExporter(DocxExporter.FieldLayout.INLINE));
        register(new DocxExporter(DocxExporter.FieldLayout.SEPARATE));
        register(new OpenApiExporter(OpenApiExporter.OpenApiVersion.V3_0));
        register(new OpenApiExporter(OpenApiExporter.OpenApiVersion.V3_1));
        register(new OpenApiExporter(OpenApiExporter.OpenApiVersion.SWAGGER_2));
        register(new HtmlExporter());
        register(new MarkdownExporter());
    }

    public static ApiExporterRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Register or replace an exporter.
     */
    public void register(ApiExporter exporter) {
        exporters.put(exporter.getFormatName(), exporter);
    }

    public Optional<ApiExporter> getExporter(String formatName) {
        if (exporters.containsKey(formatName)) {
            return Optional.ofNullable(exporters.get(formatName));
        }
        for (String t : org.assistant.tools.util.TemplateManager.getAllAvailableTemplates()) {
            if (t.startsWith("api-") && t.endsWith(".vm")) {
                if (!t.equals("api-markdown.vm") && !t.equals("api-html.vm")
                        && getCustomFormatName(t).equals(formatName)) {
                    String ext = t.substring(4, t.length() - 3);
                    return Optional.of(new DynamicApiExporter(t, formatName, ext));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Get all registered exporters.
     */
    public List<ApiExporter> getAllExporters() {
        return new ArrayList<>(exporters.values());
    }

    private String getCustomFormatName(String templateName) {
        String base = templateName.substring(4, templateName.length() - 3);
        return "Custom (" + base + ")";
    }

    public List<String> getFormatNames() {
        List<String> names = new ArrayList<>(exporters.keySet());
        for (String t : org.assistant.tools.util.TemplateManager.getAllAvailableTemplates()) {
            if (t.startsWith("api-") && t.endsWith(".vm")) {
                if (!t.equals("api-markdown.vm") && !t.equals("api-html.vm")) {
                    String customName = getCustomFormatName(t);
                    if (!names.contains(customName)) {
                        names.add(customName);
                    }
                }
            }
        }
        return names;
    }
}
