package io.fxtras.scene.control.table;

/**
 * 基于普通对象的单行数据表格
 *
 * @param <S>
 */
public class SimpleBeanTableView<S> extends BaseTableView<S> {
    public SimpleBeanTableView() {
        setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
    }
}
