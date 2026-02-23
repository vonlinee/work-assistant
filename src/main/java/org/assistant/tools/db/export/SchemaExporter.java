package org.assistant.tools.db.export;

import org.assistant.tools.db.parser.DbSchema;
import java.io.File;

public interface SchemaExporter {
    String getFormatName();

    String getFileExtension();

    void export(DbSchema schema, File outputFile) throws Exception;
}
