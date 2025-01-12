package io.devpl.fxui.utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

public class ClassUtils {

    private ClassUtils() {
    }

    // getConstructor 方法入参是可变长参数列表，对应类中构造方法的入参类型，这里使用无参构造。
    // newInstance 返回的是泛型 T，取决于 clazz 的类型 Class<T>。这里直接用 Object 接收了。
    public static <T> T instantiate(Class<T> clazz) throws RuntimeException {
        try {
            final Constructor<T> constructor = clazz.getConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("failed to instantiate class " + clazz + " cause:", e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("failed to instantiate class " + clazz + " cause: no default constructor in Class[" + clazz + "]", e);
        }
    }

    /**
     * 两个数组是否相等
     * @param a1, a2
     * @return 数组是否相等
     */
    public static boolean equals(Object[] a1, Object[] a2) {
        if (a1 == null) {
            return a2 == null || a2.length == 0;
        }
        if (a2 == null) {
            return a1.length == 0;
        }
        if (a1.length != a2.length) {
            return false;
        }
        for (int i = 0; i < a1.length; i++) {
            if (a1[i] != a2[i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param classSimpleName 类的全限定类名
     * @return 类名
     */
    public static String getClassName(String classSimpleName) {
        if (classSimpleName == null) return "";
        int index = classSimpleName.lastIndexOf(".");
        if (index < 0) return "";
        return classSimpleName.substring(index + 1);
    }

    public static Field[] getDeclaredFields(Class<?> clazz) {
        return clazz.getDeclaredFields();
    }
}
