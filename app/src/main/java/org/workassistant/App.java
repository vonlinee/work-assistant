package org.workassistant;

import io.fxtras.JavaFXApplication;
import io.fxtras.ResourceLoader;
import io.fxtras.Theme;
import io.fxtras.fxml.FXMLLocator;
import io.fxtras.scene.RouterView;
import io.fxtras.scene.mvvm.View;
import io.fxtras.utils.FXUtils;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.jetbrains.annotations.Nullable;
import org.workassistant.tools.ToolContainerPane;
import org.workassistant.ui.app.JvmTool;
import org.workassistant.ui.app.MavenTool;
import org.workassistant.ui.controller.dbconn.ConnManageView;
import org.workassistant.ui.controller.domain.ClassDefView;
import org.workassistant.ui.controller.domain.TypeMappingTable;
import org.workassistant.ui.controller.expression.ExpressionEngineView;
import org.workassistant.ui.controller.fields.FieldsManageView;
import org.workassistant.ui.controller.mbg.MyBatisCodeGenerationView;
import org.workassistant.ui.controller.template.TemplateManageView;
import org.workassistant.ui.layout.LayoutPane;
import org.workassistant.ui.layout.NavigationMenu;
import org.workassistant.ui.tools.fx.Tool;
import org.workassistant.ui.tools.mybatis.MyBatisXmlToolPane;
import org.workassistant.ui.view.DataTypeInfoTableView;
import org.workassistant.ui.view.FileTreeView;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class App extends JavaFXApplication {

    Stage stage = new Stage();

    @Override
    public void onInit() throws Exception {
        // String property = System.getProperty("java.version");
        String javaSpecificationVersion = System.getProperty("java.specification.version");
        float javaVersion = Float.parseFloat(javaSpecificationVersion);
        if (javaVersion > 8) {
            // JDK9+以后只搜索模块内部的资源
            // 需要在module-info.java中添加export对应的View类给fxsdk模块
            View.setFxmlLocator(new FXMLLocator() {
                @Override
                public URL locate(Class<?> viewClass, String location) {
                    if (!location.startsWith("/")) {
                        location = "/" + location;
                    }
                    return viewClass.getResource(location);
                }
            });
        }
    }

    @Override
    protected @Nullable Theme getTheme() {
        return new Theme() {

            @Override
            public String getName() {
                return "Custom";
            }

            @Override
            public String getUserAgentStylesheet() {
                return "/theme/light.css";
            }

            @Override
            public @Nullable String getUserAgentStylesheetBSS() {
                return null;
            }
        };
    }

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

        NavigationMenu toolsMenu = new NavigationMenu("常用工具", new ToolContainerPane());
        layoutPane.addNavigationMenu(toolsMenu, menu1, menu);

        root.setTop(topBar());
        root.setCenter(layoutPane);

        layoutPane.expandAllMenu();

        Rectangle2D box = FXUtils.getScreenBox();
        Scene scene = new Scene(root, box.getWidth() * 0.75, box.getHeight() * 0.8);

        stage.setTitle("Assistant");
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

    @Override
    protected @Nullable ResourceLoader getResourceLoader() {
        return new ResourceLoader() {
            @Override
            public URL getResourceAsUrl(String resource) {
                return App.class.getResource(resource);
            }
        };
    }
}