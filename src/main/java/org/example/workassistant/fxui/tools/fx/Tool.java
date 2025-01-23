package org.example.workassistant.fxui.tools.fx;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.example.workassistant.fxui.tools.maven.AnalyzePomDependencies;
import org.example.workassistant.fxui.tools.maven.LocalJarDependency;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.List;

public class Tool extends BorderPane {

    TextField mavenHomeTextField = new TextField();
    TextField javaHomeTextField = new TextField();
    TextField localMavenRepositoryTextField = new TextField();
    TextField projectDir = new TextField();

    ListView<LocalJarDependency> list;

    public Tool() {

        setTop(initTop());

        setCenter(initCenter());


        init();
    }

    private VBox initTop() {
        VBox vBox = new VBox();

        // 创建 GridPane
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10)); // 设置内边距
        grid.setVgap(8); // 设置垂直间距
        grid.setHgap(10); // 设置水平间距

        // 将控件添加到 GridPane
        grid.add(new Label("JDK安装位置"), 0, 0); // (控件, 列, 行)
        grid.add(javaHomeTextField, 1, 0);
        grid.add(new Label("Maven安装位置"), 0, 1);
        grid.add(mavenHomeTextField, 1, 1);
        grid.add(new Label("本地仓库地址"), 0, 2);
        grid.add(localMavenRepositoryTextField, 1, 2);

        // 创建列约束
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPrefWidth(100); // 第 1 列固定宽度
        grid.getColumnConstraints().addAll(column1);

        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(100); // 第 2 列占满剩余空间
        grid.getColumnConstraints().addAll(column2);
        GridPane.setMargin(localMavenRepositoryTextField, new Insets(0, 10, 0, 0)); // 在右侧添加 10 的边距

        grid.prefWidthProperty().bind(vBox.widthProperty());

        vBox.getChildren().addAll(grid);

        setTop(vBox);

        ButtonBar buttonBar = new ButtonBar();

        Button parse = new Button("解析依赖项");

        parse.setOnAction(e -> {
            List<LocalJarDependency> dependencies = AnalyzePomDependencies.parseDependencies(new File(projectDir.getText()));

            list.getItems().addAll(dependencies);
        });

        buttonBar.getButtons().add(parse);
        Button btn = new Button("检测环境");
        buttonBar.getButtons().add(btn);
        btn.setOnAction(e -> init());
        vBox.getChildren().add(buttonBar);

        GridPane grid1 = new GridPane();
        grid1.setPadding(new Insets(10, 10, 10, 10)); // 设置内边距
        grid1.setVgap(8); // 设置垂直间距
        grid1.setHgap(10); // 设置水平间距
        grid1.add(new Label("项目地址"), 0, 0); // (控件, 列, 行)
        grid1.add(projectDir, 1, 0); // (控件, 列, 行)

        vBox.getChildren().add(grid1);
        ColumnConstraints column11 = new ColumnConstraints();
        column11.setPrefWidth(100); // 第 1 列固定宽度
        grid1.getColumnConstraints().addAll(column11);

        ColumnConstraints column22 = new ColumnConstraints();
        column22.setPercentWidth(100); // 第 2 列占满剩余空间
        grid1.getColumnConstraints().addAll(column22);
        GridPane.setMargin(projectDir, new Insets(0, 10, 0, 0)); // 在右侧添加 10 的边距

        projectDir.setText("D:\\Develop\\Code\\work-assistant");

        return vBox;
    }

    private BorderPane initCenter() {
        BorderPane borderPane = new BorderPane();

        list = new ListView<>();
        list.setItems(FXCollections.observableArrayList());
        list.setCellFactory(dependencyListView -> {

            TextFieldListCell<LocalJarDependency> cell = new TextFieldListCell<>();

            cell.setConverter(new StringConverter<>() {
                @Override
                public String toString(LocalJarDependency localJarDependency) {
                    return localJarDependency.toString();
                }

                @Override
                public LocalJarDependency fromString(String s) {
                    return new LocalJarDependency(s);
                }
            });

            return cell;
        });

        borderPane.setCenter(list);

        return borderPane;
    }

    public void init() {
        mavenHomeTextField.setText(System.getenv("MAVEN_HOME"));
        javaHomeTextField.setText(System.getenv("JAVA_HOME"));
        String mavenHome = mavenHomeTextField.getText();
        // 查找settings.xml文件 本地仓库地址
        localMavenRepositoryTextField.setText(getLocalRepositoryDirectory(new File(mavenHome, "conf/settings.xml")));
    }

    private String getLocalRepositoryDirectory(File settingsFile) {
        try {
            // 创建 XML 解析器
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(settingsFile);

            // 获取所有仓库节点
            NodeList localRepositories = document.getElementsByTagName("localRepository");
            Element localRepository = (Element) localRepositories.item(0);
            return localRepository.getTextContent();
        } catch (Exception ignore) {
        }
        return null;
    }
}
