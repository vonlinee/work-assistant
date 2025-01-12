package io.devpl.fxui.controller.fields;

import io.devpl.common.interfaces.FieldParser;
import io.devpl.common.interfaces.impl.FieldInfoMap;
import io.devpl.fxui.model.FieldNode;
import io.devpl.fxui.utils.FXUtils;
import javafx.scene.Node;
import javafx.scene.layout.Region;

import java.util.ArrayList;
import java.util.List;

/**
 * 字段解析UI界面
 */
abstract class FieldParseView extends Region {

    Node root;

    public FieldParseView() {
        this.root = createRootNode();
        if (this.root != null)
            getChildren().add(root);
    }

    /**
     * 标签名
     *
     * @return 标签名
     */
    abstract String getName();

    /**
     * 创建UI部分
     *
     * @return UI根节点
     */
    abstract Node createRootNode();

    /**
     * 获取可解析的文本
     *
     * @return 可解析的文本
     */
    public abstract String getParseableText();

    /**
     * 填充示例文本
     */
    public abstract void fillSampleText();

    /**
     * 获取示例文本
     *
     * @return 示例文本
     */
    public abstract String getSampleText();

    /**
     * 解析文本得到字段
     *
     * @return 字段信息
     */
    public List<FieldNode> parse(String text) {
        List<FieldNode> nodes = new ArrayList<>();
        for (FieldInfoMap fieldInfoMap : FieldInfoMap.wrap(getFieldParser().parse(text))) {
            FieldNode node = new FieldNode();
            node.setName(fieldInfoMap.getFieldName());
            node.setDataType(fieldInfoMap.getFieldDataType());
            node.setDescription(fieldInfoMap.getFieldDescription());
            nodes.add(node);
        }
        return nodes;
    }

    /**
     * 自定义解析器
     *
     * @return 字段解析器
     */
    protected FieldParser getFieldParser() {
        return FieldParser.EMPTY;
    }

    @Override
    protected void layoutChildren() {
        if (root != null) {
            FXUtils.layoutRoot(this, root);
        }
    }
}
