package org.assistant.tools.doc;

import lombok.Data;

import java.util.List;

@Data
public class WebApiInfo {

	String path;
	String method;
	List<ApiParam> params;
}
