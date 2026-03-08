package org.assistant.tools.crud;

/**
 * Configuration object for the CRUD code generator.
 * All settings are persisted via {@link java.util.prefs.Preferences}.
 */
public class CrudGeneratorConfig {

    // ── Package / project settings ─────────────────────────────────────────
    private String basePackage = "com.example";
    private String moduleName = ""; // optional sub-module, e.g. "user"
    private String author = System.getProperty("user.name", "author");

    // ── Table → class name settings ────────────────────────────────────────
    /** Comma-separated table prefixes to strip, e.g. "t_,tbl_". */
    private String tablePrefix = "t_,tbl_";

    // ── Code style ─────────────────────────────────────────────────────────
    private boolean useLombok = true;
    private boolean useSwagger = false;

    // ── Super-class names (empty = no super-class) ─────────────────────────
    private String entitySuperClass = "";
    private String controllerSuperClass = "";

    // ── Output path (optional; shown in UI only, no actual file writing unless
    // user clicks Save) ──
    private String outputBaseDir = System.getProperty("user.home") + "/generated";

    // ── Template selection settings ────────────────────────────────────────
    private String entityTemplate = "crud-entity.vm";
    private String mapperJavaTemplate = "crud-mapper-java.vm";
    private String mapperXmlTemplate = "crud-mapper-xml.vm";
    private String serviceTemplate = "crud-service.vm";
    private String serviceImplTemplate = "crud-service-impl.vm";
    private String controllerTemplate = "crud-controller.vm";
    private String sqlTemplate = "crud-create-table.vm";

    // ── Getters & Setters ──────────────────────────────────────────────────

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String v) {
        this.basePackage = v;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String v) {
        this.moduleName = v;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String v) {
        this.author = v;
    }

    public String getTablePrefix() {
        return tablePrefix;
    }

    public void setTablePrefix(String v) {
        this.tablePrefix = v;
    }

    public boolean isUseLombok() {
        return useLombok;
    }

    public void setUseLombok(boolean v) {
        this.useLombok = v;
    }

    public boolean isUseSwagger() {
        return useSwagger;
    }

    public void setUseSwagger(boolean v) {
        this.useSwagger = v;
    }

    public String getEntitySuperClass() {
        return entitySuperClass;
    }

    public void setEntitySuperClass(String v) {
        this.entitySuperClass = v;
    }

    public String getControllerSuperClass() {
        return controllerSuperClass;
    }

    public void setControllerSuperClass(String v) {
        this.controllerSuperClass = v;
    }

    public String getOutputBaseDir() {
        return outputBaseDir;
    }

    public void setOutputBaseDir(String v) {
        this.outputBaseDir = v;
    }

    public String getEntityTemplate() {
        return entityTemplate;
    }

    public void setEntityTemplate(String v) {
        this.entityTemplate = v;
    }

    public String getMapperJavaTemplate() {
        return mapperJavaTemplate;
    }

    public void setMapperJavaTemplate(String v) {
        this.mapperJavaTemplate = v;
    }

    public String getMapperXmlTemplate() {
        return mapperXmlTemplate;
    }

    public void setMapperXmlTemplate(String v) {
        this.mapperXmlTemplate = v;
    }

    public String getServiceTemplate() {
        return serviceTemplate;
    }

    public void setServiceTemplate(String v) {
        this.serviceTemplate = v;
    }

    public String getServiceImplTemplate() {
        return serviceImplTemplate;
    }

    public void setServiceImplTemplate(String v) {
        this.serviceImplTemplate = v;
    }

    public String getControllerTemplate() {
        return controllerTemplate;
    }

    public void setControllerTemplate(String v) {
        this.controllerTemplate = v;
    }

    public String getSqlTemplate() {
        return sqlTemplate;
    }

    public void setSqlTemplate(String v) {
        this.sqlTemplate = v;
    }

    // ── Derived helpers ────────────────────────────────────────────────────

    /**
     * Remove known table prefixes and convert to PascalCase class name.
     * e.g. "t_user_info" with prefix "t_" → "UserInfo"
     */
    public String tableNameToClassName(String tableName) {
        String stripped = tableName;
        for (String prefix : tablePrefix.split(",")) {
            String p = prefix.trim();
            if (!p.isEmpty() && stripped.toLowerCase().startsWith(p.toLowerCase())) {
                stripped = stripped.substring(p.length());
                break;
            }
        }
        // camelCase → PascalCase
        String camel = TableColumnInfo.toCamelCase(stripped);
        if (camel.isEmpty())
            return stripped;
        return Character.toUpperCase(camel.charAt(0)) + camel.substring(1);
    }

    /** e.g. basePackage="com.example", moduleName="user" → "com.example.user" */
    public String effectivePackage() {
        if (moduleName == null || moduleName.isBlank())
            return basePackage;
        return basePackage + "." + moduleName;
    }
}
