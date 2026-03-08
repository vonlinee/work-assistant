package org.assistant.tools.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TemplateManager {

    private static final String CUSTOM_TEMPLATE_DIR = System.getProperty("user.home") + File.separator
            + ".work-assistant" + File.separator + "templates";
    private static final String CLASSPATH_TEMPLATE_DIR = "templates"; // Used for fetching defaults via
                                                                      // reflection/resource scanning if needed, though
                                                                      // for now we can just hardcode known defaults.

    private static final List<String> DEFAULT_TEMPLATES = List.of(
            "api-markdown.vm",
            "api-html.vm",
            "crud-controller.vm",
            "crud-create-table.vm",
            "crud-entity.vm",
            "crud-mapper-java.vm",
            "crud-mapper-xml.vm",
            "crud-service.vm",
            "crud-service-impl.vm",
            "db-schema-markdown.vm",
            "db-schema-xml.vm");

    /**
     * Retrieves a list of all available templates (combining defaults and any
     * custom .vm files).
     */
    public static List<String> getAllAvailableTemplates() {
        Set<String> templates = new HashSet<>(DEFAULT_TEMPLATES);

        File dir = new File(CUSTOM_TEMPLATE_DIR);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".vm"));
            if (files != null) {
                for (File f : files) {
                    templates.add(f.getName());
                }
            }
        }

        return templates.stream().sorted().collect(Collectors.toList());
    }

    /**
     * Reads the content of a template. It first tries to read from the custom
     * directory, then falls back to the classpath.
     */
    public static String readTemplate(String templateName) throws IOException {
        File customFile = new File(CUSTOM_TEMPLATE_DIR, templateName);
        if (customFile.exists() && customFile.isFile()) {
            return Files.readString(customFile.toPath(), StandardCharsets.UTF_8);
        }

        // Fallback to classpath
        String classpathPath = CLASSPATH_TEMPLATE_DIR + "/" + templateName;
        try (java.io.InputStream is = TemplateManager.class.getClassLoader().getResourceAsStream(classpathPath)) {
            if (is == null) {
                return ""; // Template doesn't exist yet, return empty for new template creation
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    /**
     * Saves a template to the custom template directory, overriding any default
     * behavior.
     */
    public static void saveTemplate(String templateName, String content) throws IOException {
        File dir = new File(CUSTOM_TEMPLATE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File customFile = new File(dir, templateName);
        Files.writeString(customFile.toPath(), content, StandardCharsets.UTF_8);
    }

    /**
     * Deletes a custom template file.
     */
    public static boolean deleteCustomTemplate(String templateName) {
        File customFile = new File(CUSTOM_TEMPLATE_DIR, templateName);
        if (customFile.exists() && customFile.isFile()) {
            return customFile.delete();
        }
        return false;
    }

    /**
     * Indicates if a template is overridden or completely custom (exists in the
     * custom directory).
     */
    public static boolean isCustomized(String templateName) {
        File customFile = new File(CUSTOM_TEMPLATE_DIR, templateName);
        return customFile.exists() && customFile.isFile();
    }
}
