package org.assistant.tools.doc;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single web API endpoint extracted from source code.
 */

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

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getControllerClass() {
		return controllerClass;
	}

	public void setControllerClass(String controllerClass) {
		this.controllerClass = controllerClass;
	}

	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public List<FieldInfo> getReturnTypeFields() {
		return returnTypeFields;
	}

	public void setReturnTypeFields(List<FieldInfo> returnTypeFields) {
		this.returnTypeFields = returnTypeFields;
	}

	public List<String> getConsumes() {
		return consumes;
	}

	public void setConsumes(List<String> consumes) {
		this.consumes = consumes;
	}

	public List<String> getProduces() {
		return produces;
	}

	public void setProduces(List<String> produces) {
		this.produces = produces;
	}

	public boolean isDeprecated() {
		return deprecated;
	}

	public void setDeprecated(boolean deprecated) {
		this.deprecated = deprecated;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public List<ApiParam> getParams() {
		return params;
	}

	public void setParams(List<ApiParam> params) {
		this.params = params;
	}

	public void addParam(ApiParam param) {
		this.params.add(param);
	}

	public List<ApiParam> getParamsByLocation(ParamLocation location) {
		List<ApiParam> result = new ArrayList<>();
		for (ApiParam param : params) {
			if (param.getIn() == location) {
				result.add(param);
			}
		}
		return result;
	}

	/**
	 * Gets the frontend return data type for this API.
	 */
	public String getFrontendReturnType() {
		return TypeConverter.toFrontendType(this.returnType);
	}
}
