package org.assistant.control;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Setter
@Getter
class ParameterMapping {
	private String property;

	public ParameterMapping(String property) {
		this.property = property;
	}
}

@Setter
@Getter
class ParamNode {
	private String key;
	private List<ParamNode> children;

	public ParamNode(String key) {
		this.key = key;
		this.children = new ArrayList<>();
	}

	public void addChild(ParamNode child) {
		this.children.add(child);
	}

	@Override
	public String toString() {
		return "ParamNode{" +
			"key='" + key + '\'' +
			", children=" + children +
			'}';
	}
}

public class TreeBuilder {

	public static void main(String[] args) {
		List<ParameterMapping> mappings = Arrays.asList(
			new ParameterMapping("aaa.bbb.ccc"),
			new ParameterMapping("aaa.bbb.ddd"),
			new ParameterMapping("aaa.eee"),
			new ParameterMapping("fff")
		);

		ParamNode tree = buildTree(mappings);
		System.out.println(tree);
	}

	private static ParamNode buildTree(List<ParameterMapping> mappings) {
		ParamNode root = new ParamNode("root"); // 根节点
		for (ParameterMapping mapping : mappings) {
			String[] parts = mapping.getProperty().split("\\.");
			addToTree(root, parts);
		}
		return root;
	}

	private static void addToTree(ParamNode currentNode, String[] parts) {
		for (String part : parts) {
			Optional<ParamNode> existingNode = currentNode.getChildren().stream()
				.filter(child -> child.getKey().equals(part))
				.findFirst();
			if (existingNode.isPresent()) {
				currentNode = existingNode.get(); // 如果节点存在，进入该节点
			} else {
				ParamNode newNode = new ParamNode(part);
				currentNode.addChild(newNode);
				currentNode = newNode; // 进入新节点
			}
		}
	}
}