package org.example.workassistant.sdk.lang;

public interface Bindings {

    /**
     * 根据名称获取值
     *
     * @param name 名称
     * @return 值
     */
    Object get(String name);

    class ArrayBindings implements Bindings {
        private final Object[] array;

        public ArrayBindings(Object[] array) {
            this.array = array;
        }

        @Override
        public Object get(String name) {
            return array[Integer.parseInt(name)];
        }
    }

    static Bindings ofArray(Object... args) {
        return new ArrayBindings(args);
    }
}
