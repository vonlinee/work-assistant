package org.assistant.tools.doc;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single parameter of a web API endpoint.
 * <p>
 * For complex types (DTOs/POJOs), the {@link #fields} list contains
 * the resolved field information including each field's name, type,
 * and Javadoc description.
 * </p>
 */

public class ApiParam {

	/** Parameter name */
	private String name;

	/** Java data type (e.g. String, Integer, List<UserDto>) */
	private String dataType;

	/** Where the parameter is located */
	private ParamLocation in;

	/** Whether this parameter is required */
	private boolean required;

	/** Default value if any */
	private String defaultValue;

	/** Description (from Javadoc @param or annotation) */
	private String description;

	/** Example value */
	private String example;

	/**
	 * Resolved fields of this parameter's type, when it is a complex object.
	 * Empty for primitive / simple types.
	 */
	private List<FieldInfo> fields = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	public ParamLocation getIn() {
		return in;
	}

	public void setIn(ParamLocation in) {
		this.in = in;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getExample() {
		return example;
	}

	public void setExample(String example) {
		this.example = example;
	}

	public List<FieldInfo> getFields() {
		return fields;
	}

	public void setFields(List<FieldInfo> fields) {
		this.fields = fields;
	}

	/**
	 * Whether this parameter has resolved field info.
	 */
	public boolean hasFields() {
		return fields != null && !fields.isEmpty();
	}

	/**
	 * Gets the frontend data type for this parameter.
	 */
	public String getFrontendDataType() {
		return TypeConverter.toFrontendType(this.dataType);
	}
}
