package org.assistant.tools.mybatis;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.mapping.ParameterMapping;
import org.jdesktop.swingx.treetable.DefaultMutableTreeTableNode;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ParamNode extends DefaultMutableTreeTableNode {

	/**
	 * 参数Key
	 * 1. 嵌套形式，比如 user.name
	 * 2. 非嵌套形式, {@link ParamNode#getChildren()}
	 */
	private String key;

	/**
	 * 字面值
	 */
	private String value;

	/**
	 * jdbcType
	 */
	private String jdbcType;

	/**
	 * 数据类型 (Now stored as a String to support arbitrary configuration items)
	 */
	private String dataType;

	/**
	 * 参数元数据
	 */
	@Nullable
	private ParameterMapping parameterMapping;

	private List<ParamNode> children;

	public ParamNode() {
		this(null, null, ParamDataType.UNKNOWN.name());
	}

	public ParamNode(String key, String value, String dataType) {
		this.key = key;
		this.value = value;
		this.dataType = dataType;
	}

	public boolean hasChildren() {
		return getChildCount() > 0;
	}

	public void addChild(ParamNode child) {
		add(child);
	}

	@Override
	public String toString() {
		return key;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
}
