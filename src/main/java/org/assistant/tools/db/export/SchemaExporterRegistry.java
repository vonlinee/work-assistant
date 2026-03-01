package org.assistant.tools.db.export;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class SchemaExporterRegistry {
    private static final SchemaExporterRegistry INSTANCE = new SchemaExporterRegistry();
    private final Map<String, SchemaExporter> exporters = new LinkedHashMap<>();

    private SchemaExporterRegistry() {
        register(new MarkdownSchemaExporter());
        register(new XmlSchemaExporter());
        register(new ExcelSchemaExporter());
        register(new WordSchemaExporter());
    }

    public static SchemaExporterRegistry getInstance() {
        return INSTANCE;
    }

    private void register(SchemaExporter exporter) {
        exporters.put(exporter.getFormatName(), exporter);
    }

    private String getCustomFormatName(String templateName) {
        String base = templateName.substring(10, templateName.length() - 3);
        return "Custom (" + base + ")";
    }

    public Collection<String> getFormatNames() {
        java.util.List<String> names = new java.util.ArrayList<>(exporters.keySet());
        for (String t : org.assistant.tools.util.TemplateManager.getAllAvailableTemplates()) {
            if (t.startsWith("db-schema-") && t.endsWith(".vm")) {
                if (!t.equals("db-schema-markdown.vm") && !t.equals("db-schema-xml.vm")) {
                    String customName = getCustomFormatName(t);
                    if (!names.contains(customName)) {
                        names.add(customName);
                    }
                }
            }
        }
        return names;
    }

    public Optional<SchemaExporter> getExporter(String formatName) {
        if (exporters.containsKey(formatName)) {
            return Optional.ofNullable(exporters.get(formatName));
        }
        for (String t : org.assistant.tools.util.TemplateManager.getAllAvailableTemplates()) {
            if (t.startsWith("db-schema-") && t.endsWith(".vm")) {
                if (!t.equals("db-schema-markdown.vm") && !t.equals("db-schema-xml.vm")
                        && getCustomFormatName(t).equals(formatName)) {
                    String ext = t.substring(10, t.length() - 3);
                    return Optional.of(new DynamicSchemaExporter(t, formatName, ext));
                }
            }
        }
        return Optional.empty();
    }
}
