package io.devpl.fxui.view;

import io.devpl.fxui.components.NodeRender;
import io.devpl.fxui.components.pane.RouterPane;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;

import java.util.ArrayList;
import java.util.List;

/**
 * Mock Generator 配置面板
 */
public class MockGeneratorView extends BorderPane {

    public MockGeneratorView() {

        SplitPane splitPane = new SplitPane();

        List<GeneratorItem> generatorItems = generatorItems();

        List<String> list = generatorItems.stream().map(GeneratorItem::getName).toList();

        RouterPane routerPane = new RouterPane();

        ColumnValueGeneratorTable columnTable = new ColumnValueGeneratorTable(routerPane, list);

        columnTable.addColumns("db1", "name", "age", "sex");
        columnTable.addColumns("db2", "name", "age", "sex");

        columnTable.expandAll();

        for (GeneratorItem item : generatorItems) {
            routerPane.addRouteMapping(item.getName(), new NodeRender<DetailTable>() {
                @Override
                public DetailTable render() {
                    DetailTable detailTable = new DetailTable();
                    detailTable.setGeneratorName(item);
                    return detailTable;
                }
            });
        }

        splitPane.getItems().addAll(columnTable, routerPane);

        setCenter(splitPane);
    }

    public List<GeneratorItem> generatorItems() {
        List<GeneratorItem> generators = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            GeneratorItem generatorItem = new GeneratorItem("Generator" + i, "");

            for (int j = 0; j < i + 2; j++) {
                Option option = new Option();
                option.setName("Name" + j);
                option.setValue("Value" + i);

                generatorItem.addOption(option);
            }

            generators.add(generatorItem);
        }
        return generators;
    }
}
