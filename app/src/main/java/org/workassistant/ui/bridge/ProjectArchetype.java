package org.workassistant.ui.bridge;

import java.io.File;

/**
 * 项目骨架
 */
public abstract class ProjectArchetype extends PropertyHolder {

    /**
     * 项目根目录
     */
    protected String rootDir;

    /**
     * 模块名称
     */
    protected String moduleName;

    /**
     * 根目录
     *
     * @param rootDir 根目录
     */
    public final void setRootDirectory(String rootDir) {
        this.rootDir = rootDir;
    }

    /**
     * 设置模块名称
     *
     * @param module 模块名称
     */
    public final void setModuleName(String module) {
        this.moduleName = module;
    }

    /**
     * 是否是项目根目录
     *
     * @param file 目录
     * @return 是否是项目根目录
     */
    public abstract boolean isProjectRoot(File file);

    /**
     * 返回相对于项目根路径的相对路径
     *
     * @param file 文件信息
     * @return 文件存放位置，相对路径
     */
    public abstract String locate(GeneratedFile file);
}
