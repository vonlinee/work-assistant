package org.workassistant.ui.bridge;

import org.mybatis.generator.config.Context;

/**
 * 生成目标文件类型
 * 相当于一个初始化模板，初始化文件的数据
 */
public interface TargetFile {

    /**
     * 文件类型的唯一名称
     *
     * @return 文件类型的唯一名称
     */
    String getName();

    /**
     * 初始化文件信息
     *
     * @param file 文件信息
     */
    void initialize(GeneratedFile file);

    /**
     * 根据生成目标初始化
     *
     * @param target 生成目标
     */
    void initialize(GenerationTarget target);

    /**
     * 生成该类型文件使用的 FileGenerator
     *
     * @return FileGenerator
     */
    FileGenerator getFileGenerator(Context context);

    /**
     * 文件扩展名
     *
     * @param target 生成目标
     * @return 文件扩展名
     */
    String getExtension(GenerationTarget target);

    /**
     * 文件名
     *
     * @param target 生成目标
     * @return 该文件类型默认的文件名模板
     */
    String getFilename(GenerationTarget target);
}
