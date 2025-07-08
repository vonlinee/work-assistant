package org.workassistant.util.lang;

import java.util.Map;
import java.util.regex.Pattern;

public class NamedInterpolationEngine extends PatternInterpolationEngine {
    private static final Pattern PATTERN = Pattern
        .compile("\\{([_a-zA-Z0-9]+)}");

    public NamedInterpolationEngine() {
        super(PATTERN);
    }

    public static Bindings createBindings(Object... array) {
        return new AssociativeArrayBindings(array);
    }

    public static Bindings createBindings(Map<String, ?> map,
                                          Object defaultValue) {
        return new MapBindings(map, defaultValue);
    }

    private static final class AssociativeArrayBindings implements Bindings {
        private final Object[] associativeArray;
        private final int lookupLength;
        private final Object defaultValue;

        public AssociativeArrayBindings(Object[] associativeArray) {
            this.associativeArray = associativeArray;
            if (associativeArray.length % 2 == 0) {
                lookupLength = associativeArray.length;
                defaultValue = null;
            } else {
                lookupLength = associativeArray.length - 1;
                defaultValue = associativeArray[associativeArray.length - 1];
            }
        }

        @Override
        public Object get(String name) {
            for (int i = 0; i < lookupLength; i += 2) {
                if (name.equals(associativeArray[i])) {
                    return associativeArray[i + 1];
                }
            }
            return defaultValue;
        }
    }

    private static final class MapBindings implements Bindings {
        private final Map<String, ?> map;
        private final Object defaultValue;

        public MapBindings(Map<String, ?> map, Object defaultValue) {
            this.map = map;
            this.defaultValue = defaultValue;
        }

        @Override
        public Object get(String name) {
            Object val = map.get(name);
            return val == null ? defaultValue : val;
        }
    }
}
