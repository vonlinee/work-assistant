package io.devpl.fxui.view;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.model.util.BindingMode;
import com.dlsc.formsfx.model.validators.CustomValidator;
import io.devpl.fxui.components.table.TableOperation;
import io.devpl.fxui.components.table.TablePane;
import io.devpl.fxui.components.table.TablePaneOption;
import io.devpl.fxui.mapper.DataTypeItemMapper;
import io.devpl.fxui.mapper.MyBatis;
import io.devpl.fxui.utils.FXUtils;
import io.devpl.fxui.utils.Helper;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.scene.layout.BorderPane;

import java.util.List;

public class DataTypeInfoTableView extends BorderPane {

    DataTypeItemMapper dataTypeItemMapper = MyBatis.getMapper(DataTypeItemMapper.class);

    public DataTypeInfoTableView() {
        setCenter(FXUtils.createTableView(DataTypeItem.class));

        TablePaneOption option = TablePaneOption
            .model(DataTypeItem.class)
            .enablePagination(true)
            .enableToolbar(true)
            .form(new DataTypeModel(), formObject -> {
                Form loginForm = Form.of(
                    Group.of(
                        Field.ofSingleSelectionType(new SimpleListProperty<>(FXCollections.observableArrayList(List.of("JSON", "Java", "JDBC"))), formObject.typeGroupProperty())
                            .label("类型分组")
                            .placeholder("")
                            .select(0),
                        Field.ofStringType(formObject.typeKeyProperty())
                            .placeholder("typeKey")
                            .label("类型Key"),
                        Field.ofStringType(formObject.typeNameProperty())
                            .placeholder("Unknown")
                            .label("类型名称"),
                        Field.ofIntegerType(formObject.minLengthProperty())
                            .placeholder("-1")
                            .validate(CustomValidator.forPredicate(val -> val >= -1, ""))
                            .label("最小长度"),
                        Field.ofIntegerType(formObject.maxLengthProperty())
                            .placeholder("-1")
                            .validate(CustomValidator.forPredicate(val -> val >= -1, ""))
                            .label("最大长度"),
                        Field.ofStringType(formObject.defaultValueProperty())
                            .placeholder("-1")
                            .label("默认值"),
                        Field.ofStringType(formObject.descriptionProperty())
                            .placeholder("")
                            .label("描述信息")
                    )
                ).title("新增类型");
                loginForm.binding(BindingMode.CONTINUOUS);
                return loginForm;
            });

        TablePane<DataTypeItem> table = new TablePane<>(DataTypeItem.class, option);

        table.setTableOperation(new TableOperation<DataTypeModel, DataTypeItem>() {
            @Override
            public List<DataTypeItem> loadPageData(int pageNum, int pageSize) {
                return dataTypeItemMapper.selectPage(pageNum, pageSize);
            }

            @Override
            public DataTypeItem extractForm(DataTypeModel oldForm, DataTypeItem row) {
                if (row == null) {
                    row = new DataTypeItem();
                }
                row.setTypeKey(oldForm.getTypeKey());
                row.setTypeName(oldForm.getTypeName());
                row.setTypeGroupId(oldForm.getTypeGroup());
                row.setMinLength(oldForm.getMinLength());
                row.setMaxLength(oldForm.getMaxLength());
                row.setDefaultValue(oldForm.getDefaultValue());
                row.setDescription(oldForm.getDescription());
                return row;
            }

            @Override
            public void fillForm(int rowIndex, DataTypeItem row, DataTypeModel formObject) {
                if (row != null) {
                    formObject.setTypeGroup(row.getTypeGroupId());
                    formObject.setTypeName(row.getTypeName());
                    formObject.setTypeKey(row.getTypeKey());
                    formObject.setMinLength(Helper.defaults(row.getMinLength(), -1));
                    formObject.setMaxLength(Helper.defaults(row.getMaxLength(), -1));
                    formObject.setDefaultValue(row.getDefaultValue());
                } else {
                    formObject.setTypeGroup("Java");
                    formObject.setTypeName("typeName");
                    formObject.setTypeKey("typeKey");
                    formObject.setDescription("");
                }
            }

            @Override
            public void resetForm(DataTypeModel formObject) {
                formObject.setTypeName("");
                formObject.setDescription("");
                formObject.setTypeGroup("");
                formObject.setTypeKey("");
                formObject.setMinLength(-1);
                formObject.setMaxLength(-1);
                formObject.setDefaultValue("");
            }

            @Override
            public void save(DataTypeItem record) {
                int res = dataTypeItemMapper.insert(record);
                if (res > 0) {
                    record.setId((long) res);
                }
            }

            @Override
            public void update(DataTypeItem record) {
                if (record.getId() == null) {
                    return;
                }
                dataTypeItemMapper.updateById(record);
            }

            @Override
            public void delete(DataTypeItem record) {
                dataTypeItemMapper.deleteById(record.getId());
            }
        });
        setCenter(table);
    }
}
