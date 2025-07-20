package org.assistant.tools.mybatis;

import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.mapping.ParameterMapping;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
public class ParamNode extends DefaultMutableTreeNode {

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
	 * 数据类型
	 */
	private ParamDataType dataType;

	/**
	 * 参数元数据
	 */
	@Nullable
	private ParameterMapping parameterMapping;

	private List<ParamNode> children;

	public ParamNode() {
		this(null, null, ParamDataType.UNKNOWN);
	}

	public ParamNode(String key, String value, ParamDataType dataType) {
		this.key = key;
		this.value = value;
		this.dataType = dataType;
	}

	public boolean hasChildren() {
		return children != null && !children.isEmpty();
	}

	public void addChild(ParamNode child) {
		if (children == null) {
			children = new ArrayList<>();
		}
		children.add(child);
	}

	@Override
	public String toString() {
		return key;
	}

	public void setDataType(String dataType) {
		this.dataType = ParamDataType.valueOf(dataType);
	}
}
