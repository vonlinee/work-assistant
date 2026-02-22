package org.assistant.tools.doc;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single web API endpoint extracted from source code.
 */
@Data
public class WebApiInfo {

	/** HTTP method (GET, POST, PUT, DELETE, PATCH, etc.) */
	private String method;

	/** URL path (combined class-level + method-level) */
	private String path;

	/** Short summary (from annotation value or Javadoc first line) */
	private String summary;

	/** Full description (from Javadoc) */
	private String description;

	/** Java method name in the controller */
	private String methodName;

	/** Fully qualified controller class name */
	private String controllerClass;

	/** Return type (Java type name) */
	private String returnType;

	/** Resolved fields of the return type, when it is a complex object */
	private List<FieldInfo> returnTypeFields = new ArrayList<>();

	/** Request content types (e.g. application/json) */
	private List<String> consumes = new ArrayList<>();

	/** Response content types */
	private List<String> produces = new ArrayList<>();

	/** Whether this endpoint is deprecated */
	private boolean deprecated;

	/** Tags for grouping (e.g. controller name) */
	private List<String> tags = new ArrayList<>();

	/** All parameters */
	private List<ApiParam> params = new ArrayList<>();

	public void addParam(ApiParam param) {
		this.params.add(param);
	}

	/**
	 * Get parameters filtered by location.
	 */
	public List<ApiParam> getParamsByLocation(ParamLocation location) {
		List<ApiParam> result = new ArrayList<>();
		for (ApiParam param : params) {
			if (param.getIn() == location) {
				result.add(param);
			}
		}
		return result;
	}
}
