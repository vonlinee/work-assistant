package io.devpl.sdk.util;

import java.util.function.Supplier;

/**
 * DataObject工厂
 *
 * @see DataObject
 */
public final class DataClass {

    private static final DataClass INSTANCE = new DataClass(() -> new ArrayMapDataObject(5));
    private Supplier<DataObject> supplier;

    DataClass(Supplier<DataObject> supplier) {
        this.supplier = supplier;
    }

    public static DataObject newObject() {
        return INSTANCE.supplier.get();
    }

    public static void setObjectFactory(Supplier<DataObject> supplier) {
        INSTANCE.supplier = supplier;
    }
}
