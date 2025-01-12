package io.devpl.fxui.controller;

import io.devpl.fxui.common.Constants;
import io.devpl.fxui.model.props.ColumnCustomConfiguration;
import io.devpl.sdk.util.CollectionUtils;
import io.devpl.sdk.util.StringUtils;
import io.devpl.fxui.fxtras.Alerts;
import io.devpl.fxui.fxtras.mvvm.FxmlBinder;
import io.devpl.fxui.fxtras.mvvm.FxmlView;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.mybatis.generator.internal.util.JavaBeansUtil;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 定制列配置控制器
 */
@FxmlBinder(location = "layout/tableColumnConfigs.fxml")
public class TableColumnConfigView extends FxmlView {

    @FXML
    private Label currentTableNameLabel;
    @FXML
    private TextField columnNamePrefixTextLabel;

    private TableView<ColumnCustomConfiguration> columnListView;
    private String tableName;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // do nothing
    }

    @FXML
    public void cancel(ActionEvent event) {
        getStage(event).close();
    }

    @FXML
    public void confirm(ActionEvent event) {
        try {
            // 1. generator bean property name
            genPropertyNameByColumnNamePrefix();
            getStage(event).close(); // close window
        } catch (Exception e) {
            log.error("confirm throw exception.", e);
            Alerts.error(e.getMessage()).showAndWait();
        }
    }

    public void setColumnListView(TableView<ColumnCustomConfiguration> columnListView) {
        this.columnListView = columnListView;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
        currentTableNameLabel.setText(tableName);
    }

    private void genPropertyNameByColumnNamePrefix() {
        String columnNamePrefix = this.columnNamePrefixTextLabel.getText();
        if (StringUtils.hasText(columnNamePrefix)) {
            if (StringUtils.endsWith(columnNamePrefix.trim(), Constants.OR_REGEX)) {
                columnNamePrefix = StringUtils.removeEnd(columnNamePrefix.trim(), Constants.OR_REGEX);
            }
            String regex = String.format(Constants.COLUMN_PREFIX_PATTERN, columnNamePrefix);
            log.info("table:{}, column_name_prefix:{}, regex:{}", this.tableName, columnNamePrefix, regex);
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

            ObservableList<ColumnCustomConfiguration> items = columnListView.getItems();
            if (CollectionUtils.isEmpty(items)) {
                return;
            }
            items.forEach(item -> {
                String columnName = item.getColumnName();
                Matcher matcher = pattern.matcher(columnName);
                if (matcher.find()) {
                    // use first match result
                    String regexColumnName = matcher.group();
                    if (StringUtils.hasText(regexColumnName)) {
                        String propertyName = JavaBeansUtil.getCamelCaseString(regexColumnName, false);
                        log.debug("table:{} column_name:{} regex_column_name:{} property_name:{}", tableName, columnName, regexColumnName, propertyName);

                        if (StringUtils.hasText(propertyName)) item.setPropertyName(propertyName);
                    } else {
                        log.warn("table:{} column_name:{} regex_column_name is blank", tableName, columnName);
                    }
                } else {
                    // if not match, set property name is null
                    item.setPropertyName(null);
                }
            });
        }
    }
}
