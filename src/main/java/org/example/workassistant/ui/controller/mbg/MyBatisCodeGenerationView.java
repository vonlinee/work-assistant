package org.example.workassistant.ui.controller.mbg;

import org.example.workassistant.common.interfaces.impl.TableMetadata;
import org.example.workassistant.common.utils.MavenProjectAnalyser;
import org.example.workassistant.common.utils.ProjectAnalyser;
import org.example.workassistant.ui.bridge.MyBatisPlusGenerator;
import org.example.workassistant.ui.bridge.ProjectConfiguration;
import org.example.workassistant.ui.controller.TableCustomizationView;
import org.example.workassistant.ui.model.*;
import org.example.workassistant.utils.*;
import org.example.workassistant.utils.util.StringUtils;
import io.fxtras.Alerts;
import io.fxtras.scene.mvvm.FxmlBinder;
import io.fxtras.scene.mvvm.FxmlView;
import io.fxtras.scene.mvvm.View;
import io.fxtras.utils.EventUtils;
import io.fxtras.utils.PropertyBinder;
import io.fxtras.utils.StageManager;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.text.TextAlignment;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MyBatis Code Generation
 */
@FxmlBinder(location = "layout/mbg/MyBatisCodeGenerationView.fxml", label = "MyBatis代码生成")
public class MyBatisCodeGenerationView extends FxmlView {

    /**
     * 项目配置项
     */
    private final ProjectConfiguration projectConfig = ProjectConfiguration.builder().build();
    /**
     * 保存哪些表需要进行代码生成
     * 存放的key:TableCodeGenConfig#getUniqueKey()
     *
     * @see TableGeneration#getUniqueKey()
     */
    private final Map<String, TableGeneration> tableConfigsToBeGenerated = new ConcurrentHashMap<>(10);
    @FXML
    public TableView<TableGeneration> tblvTableCustomization;
    @FXML
    public TableColumn<TableGeneration, String> tblcTableName;
    @FXML
    public TableColumn<TableGeneration, String> tblcTableComment;
    @FXML
    public ComboBox<String> cboxConnection;
    @FXML
    public ComboBox<String> cboxDatabase;
    @FXML
    public TableView<TableGeneration> tblvTableSelected; // 选中的表
    @FXML
    public TableColumn<TableGeneration, String> tblcSelectedTableName;
    @FXML
    public TableColumn<TableGeneration, String> tblcSelectedTableComment;
    @FXML
    public TextField txfParentPackageName;
    @FXML
    public TextField modelTargetPackage;
    @FXML
    public TextField txfXmlMapperPackageName; // Xml Mapper文件包名
    @FXML
    public TextField txfMapperPackageName;  // DAO接口包名
    @FXML
    public TextField projectFolderField;
    @FXML
    public Button btnSaveConfig;
    @FXML
    public Button btnLoadConfig;
    @FXML
    public ChoiceBox<String> chobProjectLayout;
    @FXML
    public CheckBox chbUseExample;
    @FXML
    public CheckBox chbOffsetLimit;
    @FXML
    public CheckBox chbComment;
    @FXML
    public CheckBox chbOverrideXML;
    @FXML
    public CheckBox chbUseLombokPlugin;
    @FXML
    public CheckBox chbNeedToStringHashcodeEquals;
    @FXML
    public CheckBox chbUseSchemaPrefix;
    @FXML
    public CheckBox chbForUpdate;
    @FXML
    public CheckBox chbAnnotationDao;
    @FXML
    public CheckBox chbJsr310Support;
    @FXML
    public CheckBox useActualColumnNamesCheckbox;
    @FXML
    public CheckBox annotationCheckBox;
    @FXML
    public CheckBox chbMapperExtend;
    @FXML
    public CheckBox useTableNameAliasCheckbox;
    @FXML
    public CheckBox addMapperAnnotationCheckBox;
    @FXML
    public CheckBox chbEnableSwagger;
    TextInputDialog configSaveDialog;
    CodeGenerationResultDialog dialog = new CodeGenerationResultDialog();
    MyBatisPlusGenerator generator = new MyBatisPlusGenerator();
    ProjectAnalyser analyser = new MavenProjectAnalyser();

    static String findMinLengthString(Set<String> stringSet) {
        if (stringSet == null || stringSet.isEmpty()) {
            return null; // 或者抛出一个异常，取决于你的需求
        }
        String minLengthString = null;
        int minLength = Integer.MAX_VALUE;
        for (String str : stringSet) {
            int length = str.length();
            if (length < minLength) {
                minLength = length;
                minLengthString = str;
            }
        }
        return minLengthString;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        btnLoadConfig.setOnAction(event -> StageManager.show(ProjectConfigurationView.class));
        cboxConnection.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (StringUtils.hasText(newValue)) {
                cboxDatabase.getItems().clear();
                ConnectionConfig cf = ConnectionRegistry.get(newValue);
                try (Connection connection = cf.getConnection()) {
                    cboxDatabase.getItems().addAll(DBUtils.getDatabaseNames(connection));
                } catch (SQLException e) {
                    log.error("连接失败{} ", newValue);
                }
                cboxDatabase.getSelectionModel().selectFirst();
            }
        });

        cboxDatabase.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            String connectionNameSelected = cboxConnection.getSelectionModel().getSelectedItem();
            if (connectionNameSelected == null) {
                return;
            }
            refreshTables(connectionNameSelected, newValue);
        });
        // 连接列表
        cboxConnection.setOnMouseClicked(event -> Platform.runLater(() -> cboxConnection.getItems()
            .setAll(AppConfig.listConnectionInfo().stream().map(ConnectionConfig::getUniqueKey).toList())));

        tblcTableName.setCellValueFactory(new PropertyValueFactory<>("tableName"));
        tblcTableName.setCellFactory(param -> {
            TableCell<TableGeneration, String> cell = new TextFieldTableCell<>();
            cell.setAlignment(Pos.CENTER);
            cell.setTextAlignment(TextAlignment.CENTER);
            return cell;
        });
        tblcSelectedTableName.setCellValueFactory(new PropertyValueFactory<>("tableName"));
        tblcSelectedTableName.setCellFactory(param -> {
            TableCell<TableGeneration, String> cell = new TextFieldTableCell<>();
            cell.setAlignment(Pos.CENTER);
            cell.setTextAlignment(TextAlignment.CENTER);
            return cell;
        });

        tblvTableCustomization.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblvTableSelected.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tblvTableSelected.setRowFactory(param -> {
            TableRow<TableGeneration> row = new TableRow<>();
            MenuItem deleteThisRowMenuItem = new MenuItem("删除");
            MenuItem customizeMenuItem = new MenuItem("定制");
            deleteThisRowMenuItem.setOnAction(event -> {
                TableGeneration item = param.getSelectionModel().getSelectedItem();
                param.getItems().remove(item);
                tableConfigsToBeGenerated.remove(item.getUniqueKey()); // 移除该表
            });
            customizeMenuItem.setOnAction(event -> {
                // 表定制事件
                TableGeneration item = param.getSelectionModel().getSelectedItem();
                Parent root = View.load(TableCustomizationView.class);
                this.publish("CustomizeTable", item);
                StageManager.show("表生成定制", root);
                event.consume();
            });
            ContextMenu menu = new ContextMenu(deleteThisRowMenuItem, customizeMenuItem);
            row.setContextMenu(menu);
            return row;
        });

        tblvTableCustomization.setRowFactory(param -> {
            TableRow<TableGeneration> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (EventUtils.isPrimaryButtonDoubleClicked(event)) {
                    TableGeneration tableItem = row.getItem();
                    if (tableItem != null) {
                        String uniqueKey = tableItem.getUniqueKey();
                        if (!tableConfigsToBeGenerated.containsKey(uniqueKey)) {
                            tableConfigsToBeGenerated.put(uniqueKey, tableItem);
                            tblvTableSelected.getItems().add(tableItem);
                        }
                    }
                }
            });
            return row;
        });

        PropertyBinder.bind(projectFolderField.textProperty(), projectConfig, ProjectConfiguration::setProjectRootFolder);
        PropertyBinder.bind(modelTargetPackage.textProperty(), projectConfig, ProjectConfiguration::setEntityPackageName);
        PropertyBinder.bind(txfParentPackageName.textProperty(), projectConfig, ProjectConfiguration::setParentPackage);
        PropertyBinder.bind(txfMapperPackageName.textProperty(), projectConfig, ProjectConfiguration::setMapperPackageName);
        PropertyBinder.bind(txfXmlMapperPackageName.textProperty(), projectConfig, ProjectConfiguration::setMapperXmlPackage);

        chobProjectLayout.getItems().addAll("MAVEN");
        chobProjectLayout.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {

        });
        chobProjectLayout.getSelectionModel().selectFirst();
    }

    /**
     * 刷新表信息
     *
     * @param connectionName 连接名称
     */
    private void refreshTables(String connectionName, String dbName) {
        ConnectionConfig cf = ConnectionRegistry.get(connectionName);
        final ObservableList<TableGeneration> items = tblvTableCustomization.getItems();
        if (!items.isEmpty()) items.clear();
        try (Connection connection = cf.getConnection(dbName)) {
            List<TableMetadata> tablesMetadata = DBUtils.getTablesMetadata(connection);
            for (TableMetadata tablesMetadatum : tablesMetadata) {
                TableGeneration tcgf = new TableGeneration();
                tcgf.setConnectionName(connectionName);
                tcgf.setDatabaseName(dbName);
                tcgf.setTableName(tablesMetadatum.getTableName());
                items.add(tcgf);
            }
        } catch (Exception exception) {
            Alerts.exception("刷新表信息失败", exception).showAndWait();
        }
    }

    @FXML
    public void generateCode(ActionEvent actionEvent) {
        if (tableConfigsToBeGenerated.isEmpty()) {
            Alerts.error("选择的数据表为空").show();
            return;
        }
        String errMsg = validateConfig(this.projectConfig);
        if (StringUtils.hasText(errMsg)) {
            Alerts.error(errMsg).show();
            return;
        }
        CodeGenContext context = new CodeGenContext();
        context.setProjectConfiguration(this.projectConfig);
        context.setTargetedTables(new HashMap<>(tableConfigsToBeGenerated));
        generate(context);
    }

    /**
     * 代码生成
     * 使用mybatis-plus-generator
     *
     * @param context 参数
     */
    private void generate(CodeGenContext context) {
        try {
            // generator.generate(context);
            dialog.showDirectory(new File(context.getProjectConfiguration().getProjectRootFolder()));
        } catch (Exception exception) {
            Alerts.exception("生成失败", exception).showAndWait();
        }
    }

    /**
     * 检查并创建不存在的文件夹
     *
     * @return 是否创建成功
     */
    private boolean checkDirs(ProjectConfiguration config) {
        List<Path> targetDirs = new ArrayList<>();
        targetDirs.add(Path.of(config.getProjectRootFolder()));
        // targetDirs.add(config.getEntityTargetDirectory());
        // targetDirs.add(config.getMappingXMLTargetDirectory());
        // targetDirs.add(config.getMapperTargetDirectory());
        StringBuilder sb = new StringBuilder();
        for (Path dir : targetDirs) {
            if (!Files.exists(dir)) {
                sb.append(dir).append("\n");
            }
        }
        if (!sb.isEmpty()) {
            Alerts.confirm("以下目录不存在, 是否创建?\n" + sb).showAndWait().ifPresent(buttonType -> {
                if (ButtonType.OK == buttonType) {
                    try {
                        FileUtils.forceMkdir(targetDirs);
                    } catch (Exception e) {
                        Alerts.error(Messages.getString("PromptText.3")).show();
                    }
                }
            });
        }
        return true;
    }

    /**
     * 添加一个表到需要进行代码生成的表
     *
     * @param tableInfo 表信息
     */
    public void addTable(TableGeneration tableInfo) {
        String key = tableInfo.getUniqueKey();
        if (tableConfigsToBeGenerated.containsKey(key)) {
            return;
        }
        tableConfigsToBeGenerated.put(key, tableInfo);
        tblvTableCustomization.getItems().add(tableInfo);
    }

    /**
     * 校验配置项
     *
     * @param projectConfig 项目配置
     */
    private String validateConfig(ProjectConfiguration projectConfig) {
        return Validator.target(projectConfig)
            .hasText(ProjectConfiguration::getProjectRootFolder, "项目根目录为空")
            .hasText(ProjectConfiguration::getMapperPackageName, "Mapper接口包名为空")
            .hasText(ProjectConfiguration::getEntityPackageName, "实体类包名为空")
            .hasText(ProjectConfiguration::getEntityPackageFolder, "实体类所在目录为空")
            .hasText(ProjectConfiguration::getMapperXmlPackage, "映射XML文件包名为空")
            .getErrorMessages();
    }

    /**
     * 选择项目文件夹
     *
     * @param event 事件
     */
    @FXML
    public void chooseProjectFolder(ActionEvent event) {
        FileChooserDialog.showDirectoryDialog(getStage(event))
            .ifPresent(file -> {
                if (!analyser.isProjectRootDirectory(file)) {
                    Alerts.error("目录%s不是项目根目录", file.getAbsolutePath()).showAndWait();
                    return;
                }
                try {
                    Set<String> packageNames = new HashSet<>(analyser.getPackageNames(file));
                    String parentPackage = findMinLengthString(packageNames);
                    if (parentPackage == null || parentPackage.isBlank()) {
                        parentPackage = "org.example";
                    }
                    txfParentPackageName.setText(parentPackage);
                    modelTargetPackage.setText(parentPackage + ".entity");
                    txfXmlMapperPackageName.setText(parentPackage + ".mapper");
                    txfMapperPackageName.setText(parentPackage + ".mapper");
                    projectFolderField.setText(file.getAbsolutePath());
                } catch (Exception exception) {
                    Alerts.error(exception.getMessage()).showAndWait();
                }
            });
    }

    /**
     * 保存配置
     *
     * @param actionEvent 事件
     */
    @FXML
    public void saveCodeGenConfig(ActionEvent actionEvent) {
        String errMsg = validateConfig(this.projectConfig);
        if (StringUtils.hasText(errMsg)) {
            Alerts.error(errMsg).show();
            return;
        }
        if (configSaveDialog == null) {
            configSaveDialog = new TextInputDialog();
            configSaveDialog.setTitle("保存代码生成配置");
            configSaveDialog.setContentText("输入配置名称:");
        }
        Optional<String> resultOptional = configSaveDialog.showAndWait();
        if (resultOptional.isPresent()) {
            this.projectConfig.setName(resultOptional.get());
        } else {
            Alerts.error("配置名称不能为空");
        }
        AppConfig.saveProjectConfiguration(this.projectConfig);
    }

    /**
     * 加载配置信息，填充到界面上
     *
     * @param projectConfig 配置信息
     */
    public void loadConfig(ProjectConfiguration projectConfig) {
        projectFolderField.setText(projectConfig.getProjectRootFolder());
        modelTargetPackage.setText(projectConfig.getEntityPackageName());
        txfParentPackageName.setText(projectConfig.getParentPackage());
        txfMapperPackageName.setText(projectConfig.getMapperPackageName());
        txfXmlMapperPackageName.setText(projectConfig.getMapperXmlPackage());
    }
}
