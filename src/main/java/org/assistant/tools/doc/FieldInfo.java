package org.assistant.tools.doc;

import lombok.Data;

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
@Data
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

    /**
     * Whether this field has nested children (i.e. is itself a complex type).
     */
    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }
}
