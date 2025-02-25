package org.example.workassistant;

import io.fxtras.FXUtils;
import io.fxtras.scene.mvvm.View;
import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.example.workassistant.ui.app.JvmTool;
import org.example.workassistant.ui.app.MavenTool;
import io.fxtras.scene.RouterView;
import org.example.workassistant.ui.controller.dbconn.ConnManageView;
import org.example.workassistant.ui.controller.domain.ClassDefView;
import org.example.workassistant.ui.controller.domain.TypeMappingTable;
import org.example.workassistant.ui.controller.expression.ExpressionEngineView;
import org.example.workassistant.ui.controller.fields.FieldsManageView;
import org.example.workassistant.ui.controller.mbg.MyBatisCodeGenerationView;
import org.example.workassistant.ui.controller.template.TemplateManageView;
import org.example.workassistant.ui.controls.PopupTextField;
import org.example.workassistant.ui.layout.LayoutPane;
import org.example.workassistant.ui.layout.menu.NavigationMenu;
import org.example.workassistant.ui.tools.fx.Tool;
import org.example.workassistant.ui.tools.mybatis.MyBatisXmlToolPane;
import org.example.workassistant.ui.view.DataTypeInfoTableView;
import org.example.workassistant.ui.view.FileTreeView;

import java.io.File;
import java.io.IOException;

public class Main extends Application {

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
        menu.addChild("JavaFX", RouterView.of(new Tool()));

        layoutPane.addNavigationMenu(menu1, menu);

        root.setTop(topBar());
        root.setCenter(layoutPane);

        Rectangle2D box = FXUtils.getScreenBox();
        layoutPane.expandAllMenu();

        Scene scene = new Scene(root, box.getWidth() * 0.75, box.getHeight() * 0.8);

        root.setBottom(new PopupTextField());

        stage.setTitle("开发工具");
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
        launch();
    }
}