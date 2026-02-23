package org.assistant.tools.db.export;

import org.assistant.tools.db.parser.ColumnInfo;
import org.assistant.tools.db.parser.DbSchema;
import org.assistant.tools.db.parser.TableInfo;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileOutputStream;

public class XmlSchemaExporter implements SchemaExporter {

    @Override
    public String getFormatName() {
        return "XML (DdlUtils) (*.xml)";
    }

    @Override
    public String getFileExtension() {
        return "xml";
    }

    @Override
    public void export(DbSchema schema, File outputFile) throws Exception {
        Document document = DocumentHelper.createDocument();
        Element database = document.addElement("database");
        database.addAttribute("name", schema.getCatalog() != null ? schema.getCatalog() : "schema");

        for (TableInfo table : schema.getTables()) {
            Element tableElement = database.addElement("table");
            tableElement.addAttribute("name", table.getName());
            if (table.getRemarks() != null && !table.getRemarks().isEmpty()) {
                tableElement.addAttribute("description", table.getRemarks());
            }

            for (ColumnInfo col : table.getColumns()) {
                Element columnElement = tableElement.addElement("column");
                columnElement.addAttribute("name", col.getName());
                columnElement.addAttribute("primaryKey", String.valueOf(col.isPrimaryKey()));
                columnElement.addAttribute("required", String.valueOf(!col.isNullable()));
                columnElement.addAttribute("type", col.getTypeName());
                columnElement.addAttribute("size", String.valueOf(col.getSize()));
                columnElement.addAttribute("autoIncrement", String.valueOf(col.isAutoIncrement()));
                if (col.getDefaultValue() != null) {
                    columnElement.addAttribute("default", col.getDefaultValue());
                }
                if (col.getRemarks() != null && !col.getRemarks().isEmpty()) {
                    columnElement.addAttribute("description", col.getRemarks());
                }
            }
        }

        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setEncoding("UTF-8");
        try (FileOutputStream fos = new FileOutputStream(outputFile)) {
            XMLWriter writer = new XMLWriter(fos, format);
            writer.write(document);
            writer.flush();
        }
    }
}
