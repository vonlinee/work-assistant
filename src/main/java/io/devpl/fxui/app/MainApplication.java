package io.devpl.fxui.app;

import io.devpl.fxui.components.RouterView;
import io.devpl.fxui.controller.dbconn.ConnManageView;
import io.devpl.fxui.controller.domain.ClassDefView;
import io.devpl.fxui.controller.domain.TypeMappingTable;
import io.devpl.fxui.controller.expression.ExpressionEngineView;
import io.devpl.fxui.controller.fields.FieldsManageView;
import io.devpl.fxui.controller.mbg.MyBatisCodeGenerationView;
import io.devpl.fxui.controller.template.TemplateManageView;
import io.devpl.fxui.fxtras.mvvm.View;
import io.devpl.fxui.layout.LayoutPane;
import io.devpl.fxui.layout.menu.NavigationMenu;
import io.devpl.fxui.tools.mybatis.MyBatisXmlToolPane;
import io.devpl.fxui.utils.FXUtils;
import io.devpl.fxui.view.DataTypeInfoTableView;
import io.devpl.fxui.view.FileTreeView;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class MainApplication extends Application {

    Stage stage = new Stage();

    @Override
    public void start(Stage stage) throws IOException {
        BorderPane root = new BorderPane();

        LayoutPane layoutPane = new LayoutPane();

        NavigationMenu menu1 = new NavigationMenu("连接信息", View.load(ConnManageView.class));
        NavigationMenu menu = new NavigationMenu("开发工具", null);
        menu.addChild("代码生成", RouterView.of(View.load(MyBatisCodeGenerationView.class)));
        menu.addChild("MyBatis工具", RouterView.of(new MyBatisXmlToolPane()));
        menu.addChild("领域模型", RouterView.of(View.load(ClassDefView.class)));
        menu.addChild("类型映射", RouterView.of(new TypeMappingTable()));
        menu.addChild("数据类型", RouterView.of(new DataTypeInfoTableView()));
        menu.addChild("字段管理", RouterView.of(View.load(FieldsManageView.class)));
        menu.addChild("模板管理", RouterView.of(View.load(TemplateManageView.class)));
        menu.addChild("表达式引擎", RouterView.of(View.load(ExpressionEngineView.class)));
        menu.addChild("Maven", RouterView.of(new MavenTool()));
        menu.addChild("JVM工具", RouterView.of(new JvmTool()));

        layoutPane.addNavigationMenu(menu1, menu);

        root.setTop(topBar());
        root.setCenter(layoutPane);

        Rectangle2D box = FXUtils.getScreenBox();

        layoutPane.expandAllMenu();

        Scene scene = new Scene(root, box.getWidth() * 0.75, box.getHeight() * 0.8);
        stage.setTitle("Devpl");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * 顶部菜单栏
     *
     * @return MenuBar
     */
    public MenuBar topBar() {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem menuItem_open = new MenuItem("Open");

        FileTreeView fileTreeView = new FileTreeView(new File("D:/Temp"));
        menuItem_open.setOnAction(event -> {
            if (stage.getScene() == null) {
                stage.setScene(new Scene(fileTreeView));
            }
            stage.show();
        });

        MenuItem menuItem_import = new MenuItem("Import");
        fileMenu.getItems().addAll(menuItem_open, menuItem_import);

        Menu helpMenu = new Menu("Help");
        MenuItem menuItem_about = new MenuItem("关于");
        helpMenu.getItems().addAll(menuItem_about);

        menuBar.getMenus().addAll(fileMenu, helpMenu);
        return menuBar;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
