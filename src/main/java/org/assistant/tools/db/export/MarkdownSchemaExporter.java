package org.assistant.tools.db.export;

import org.assistant.tools.db.parser.ColumnInfo;
import org.assistant.tools.db.parser.DbSchema;
import org.assistant.tools.db.parser.TableInfo;

import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;

public class MarkdownSchemaExporter implements SchemaExporter {

    @Override
    public String getFormatName() {
        return "Markdown (*.md)";
    }

    @Override
    public String getFileExtension() {
        return "md";
    }

    @Override
    public void export(DbSchema schema, File outputFile) throws Exception {
        try (FileWriter writer = new FileWriter(outputFile, StandardCharsets.UTF_8)) {
            writer.write("# Database Schema Documentation\n\n");

            for (TableInfo table : schema.getTables()) {
                writer.write("## Table: `" + table.getName() + "`\n\n");
                if (table.getRemarks() != null && !table.getRemarks().isEmpty()) {
                    writer.write("**Description:** " + table.getRemarks() + "\n\n");
                }

                writer.write(
                        "| Column | Type | Size | Primary Key | Auto Increment | Nullable | Default | Remarks |\n");
                writer.write("|---|---|---|---|---|---|---|---|\n");

                for (ColumnInfo col : table.getColumns()) {
                    writer.write(String.format("| %s | %s | %d | %s | %s | %s | %s | %s |\n",
                            col.getName(),
                            col.getTypeName(),
                            col.getSize(),
                            col.isPrimaryKey() ? "Yes" : "No",
                            col.isAutoIncrement() ? "Yes" : "No",
                            col.isNullable() ? "Yes" : "No",
                            col.getDefaultValue() != null ? col.getDefaultValue() : "",
                            col.getRemarks() != null ? col.getRemarks().replace("\n", " ") : ""));
                }
                writer.write("\n");
            }
        }
    }
}
