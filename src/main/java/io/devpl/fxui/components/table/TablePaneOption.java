package io.devpl.fxui.components.table;

import com.dlsc.formsfx.model.structure.Form;

import java.util.function.Function;

public class TablePaneOption {

    private final Class<?> modelClass;
    private boolean enablePagination;
    private boolean enableToolBar;

    /**
     * 表单对象只有1个
     */
    private Object formObject;
    private Function<Object, Form> formCreator;

    public Object getFormObject() {
        return formObject;
    }

    @SuppressWarnings("unchecked")
    public <F> TablePaneOption form(F form, Function<F, Form> formCreator) {
        this.formObject = form;
        this.formCreator = (Function<Object, Form>) formCreator;
        return this;
    }

    public TablePaneOption formObject(Object formObject) {
        this.formObject = formObject;
        return this;
    }

    public Function<Object, Form> getFormCreator() {
        return formCreator;
    }

    public void formCreator(Function<Object, Form> formCreator) {
        this.formCreator = formCreator;
    }

    TablePaneOption(Class<?> modelClass) {
        this.modelClass = modelClass;
    }

    public static <T> TablePaneOption model(Class<T> modelClass) {
        return new TablePaneOption(modelClass);
    }

    public TablePaneOption enablePagination(boolean pageable) {
        this.enablePagination = pageable;
        return this;
    }

    public TablePaneOption enableToolbar(boolean enableToolBar) {
        this.enableToolBar = enableToolBar;
        return this;
    }

    public Class<?> getModelClass() {
        return modelClass;
    }

    public final boolean isPaginationEnabled() {
        return enablePagination;
    }

    public final boolean isToolBarEnabled() {
        return enableToolBar;
    }
}
