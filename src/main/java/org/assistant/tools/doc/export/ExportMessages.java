package org.assistant.tools.doc.export;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Provides locale-aware labels for API documentation export.
 * <p>
 * Uses {@code export_messages[_xx].properties} resource bundles.
 * Falls back to English when no locale-specific bundle is found.
 * </p>
 */
public final class ExportMessages {

    private static final String BUNDLE_NAME = "message.export_messages";
    private final ResourceBundle bundle;

    private static final ExportMessages INSTANCE = new ExportMessages(Locale.getDefault());

    private ExportMessages(Locale locale) {
        this.bundle = ResourceBundle.getBundle(BUNDLE_NAME, locale);
    }

    public static ExportMessages getInstance() {
        return INSTANCE;
    }

    public static ExportMessages of(Locale locale) {
        return new ExportMessages(locale);
    }

    public String get(String key) {
        try {
            return bundle.getString(key);
        } catch (Exception e) {
            return key;
        }
    }

    // ---- Convenience accessors ----

    public String apiDocumentation() {
        return get("export.title.apiDocumentation");
    }

    public String version() {
        return get("export.label.version");
    }

    public String tableOfContents() {
        return get("export.label.tableOfContents");
    }

    public String parameters() {
        return get("export.label.parameters");
    }

    public String returnType() {
        return get("export.label.returnType");
    }

    public String responseFields() {
        return get("export.label.responseFields");
    }

    public String deprecated() {
        return get("export.label.deprecated");
    }

    public String basePath() {
        return get("export.label.basePath");
    }

    public String fields() {
        return get("export.label.fields");
    }

    public String tocHint() {
        return get("export.label.tocHint");
    }

    // Table headers
    public String headerName() {
        return get("export.header.name");
    }

    public String headerLocation() {
        return get("export.header.location");
    }

    public String headerType() {
        return get("export.header.type");
    }

    public String headerRequired() {
        return get("export.header.required");
    }

    public String headerDefault() {
        return get("export.header.default");
    }

    public String headerDescription() {
        return get("export.header.description");
    }

    public String headerField() {
        return get("export.header.field");
    }

    public String headerIn() {
        return get("export.header.in");
    }

    public String headerMethod() {
        return get("export.header.method");
    }

    public String headerPath() {
        return get("export.header.path");
    }

    public String headerSummary() {
        return get("export.header.summary");
    }

    public String headerReturnType() {
        return get("export.header.returnType");
    }

    public String headerParams() {
        return get("export.header.params");
    }

    public String headerController() {
        return get("export.header.controller");
    }

    public String yes() {
        return get("export.value.yes");
    }

    public String no() {
        return get("export.value.no");
    }
}
