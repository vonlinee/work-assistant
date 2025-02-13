package io.fxtras.scene.control.table;

import java.util.function.Function;

public class LambdaCellValueFactory<S, T> extends BeanCellValueFactory<S, T> {

    private final Function<S, T> mapper;

    public LambdaCellValueFactory(Function<S, T> mapper) {
        this.mapper = mapper;
    }

    @Override
    protected T getCellValue(S row) {
        return mapper.apply(row);
    }
}
