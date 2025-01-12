package io.devpl.fxui.bridge;

import lombok.Getter;
import lombok.Setter;
import org.mybatis.generator.config.PropertyHolder;

/**
 * 项目配置
 */
@Getter
@Setter
public class ProjectConfiguration extends PropertyHolder {

    /**
     * 配置名称
     */
    private String name;

    /**
     * 项目所在根目录
     */
    private String projectRootFolder;

    /**
     * 父包名
     */
    private String parentPackage;

    /**
     * 实体类所在包名
     */
    private String entityPackageName;

    /**
     * 实体类存放目录：相对目录
     */
    private String entityPackageFolder;

    /**
     * Mapper接口包名
     */
    private String mapperPackageName;

    /**
     * Mapper接口存放目录
     */
    private String mapperFolder;

    /**
     * 映射XML文件包名
     */
    private String mapperXmlPackage;

    /**
     * 映射XML文件存放目录
     */
    private String mapperXmlFolder;
    /**
     * 项目结构
     */
    private ProjectArchetype projectArchetype;

    private ProjectConfiguration(Builder builder) {
        this.name = builder.name;
        this.projectRootFolder = builder.projectRootFolder;
        this.parentPackage = builder.parentPackage;
        this.entityPackageName = builder.entityPackageName;
        this.entityPackageFolder = builder.entityPackageFolder;
        this.mapperPackageName = builder.mapperPackageName;
        this.mapperFolder = builder.mapperFolder;
        this.mapperXmlPackage = builder.mapperXmlPackage;
        this.mapperXmlFolder = builder.mapperXmlFolder;
        this.projectArchetype = builder.projectArchetype;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String name;
        private String projectRootFolder;
        private String parentPackage;
        private String entityPackageName;
        private String entityPackageFolder;
        private String mapperPackageName;
        private String mapperFolder;
        private String mapperXmlPackage;
        private String mapperXmlFolder;
        private ProjectArchetype projectArchetype;

        private Builder() {
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withProjectRootFolder(String projectRootFolder) {
            this.projectRootFolder = projectRootFolder;
            return this;
        }

        public Builder withParentPackage(String parentPackage) {
            this.parentPackage = parentPackage;
            return this;
        }

        public Builder withEntityPackageName(String entityPackageName) {
            this.entityPackageName = entityPackageName;
            return this;
        }

        public Builder withEntityPackageFolder(String entityPackageFolder) {
            this.entityPackageFolder = entityPackageFolder;
            return this;
        }

        public Builder withMapperPackageName(String mapperPackageName) {
            this.mapperPackageName = mapperPackageName;
            return this;
        }

        public Builder withMapperFolder(String mapperFolder) {
            this.mapperFolder = mapperFolder;
            return this;
        }

        public Builder withMapperXmlPackage(String mapperXmlPackage) {
            this.mapperXmlPackage = mapperXmlPackage;
            return this;
        }

        public Builder withMapperXmlFolder(String mapperXmlFolder) {
            this.mapperXmlFolder = mapperXmlFolder;
            return this;
        }

        public Builder withProjectArchetype(ProjectArchetype projectArchetype) {
            this.projectArchetype = projectArchetype;
            return this;
        }

        public ProjectConfiguration build() {

            if (projectArchetype == null) {
                projectArchetype = new SimpleMavenProjectArchetype();
            }

            return new ProjectConfiguration(this);
        }
    }
}
