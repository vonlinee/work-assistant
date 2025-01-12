package io.devpl.fxui.utils;

import io.devpl.fxui.components.table.TableColumnInitializer;
import io.devpl.fxui.components.table.TableViewColumn;
import io.devpl.fxui.components.table.TableViewModel;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class FXUtils {

    /**
     * @return 屏幕尺寸
     * @see Toolkit#getDefaultToolkit()
     * @see Toolkit#getScreenSize()
     */
    public static Rectangle2D getScreenBox() {
        return Screen.getPrimary().getBounds();
    }

    public static void layoutInRegion(Region parent, Node child) {
        Region.layoutInArea(child, 0, 0, parent.getWidth(), parent.getHeight(), 0, Insets.EMPTY, true, true, HPos.CENTER, VPos.CENTER, parent.isSnapToPixel());
    }

    public static <S> void setData(TableView<S> tableView, List<S> data) {
        ObservableList<S> items = tableView.getItems();
        if (!items.isEmpty()) {
            items.clear();
        }
        items.addAll(data);
    }

    public static void fixWidth(TableColumn<?, ?> region, double size) {
        region.setMinWidth(size);
        region.setMaxWidth(size);
        region.setPrefWidth(size);
    }

    /**
     * 创建 TableView 实例
     *
     * @param rowClass 行数据类型
     * @param <R>      行数据类型
     * @return TableView
     * @see TableViewColumn
     * @see TableViewModel
     */
    public static <R> TableView<R> createTableView(Class<R> rowClass) {
        return initTableViewColumns(new TableView<>(), rowClass);
    }

    /**
     * 创建 TableView 实例
     *
     * @param rowClass 行数据类型
     * @param <R>      行数据类型
     * @return TableView
     * @see TableViewColumn
     * @see TableViewModel
     */
    public static <R> TableView<R> initTableViewColumns(TableView<R> tableView, Class<R> rowClass) {
        tableView.getColumns().addAll(createTableViewColumns(tableView, rowClass, new TableColumnInitializer<>()));
        return tableView;
    }

    /**
     * 创建 TableColumn 列
     *
     * @param tableView         表格
     * @param rowClass          行数据类型
     * @param columnInitializer 列初始化
     * @param <R>               行数据类型
     * @return TableColumn 列
     */
    public static <R, C> List<TableColumn<R, C>> createTableViewColumns(TableView<R> tableView, Class<R> rowClass, TableColumnInitializer<R> columnInitializer) {
        TableViewModel tableViewModel = rowClass.getAnnotation(TableViewModel.class);
        if (tableViewModel == null) {
            tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        } else {
            tableView.setEditable(tableViewModel.editable());
            if (tableViewModel.selectionModel() == 1) {
                tableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            } else if (tableViewModel.selectionModel() == 0) {
                tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
            }
            tableView.setColumnResizePolicy(tableViewModel.resizePolicy() == 1 ? TableView.CONSTRAINED_RESIZE_POLICY : TableView.UNCONSTRAINED_RESIZE_POLICY);
        }
        boolean order = tableViewModel != null && tableViewModel.orderFields();
        return columnInitializer.initColumns(rowClass, order);
    }

    public static void setText(Label label, String text) {
        Font font = label.getFont();
        double pixelWidth = calculateTextPixelWidth(text, font);
        label.setMinWidth(pixelWidth);
        label.setPrefWidth(pixelWidth);
    }

    public static void setText(Button button, String text) {
        Font font = button.getFont();
        double pixelWidth = calculateTextPixelWidth(text, font);
        button.setMinWidth(pixelWidth);
        button.setPrefWidth(pixelWidth);
    }

    /**
     * <a href="https://blog.csdn.net/wangpaiblog/article/details/121529154">...</a>
     * 不同的符号的尺寸是不同的，不仅是中文汉字与英文字母，就连运算符、数字、英文字母之间的尺寸也不尽相同。不自适应各种 Unicode 符号的算法，
     * 而是使用 JavaFX 内置的 API，如 Text 类的方法 Text对象.getBoundsInLocal().getWidth()。
     *
     * @param text 文本
     * @param font 文本字体
     * @return 对应的宽度
     */
    public static double calculateTextPixelWidth(String text, Font font) {
        Text theText = new Text(text);
        theText.setFont(font);
        return theText.getBoundsInLocal().getWidth();
    }

    /**
     * @param originText         内文本的内容
     * @param font               内文本的字体
     * @param lineSeparator      换行符的定义
     * @param originMaxWidth     内文本最大的行宽
     * @param rowExtension       对话框横向两端与内文本的边距
     * @param originSingleHeight 内文本一行的高度
     * @param columnExtension    对话框纵向向两端与内文本的边距
     * @return 计算出的对话框的宽度。其中，[0] 代表宽度，[1] 代表高度
     */
    public static double[] calculateTextBoxSize(String originText, Font font, String lineSeparator, double originMaxWidth, double rowExtension, double originSingleHeight, double columnExtension) {
        double maxRowLength = 0;
        int formattedColumnNum = 0;
        if (originText != null && !originText.isEmpty()) {
            var texts = originText.split(lineSeparator);
            if (texts.length == 0) { // 如果文本中只有换行符
                maxRowLength = 0;
                formattedColumnNum = originText.length() + 1; // 注意要加 1
            } else {
                double singleRowLength = 0;
                for (var text : texts) {
                    var singleOriginWidth = calculateTextPixelWidth(text, font);
                    singleRowLength = Math.min(singleOriginWidth, originMaxWidth); // 注意：这是求最小值
                    maxRowLength = Math.max(maxRowLength, singleRowLength); // 注意：这里求最大值
                    formattedColumnNum += (int) (singleOriginWidth / originMaxWidth) + 1; // 注意要加 1
                }
            }
        }

        double[] result = new double[2];
        result[0] = maxRowLength + rowExtension * 2;
        result[1] = formattedColumnNum * originSingleHeight + columnExtension * 2;
        return result;
    }

    /**
     * 对于 TextArea，其换行符为 \n
     *
     * @param originText         内文本的内容
     * @param font               内文本的字体
     * @param originMaxWidth     内文本最大的行宽
     * @param rowExtension       对话框横向两端与内文本的边距
     * @param originSingleHeight 内文本一行的高度
     * @param columnExtension    对话框纵向向两端与内文本的边距
     * @return 计算出的对话框的宽度。其中，[0] 代表宽度，[1] 代表高度
     */
    public static double[] calculateTextBoxSize(String originText, Font font, double originMaxWidth, double rowExtension, double originSingleHeight, double columnExtension) {
        String lineSeparator = "\n";
        return calculateTextBoxSize(originText, font, lineSeparator, originMaxWidth, rowExtension, originSingleHeight, columnExtension);
    }

    /**
     * 是否鼠标左键双击
     *
     * @param mouseEvent 鼠标事件
     * @return 是否鼠标左键双击
     */
    public static boolean isPrimaryButtonDoubleClicked(MouseEvent mouseEvent) {
        return mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2;
    }

    /**
     * 删除TableView的选择项，JavaFX提供的API有点问题
     * <a href="https://stackoverflow.com/questions/18700430/issue-with-removing-multiple-rows-at-once-from-javafx-tableview">...</a>
     *
     * @param tableView SimpleTableView
     * @param <S>       数据类型
     */
    public static <S> void removeSelected(TableView<S> tableView) {
        final TableView.TableViewSelectionModel<S> selectionModel = tableView.getSelectionModel();
        tableView.getItems().removeAll(selectionModel.getSelectedItems());
        selectionModel.clearSelection(); // 清除选择状态
    }

    public static boolean isRowEmpty(TableRow<?> row) {
        return row == null || row.isEmpty();
    }

    public static boolean isRowEmpty(TreeTableRow<?> row) {
        return row == null || row.isEmpty();
    }

    public static TextArea newTextArea(int fontSize) {
        TextArea textArea = new TextArea();
        textArea.setFont(Font.font(fontSize));
        return textArea;
    }

    public static void expandAll(TreeItem<?> treeItem) {
        if (!treeItem.isExpanded()) {
            treeItem.setExpanded(true);
        }
        if (!treeItem.isLeaf() && !treeItem.getChildren().isEmpty()) {
            for (TreeItem<?> child : treeItem.getChildren()) {
                expandAll(child);
            }
        }
    }

    public static void layoutRoot(Region region, Node root) {
        Region.layoutInArea(root, 0, 0, region.getWidth(), region.getHeight(), 0, new Insets(0), true, true, HPos.CENTER, VPos.CENTER, false);
    }

    public static Background newBackground(javafx.scene.paint.Paint color) {
        return new Background(new BackgroundFill(color, new CornerRadii(0), new Insets(0)));
    }

    public static Border newBorder(Paint color) {
        return new Border(new BorderStroke(color, BorderStrokeStyle.NONE, new CornerRadii(0), new BorderWidths(0)));
    }

    /**
     * 兼容NPE
     *
     * @param bool 布尔值
     * @return 是否为true
     */
    public static boolean isTrue(Boolean bool) {
        return bool != null && bool;
    }

    /**
     * 兼容NPE
     *
     * @param bool 布尔值
     * @return 是否为false
     */
    public static boolean isFalse(Boolean bool) {
        return bool != null && !bool;
    }

    /**
     * 获取Stage
     *
     * @param event 事件对象
     * @return Stage
     */
    @Nullable
    public static Stage getStage(Event event) {
        Object eventSource = event.getSource();
        if (eventSource instanceof Node node) {
            Scene scene = node.getScene();
            if (scene != null) {
                Window window = scene.getWindow();
                if (window instanceof Stage stage) {
                    return stage;
                }
            }
        }
        return null;
    }

    public static Button newButton(String title, EventHandler<ActionEvent> activeEventEventHandler) {
        Button button = new Button(title);
        button.setOnAction(activeEventEventHandler);
        return button;
    }

    public static boolean isStringHasText(String str) {
        return str != null && !str.isBlank();
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }
}
