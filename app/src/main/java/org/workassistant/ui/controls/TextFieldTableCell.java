package org.workassistant.ui.controls;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

/**
 * 默认的TextFieldTableCell进入编辑时，TextField的高度大于单元格行高，会将行撑开
 *
 * @see TextFieldTableCell
 */
public class TextFieldTableCell<S, T> extends TableCell<S, T> {

    /**
     * Provides a {@link TextField} that allows editing of the cell content when
     * the cell is double-clicked, or when
     * {@link TableView#edit(int, javafx.scene.control.TableColumn)} is called.
     * This method will only  work on {@link TableColumn} instances which are of
     * type String.
     *
     * @param <S> The type of the TableView generic type
     * @return A {@link Callback} that can be inserted into the
     * {@link TableColumn#cellFactoryProperty() cell factory property} of a
     * TableColumn, that enables textual editing of the content.
     */
    public static <S> Callback<TableColumn<S, String>, TableCell<S, String>> forTableColumn() {
        return forTableColumn(new DefaultStringConverter());
    }

    /**
     * Provides a {@link TextField} that allows editing of the cell content when
     * the cell is double-clicked, or when
     * {@link TableView#edit(int, javafx.scene.control.TableColumn) } is called.
     * This method will work  on any {@link TableColumn} instance, regardless of
     * its generic type. However, to enable this, a {@link StringConverter} must
     * be provided that will convert the given String (from what the user typed
     * in) into an instance of type T. This item will then be passed along to the
     * {@link TableColumn#onEditCommitProperty()} callback.
     *
     * @param <S>       The type of the TableView generic type
     * @param <T>       The type of the elements contained within the TableColumn
     * @param converter A {@link StringConverter} that can convert the given String
     *                  (from what the user typed in) into an instance of type T.
     * @return A {@link Callback} that can be inserted into the
     * {@link TableColumn#cellFactoryProperty() cell factory property} of a
     * TableColumn, that enables textual editing of the content.
     */
    public static <S, T> Callback<TableColumn<S, T>, TableCell<S, T>> forTableColumn(
        final StringConverter<T> converter) {
        return list -> new TextFieldTableCell<>(converter);
    }

    private TextField textField;

    /**
     * Creates a default TextFieldTableCell with a null converter. Without a
     * {@link StringConverter} specified, this cell will not be able to accept
     * input from the TextField (as it will not know how to convert this back
     * to the domain object). It is therefore strongly encouraged to not use
     * this constructor unless you intend to set the converter separately.
     */
    public TextFieldTableCell() {
        this(null);
    }

    /**
     * Creates a TextFieldTableCell that provides a {@link TextField} when put
     * into editing mode that allows editing of the cell content. This method
     * will work on any TableColumn instance, regardless of its generic type.
     * However, to enable this, a {@link StringConverter} must be provided that
     * will convert the given String (from what the user typed in) into an
     * instance of type T. This item will then be passed along to the
     * {@link TableColumn#onEditCommitProperty()} callback.
     *
     * @param converter A {@link StringConverter converter} that can convert
     *                  the given String (from what the user typed in) into an instance of
     *                  type T.
     */
    public TextFieldTableCell(StringConverter<T> converter) {
        this.getStyleClass().add("my-text-field-table-cell");
        setConverter(converter);
        setPadding(new Insets(0));
    }

    private final ObjectProperty<StringConverter<T>> converter =
        new SimpleObjectProperty<StringConverter<T>>(this, "converter");

    /**
     * The {@link StringConverter} property.
     *
     * @return the {@link StringConverter} property
     */
    public final ObjectProperty<StringConverter<T>> converterProperty() {
        return converter;
    }

    /**
     * Sets the {@link StringConverter} to be used in this cell.
     *
     * @param value the {@link StringConverter} to be used in this cell
     */
    public final void setConverter(StringConverter<T> value) {
        converterProperty().set(value);
    }

    /**
     * Returns the {@link StringConverter} used in this cell.
     *
     * @return the {@link StringConverter} used in this cell
     */
    public final StringConverter<T> getConverter() {
        return converterProperty().get();
    }

    @Override
    public void startEdit() {
        if (!isEditable()
            || !getTableView().isEditable()
            || !getTableColumn().isEditable()) {
            return;
        }
        super.startEdit();

        if (isEditing()) {
            if (textField == null) {
                textField = createTextField(this, getConverter());
                // 设置高度
                textField.setPrefHeight(getHeight());
                textField.setStyle("-fx-border-radius: 0; -fx-background-insets: 0; -fx-border-width: 0;-fx-border-insets: 0");
                textField.setPadding(new Insets(0));
            }
            textField.requestFocus();
            CellUtils.startEdit(this, getConverter(), null, null, textField);
        }
    }

    protected TextField createTextField(final Cell<T> cell, final StringConverter<T> converter) {
        final TextField textField = new TextField(getItemText(cell, converter));
        // Use onAction here rather than onKeyReleased (with check for Enter),
        // as otherwise we encounter RT-34685
        textField.setOnAction(event -> {
            T t = null;
            if (converter != null) {
                t = converter.fromString(textField.getText());
            }
            cell.commitEdit(t);
            event.consume();
        });
        textField.setOnKeyReleased(t -> {
            if (t.getCode() == KeyCode.ESCAPE) {
                cell.cancelEdit();
                t.consume();
            }
        });
        return textField;
    }

    public static <T> String getItemText(Cell<T> cell, StringConverter<T> converter) {
        return converter == null ? cell.getItem() == null ? "" : cell.getItem().toString() : converter.toString(cell.getItem());
    }

    @Override
    public void cancelEdit() {
        super.cancelEdit();
        CellUtils.cancelEdit(this, getConverter(), null);
    }

    @Override
    public void updateItem(T item, boolean empty) {
        super.updateItem(item, empty);
        CellUtils.updateItem(this, getConverter(), null, null, textField);
    }
}
