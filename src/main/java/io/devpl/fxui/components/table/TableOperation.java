package io.devpl.fxui.components.table;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.devpl.fxui.view.DataTypeItem;

import java.util.Collections;
import java.util.List;

/**
 * TablePane操作
 *
 * @param <R>
 */
public interface TableOperation<F, R> {

    /**
     * 加载指定页的数据，页码切换后会调用此方法
     *
     * @param pageNum  第几页
     * @param pageSize 每页记录条数
     * @return 分页数据
     */
    default TableData<R> loadPage(int pageNum, int pageSize) {
        try (Page<DataTypeItem> page = PageHelper.startPage(pageNum, pageSize)) {
            List<R> list = loadPageData(pageNum, pageSize);
            PageInfo<R> pageInfo = new PageInfo<>(list);
            return new TableData<>(list, pageInfo.getTotal(), page.getPageNum(), page.getPageSize());
        }
    }

    /**
     * 加载一页的数据
     *
     * @param pageNum  第几页
     * @param pageSize 每页数据量大小
     * @return 一页的数据
     */
    default List<R> loadPageData(int pageNum, int pageSize) {
        return Collections.emptyList();
    }

    /**
     * 填充表单数据
     * 修改row会导致表格数据发生变化，刷新后界面数据会被更新，导致数据库数据和界面数据不一致
     *
     * @param row        编辑的行对象，可能为null，表示新增(如果newItem返回不为null，则参数row不为null)，不为null表示更新
     * @param formObject 表单对象
     */
    default void fillForm(int index, R row, F formObject) {
    }

    /**
     * 重置表单数据
     *
     * @param formObject 表单对象
     */
    default void resetForm(F formObject) {
    }

    /**
     * 将表单对象转换为表格行对象
     *
     * @param row 行，可能为空
     * @return 表格行对象，返回不为null
     */
    default R extractForm(F oldForm, R row) {
        return null;
    }

    /**
     * 保存单条记录
     *
     * @param record 单条记录
     */
    default void save(R record) {
    }

    default void update(R record) {
    }

    default void delete(R record) {
    }

    /**
     * 批量保存记录
     *
     * @param records 多条记录
     */
    default void saveBatch(List<R> records) {
    }

    /**
     * 创建一个空行
     *
     * @param rowClass 行数据类, java bean
     * @return 空对象
     */
    default R newItem(Class<R> rowClass) {
        return null;
    }
}
