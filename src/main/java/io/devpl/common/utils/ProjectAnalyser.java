package io.devpl.common.utils;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;

public interface ProjectAnalyser {

    /**
     * 是否是项目根目录
     *
     * @param entryFile 入口File
     * @return 是否是项目根目录
     */
    boolean isProjectRootDirectory(File entryFile);

    /**
     * 获取源代码根目录
     *
     * @param projectRoot 项目根目录
     * @return 源代码根目录
     */
    Path getSourceRoot(Path projectRoot);

    /**
     * 获取资源目录根目录
     *
     * @param projectRoot 项目根目录
     * @return 源代码根目录
     */
    Path getResourceRoot(Path projectRoot);

    /**
     * 获取项目根路径下的所有包名
     *
     * @param projectRoot 项目根路径
     * @return 项目根路径下的所有包名
     */
    Set<String> getPackageNames(File projectRoot);

    /**
     * 解析项目结构
     *
     * @param entryFile 项目工程入口文件
     * @return 项目信息
     */
    ProjectModule analyse(File entryFile);
}
