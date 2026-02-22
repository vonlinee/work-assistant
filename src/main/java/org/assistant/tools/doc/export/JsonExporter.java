package org.assistant.tools.doc.export;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.assistant.tools.doc.ApiProject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Exports API definitions to a structured JSON file.
 */
public class JsonExporter implements ApiExporter {

    @Override
    public String getFormatName() {
        return "JSON";
    }

    @Override
    public String getFileExtension() {
        return "json";
    }

    @Override
    public void export(ApiProject project, File output) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        String json = gson.toJson(project);
        Files.writeString(output.toPath(), json, StandardCharsets.UTF_8);
    }
}
