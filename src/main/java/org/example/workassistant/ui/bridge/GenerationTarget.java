package org.example.workassistant.ui.bridge;

import org.example.workassistant.ui.model.TableGeneration;

import java.util.Map;

/**
 * @see TableGeneration 以表作为代码生成的对象
 */
public interface GenerationTarget {

    /**
     * 名称
     *
     * @return 生成单元名称
     */
    String getName();

    /**
     * 数据模型
     *
     * @return 数据模型
     */
    Map<String, Object> getDataModel();
}
