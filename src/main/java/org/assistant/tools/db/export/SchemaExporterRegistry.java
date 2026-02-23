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

    public Collection<String> getFormatNames() {
        return exporters.keySet();
    }

    public Optional<SchemaExporter> getExporter(String formatName) {
        return Optional.ofNullable(exporters.get(formatName));
    }
}
