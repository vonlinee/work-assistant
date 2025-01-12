package io.devpl.fxui.components.table;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.StringConverter;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 初始化表的列
 * @param <R>
 */
public class TableColumnInitializer<R> {

    public <C> List<TableColumn<R, C>> initColumns(Class<R> rowClass, boolean order) {
        Field[] declaredFields = rowClass.getDeclaredFields();
        List<TableColumn<R, C>> columnsToBeAdd = new ArrayList<>();
        Map<String, Integer> tableColumnOrderMap = order ? new HashMap<>() : null;
        for (Field declaredField : declaredFields) {
            TableViewColumn tvc = declaredField.getAnnotation(TableViewColumn.class);
            if (tvc == null) {
                continue;
            }
            Class<?> type = declaredField.getType();
            String propertyName = declaredField.getName();
            // 根据数据类型推断选择使用什么列
            TableColumn<R, C> column = new TableColumn<>(propertyName);
            column.setEditable(tvc.editable());
            column.setId(rowClass.getName() + "." + propertyName);

            if (tableColumnOrderMap != null) {
                tableColumnOrderMap.put(column.getId(), tvc.order());
            }
            // 只支持单层对象
            column.setCellValueFactory(getCellValueFactory(propertyName));
            column.setCellFactory(getCellFactory(rowClass, type, propertyName));
            column.setText(tvc.title());

            columnsToBeAdd.add(column);
        }
        if (order) {
            columnsToBeAdd.sort(Comparator.comparingInt(o -> tableColumnOrderMap.get(o.getId())));
        }
        return columnsToBeAdd;
    }

    /**
     * 只适合单层对象，不适合嵌套对象
     * @param propertyName 属性名
     * @param <T>          列数据类型
     * @return 单元格工厂
     */
    public <T> Callback<TableColumn.CellDataFeatures<R, T>, ObservableValue<T>> getCellValueFactory(String propertyName) {
        return new PropertyValueFactory<>(propertyName);
    }

    /**
     * 针对数据类的字段定制StringConverter
     * @param rowClass     行数据类
     * @param propertyType 属性字段类型
     * @param propertyName 属性名称
     * @param <C>          列数据类型
     * @return StringConverter实例
     */
    public <C> StringConverter<C> getStringConverter(Class<R> rowClass, Class<?> propertyType, String propertyName) {
        return new StringConverter<>() {
            @Override
            public String toString(C object) {
                return String.valueOf(object);
            }

            @Override
            public C fromString(String string) {
                return null;
            }
        };
    }

    /**
     * 针对数据类的字段定制单元格工厂
     * @param rowClass     行数据类
     * @param propertyType 属性字段类型
     * @param propertyName 属性名称
     * @param <T>          列数据类型
     * @return StringConverter实例
     */
    public <T> Callback<TableColumn<R, T>, TableCell<R, T>> getCellFactory(Class<R> rowClass, Class<?> propertyType, String propertyName) {
        return TextFieldTableCell.forTableColumn(getStringConverter(rowClass, propertyType, propertyName));
    }
}
