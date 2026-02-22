package org.assistant.tools.doc;

import lombok.Data;

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
@Data
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

	/**
	 * Whether this parameter has resolved field info.
	 */
	public boolean hasFields() {
		return fields != null && !fields.isEmpty();
	}
}
