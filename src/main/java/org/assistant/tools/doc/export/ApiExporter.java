package org.assistant.tools.doc.export;

import org.assistant.tools.doc.ApiProject;

import java.io.File;
import java.io.IOException;

/**
 * SPI interface for exporting parsed API definitions to various formats.
 * <p>
 * Implementations convert an {@link ApiProject} to a specific file format.
 * New export formats can be added by implementing this interface and
 * registering
 * in {@link ApiExporterRegistry}.
 * </p>
 *
 * <h3>Extensibility</h3>
 * <ol>
 * <li>Implement this interface</li>
 * <li>Return a unique format name from {@link #getFormatName()}</li>
 * <li>Return the file extension from {@link #getFileExtension()}</li>
 * <li>Register via {@link ApiExporterRegistry#register(ApiExporter)}</li>
 * </ol>
 */
public interface ApiExporter {

    /**
     * Human-readable format name (e.g. "JSON", "OpenAPI 3.0", "Excel").
     */
    String getFormatName();

    /**
     * File extension without dot (e.g. "json", "xlsx", "yaml").
     */
    String getFileExtension();

    /**
     * Export the API project to the given output file.
     *
     * @param project the parsed API definitions
     * @param output  target file path
     * @throws IOException if writing fails
     */
    void export(ApiProject project, File output) throws IOException;
}
