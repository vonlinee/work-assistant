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

    /**
     * Get an exporter by its format name.
     */
    public Optional<ApiExporter> getExporter(String formatName) {
        return Optional.ofNullable(exporters.get(formatName));
    }

    /**
     * Get all registered exporters.
     */
    public List<ApiExporter> getAllExporters() {
        return new ArrayList<>(exporters.values());
    }

    /**
     * Get all registered format names (for UI combo box).
     */
    public List<String> getFormatNames() {
        return new ArrayList<>(exporters.keySet());
    }
}
