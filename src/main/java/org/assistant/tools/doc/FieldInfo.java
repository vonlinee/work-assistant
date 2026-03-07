package org.assistant.tools.doc;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes a single field within a complex type (DTO/POJO).
 * <p>
 * When a parameter or return type is a complex Java object, the parser
 * resolves the class and populates a list of {@code FieldInfo} instances
 * representing each field's name, type, and documentation.
 * </p>
 */

public class FieldInfo {

    /** Field name (Java identifier) */
    private String name;

    /** Java type name (e.g. "String", "Integer", "List<Address>") */
    private String type;

    /** Description from Javadoc comment on the field */
    private String description;

    /** Description from @comment or other annotation */
    private String comment;

    /** Whether the field is required (e.g. @NotNull, @NotBlank) */
    private boolean required;

    /** Example value (from @Schema(example=...) or similar annotation) */
    private String example;

    /** Default value (from field initializer) */
    private String defaultValue;

    /** Nested fields, if this field is itself a complex type */
    private List<FieldInfo> children = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public List<FieldInfo> getChildren() {
        return children;
    }

    public void setChildren(List<FieldInfo> children) {
        this.children = children;
    }

    /**
     * Whether this field has nested children (i.e. is itself a complex type).
     */
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    /**
     * Gets the frontend data type for this field.
     */
    public String getFrontendType() {
        return TypeConverter.toFrontendType(this.type);
    }
}
