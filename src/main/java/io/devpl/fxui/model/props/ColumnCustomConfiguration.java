package io.devpl.fxui.model.props;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * 列自定义配置
 */
public class ColumnCustomConfiguration {

	private final BooleanProperty checked = new SimpleBooleanProperty(true);
	// 列名称
	private final StringProperty columnName = new SimpleStringProperty();
	// Java类型
	private final StringProperty javaType = new SimpleStringProperty();
	private final StringProperty jdbcType = new SimpleStringProperty();
	private final StringProperty propertyName = new SimpleStringProperty();
	private final StringProperty typeHandler = new SimpleStringProperty();

	public String getColumnName() {
		return columnName.get();
	}

	public void setColumnName(String columnName) {
		this.columnName.set(columnName);
	}

	public String getJdbcType() {
		return jdbcType.get();
	}

	public void setJdbcType(String jdbcType) {
		this.jdbcType.set(jdbcType);
	}

	public String getPropertyName() {
		return propertyName.get();
	}

	public void setPropertyName(String propertyName) {
		this.propertyName.set(propertyName);
	}

	public BooleanProperty checkedProperty() {
		return checked;
	}

	public boolean isChecked() {
		return this.checked.get();
	}

	public void setChecked(boolean checked) {
		this.checked.set(checked);
	}

	public StringProperty typeHandleProperty() {
		return typeHandler;
	}

	public String getTypeHandler() {
		return typeHandler.get();
	}

	public void setTypeHandler(String typeHandler) {
		this.typeHandler.set(typeHandler);
	}

	public StringProperty columnNameProperty() {
		return columnName;
	}

	public StringProperty jdbcTypeProperty() {
		return jdbcType;
	}

	public StringProperty propertyNameProperty() {
		return propertyName;
	}

	public String getJavaType() {
		return javaType.get();
	}

	public StringProperty javaTypeProperty() {
		return javaType;
	}

	public void setJavaType(String javaType) {
		this.javaType.set(javaType);
	}
}
