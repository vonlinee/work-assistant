package io.devpl.fxui.controls;

import javafx.scene.control.TreeTableCell;

/**
 * TreeTableCellBase的父类
 *
 * @param <S>
 * @param <T>
 */
abstract class TreeTableCellBase<S, T> extends TreeTableCell<S, T> {

    final int getRowIndex() {
        return getTableRow().getIndex();
    }

    final void edit() {
        getTreeTableView().edit(getRowIndex(), getTableColumn());
    }
}
