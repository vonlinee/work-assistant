package org.example.workassistant.fxui.controller.domain;

import org.example.workassistant.fxui.model.JavaType;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;

import java.sql.JDBCType;

/**
 * 类型映射表
 * JDBC type包含了所有数据库的类型，而SQLType仅包含了某一种实现，该实现可能是JDBC实现，也可能是某个数据库的实现
 */
public class TypeMappingTable extends TableView<TypeMapping> {

    public TypeMappingTable() {
        setEditable(true);

        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn<TypeMapping, String> tblcJavaTypes = new TableColumn<>("Standard Java Type");
        tblcJavaTypes.setCellValueFactory(param -> {
            TypeMapping value = param.getValue();
            JavaType javaDataType = value.getJavaDataType();
            return new SimpleStringProperty(javaDataType == null ? "" : javaDataType.getQualifier());
        });
        // JDBC Type列
        TableColumn<TypeMapping, JDBCType> tblcSQLTypes = new TableColumn<>("JDBC Type");
        tblcSQLTypes.setCellValueFactory(param -> {
            TypeMapping value = param.getValue();
            JDBCType sqlType = (JDBCType) value.getSqlType();
            return new SimpleObjectProperty<>(sqlType);
        });
        tblcSQLTypes.setCellFactory(param -> {
            ComboBoxTableCell<TypeMapping, JDBCType> cell = new ComboBoxTableCell<>();
            cell.setEditable(true);
            cell.getItems().setAll(JDBCType.values());
            return cell;
        });
        tblcSQLTypes.setEditable(true);
        // SQL数据类型默认长度列
        TableColumn<TypeMapping, String> tlbcLength = new TableColumn<>("默认长度");
        tlbcLength.setEditable(true);
        tlbcLength.setCellValueFactory(new PropertyValueFactory<>("sqlTypeLength"));
        tlbcLength.setCellFactory(TextFieldTableCell.forTableColumn());

        getColumns().add(tblcJavaTypes);
        getColumns().add(tblcSQLTypes);
        getColumns().add(tlbcLength);
        setItems(FXCollections.observableArrayList(TypeMapping.mapppings));
    }
}
