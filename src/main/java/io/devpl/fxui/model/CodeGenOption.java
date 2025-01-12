package io.devpl.fxui.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * 针对单个表生成的配置选项
 * 会影响使用到的插件
 */
public class CodeGenOption {

    private final BooleanProperty useMyBatisPlus = new SimpleBooleanProperty(true); // 是否使用mybatis-plus
    private final BooleanProperty offsetLimit = new SimpleBooleanProperty();
    private final BooleanProperty comment = new SimpleBooleanProperty();
    private final BooleanProperty overrideXML = new SimpleBooleanProperty();
    private final BooleanProperty needToStringHashcodeEquals = new SimpleBooleanProperty();
    private final BooleanProperty useLombokPlugin = new SimpleBooleanProperty();
    private final BooleanProperty needForUpdate = new SimpleBooleanProperty();
    // 是否注解DAO
    private final BooleanProperty annotationDAO = new SimpleBooleanProperty();
    private final BooleanProperty annotation = new SimpleBooleanProperty();
    // 是否使用真实的列名
    private final BooleanProperty useActualColumnNames = new SimpleBooleanProperty();
    private final BooleanProperty useExample = new SimpleBooleanProperty();
    private final StringProperty generateKeys = new SimpleStringProperty();
    private final StringProperty encoding = new SimpleStringProperty();
    private final BooleanProperty useTableNameAlias = new SimpleBooleanProperty();
    private final BooleanProperty useDAOExtendStyle = new SimpleBooleanProperty();
    private final BooleanProperty useSchemaPrefix = new SimpleBooleanProperty();
    private final BooleanProperty jsr310Support = new SimpleBooleanProperty();

    /**
     * 是否支持swagger
     */
    private final BooleanProperty swaggerSupport = new SimpleBooleanProperty();

    /**
     * 是否支持MVC
     */
    private final BooleanProperty fullMVCSupport = new SimpleBooleanProperty(true);

    public boolean isUseMyBatisPlus() {
        return useMyBatisPlus.get();
    }

    public BooleanProperty useMyBatisPlusProperty() {
        return useMyBatisPlus;
    }

    public void setUseMyBatisPlus(boolean useMyBatisPlus) {
        this.useMyBatisPlus.set(useMyBatisPlus);
    }

    public boolean isOffsetLimit() {
        return offsetLimit.get();
    }

    public BooleanProperty offsetLimitProperty() {
        return offsetLimit;
    }

    public void setOffsetLimit(boolean offsetLimit) {
        this.offsetLimit.set(offsetLimit);
    }

    public boolean isComment() {
        return comment.get();
    }

    public BooleanProperty commentProperty() {
        return comment;
    }

    public void setComment(boolean comment) {
        this.comment.set(comment);
    }

    public boolean isOverrideXML() {
        return overrideXML.get();
    }

    public BooleanProperty overrideXMLProperty() {
        return overrideXML;
    }

    public void setOverrideXML(boolean overrideXML) {
        this.overrideXML.set(overrideXML);
    }

    public boolean isNeedToStringHashcodeEquals() {
        return needToStringHashcodeEquals.get();
    }

    public BooleanProperty needToStringHashcodeEqualsProperty() {
        return needToStringHashcodeEquals;
    }

    public void setNeedToStringHashcodeEquals(boolean needToStringHashcodeEquals) {
        this.needToStringHashcodeEquals.set(needToStringHashcodeEquals);
    }

    public boolean isUseLombokPlugin() {
        return useLombokPlugin.get();
    }

    public BooleanProperty useLombokPluginProperty() {
        return useLombokPlugin;
    }

    public void setUseLombokPlugin(boolean useLombokPlugin) {
        this.useLombokPlugin.set(useLombokPlugin);
    }

    public boolean isNeedForUpdate() {
        return needForUpdate.get();
    }

    public BooleanProperty needForUpdateProperty() {
        return needForUpdate;
    }

    public void setNeedForUpdate(boolean needForUpdate) {
        this.needForUpdate.set(needForUpdate);
    }

    public boolean isAnnotationDAO() {
        return annotationDAO.get();
    }

    public BooleanProperty annotationDAOProperty() {
        return annotationDAO;
    }

    public void setAnnotationDAO(boolean annotationDAO) {
        this.annotationDAO.set(annotationDAO);
    }

    public boolean isAnnotation() {
        return annotation.get();
    }

    public BooleanProperty annotationProperty() {
        return annotation;
    }

    public void setAnnotation(boolean annotation) {
        this.annotation.set(annotation);
    }

    public boolean isUseActualColumnNames() {
        return useActualColumnNames.get();
    }

    public BooleanProperty useActualColumnNamesProperty() {
        return useActualColumnNames;
    }

    public void setUseActualColumnNames(boolean useActualColumnNames) {
        this.useActualColumnNames.set(useActualColumnNames);
    }

    public boolean isUseExample() {
        return useExample.get();
    }

    public BooleanProperty useExampleProperty() {
        return useExample;
    }

    public void setUseExample(boolean useExample) {
        this.useExample.set(useExample);
    }

    public String getGenerateKeys() {
        return generateKeys.get();
    }

    public StringProperty generateKeysProperty() {
        return generateKeys;
    }

    public void setGenerateKeys(String generateKeys) {
        this.generateKeys.set(generateKeys);
    }

    public String getEncoding() {
        return encoding.get();
    }

    public StringProperty encodingProperty() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding.set(encoding);
    }

    public boolean isUseTableNameAlias() {
        return useTableNameAlias.get();
    }

    public BooleanProperty useTableNameAliasProperty() {
        return useTableNameAlias;
    }

    public void setUseTableNameAlias(boolean useTableNameAlias) {
        this.useTableNameAlias.set(useTableNameAlias);
    }

    public boolean isUseDAOExtendStyle() {
        return useDAOExtendStyle.get();
    }

    public BooleanProperty useDAOExtendStyleProperty() {
        return useDAOExtendStyle;
    }

    public void setUseDAOExtendStyle(boolean useDAOExtendStyle) {
        this.useDAOExtendStyle.set(useDAOExtendStyle);
    }

    public boolean isUseSchemaPrefix() {
        return useSchemaPrefix.get();
    }

    public BooleanProperty useSchemaPrefixProperty() {
        return useSchemaPrefix;
    }

    public void setUseSchemaPrefix(boolean useSchemaPrefix) {
        this.useSchemaPrefix.set(useSchemaPrefix);
    }

    public boolean isJsr310Support() {
        return jsr310Support.get();
    }

    public BooleanProperty jsr310SupportProperty() {
        return jsr310Support;
    }

    public void setJsr310Support(boolean jsr310Support) {
        this.jsr310Support.set(jsr310Support);
    }

    public boolean isSwaggerSupport() {
        return swaggerSupport.get();
    }

    public BooleanProperty swaggerSupportProperty() {
        return swaggerSupport;
    }

    public void setSwaggerSupport(boolean swaggerSupport) {
        this.swaggerSupport.set(swaggerSupport);
    }

    public boolean isFullMVCSupport() {
        return fullMVCSupport.get();
    }

    public BooleanProperty fullMVCSupportProperty() {
        return fullMVCSupport;
    }

    public void setFullMVCSupport(boolean fullMVCSupport) {
        this.fullMVCSupport.set(fullMVCSupport);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TableOption{");
        sb.append("useExample=").append(useExample.get());
        sb.append(", useMyBatisPlus=").append(useMyBatisPlus.get());
        sb.append(", offsetLimit=").append(offsetLimit.get());
        sb.append(", comment=").append(comment.get());
        sb.append(", overrideXML=").append(overrideXML.get());
        sb.append(", needToStringHashcodeEquals=").append(needToStringHashcodeEquals.get());
        sb.append(", useLombokPlugin=").append(useLombokPlugin.get());
        sb.append(", needForUpdate=").append(needForUpdate.get());
        sb.append(", annotationDAO=").append(annotationDAO.get());
        sb.append(", annotation=").append(annotation.get());
        sb.append(", useActualColumnNames=").append(useActualColumnNames.get());
        sb.append(", generateKeys=").append(generateKeys.get());
        sb.append(", encoding=").append(encoding.get());
        sb.append(", useTableNameAlias=").append(useTableNameAlias.get());
        sb.append(", useDAOExtendStyle=").append(useDAOExtendStyle.get());
        sb.append(", useSchemaPrefix=").append(useSchemaPrefix.get());
        sb.append(", jsr310Support=").append(jsr310Support.get());
        sb.append(", swaggerSupport=").append(swaggerSupport.get());
        sb.append(", fullMVCSupport=").append(fullMVCSupport.get());
        sb.append('}');
        return sb.toString();
    }
}
