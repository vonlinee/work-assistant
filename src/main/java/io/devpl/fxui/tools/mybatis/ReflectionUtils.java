package io.devpl.fxui.tools.mybatis;

import java.lang.reflect.*;
import java.util.Objects;

/**
 * 反射的 Utils 函数集合
 * 提供访问私有变量, 获取泛型类型 Class, 提取集合中元素属性等 Utils 函数
 */
public final class ReflectionUtils {

    private ReflectionUtils() {
    }

    /**
     * 直接读取对象的属性值, 忽略 private/protected 修饰符, 也不经过 getter
     * @param object    对象
     * @param fieldName 对象的字段
     * @return 字段值
     */
    public static Object getValue(Object object, String fieldName) {
        Field field = getDeclaredField(object, fieldName);
        if (field == null) {
            throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + object + "]");
        }
        if (!tryMakeAccessible(field)) {
            return null;
        }
        Object result = null;
        try {
            result = field.get(object);
        } catch (IllegalAccessException e) {
            // ignore
        }
        return result;
    }

    /**
     * 直接设置对象属性值, 忽略 private/protected 修饰符, 也不经过 setter
     * @param object    对象
     * @param fieldName 对象的字段
     * @param value     设置的字段值
     */
    public static void setValue(Object object, String fieldName, Object value) {
        Field field = getDeclaredField(object, fieldName);
        if (field == null) {
            throw new IllegalArgumentException("Could not find field [" + fieldName + "] on target [" + object + "]");
        }
        if (!tryMakeAccessible(field)) {
            return;
        }
        try {
            field.set(object, value);
        } catch (IllegalAccessException e) {
            // ignore
        }
    }


    /**
     * 通过反射, 获得定义 Class 时声明的父类的泛型参数的类型
     * 如: public EmployeeDao extends BaseDao<Employee, String>
     * @param clazz 指定的Class
     * @param index 第几个泛型参数
     * @return 泛型类型
     */
    public static Class<?> getSuperClassGenricType(Class<?> clazz, int index) {
        Type genType = clazz.getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        }
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        if (index >= params.length || index < 0) {
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            return Object.class;
        }
        return (Class<?>) params[index];
    }

    /**
     * 通过反射, 获得 Class 定义中声明的父类的泛型参数类型
     * 如: public EmployeeDao extends BaseDao<Employee, String>
     * @param <T>   泛型
     * @param clazz 目标Class
     * @return 泛型Class
     */
    @SuppressWarnings("unchecked")
    public static <T> Class<T> getSuperGenericType(Class<T> clazz) {
        return (Class<T>) getSuperClassGenricType(clazz, 0);
    }

    /**
     * 循环向上转型, 获取对象的 DeclaredMethod
     * @param object         对象
     * @param methodName     方法名
     * @param parameterTypes 方法的参数类型列表
     * @return Method对象
     */
    public static Method getDeclaredMethod(Object object, String methodName, Class<?>[] parameterTypes) {

        for (Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                return superClass.getDeclaredMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                //Method 不在当前类定义, 继续向上转型
            }
        }

        return null;
    }

    /**
     * 使 filed 变为可访问
     * @param field 字段
     */
    public static boolean tryMakeAccessible(Field field) {
        try {
            if (!Modifier.isPublic(field.getModifiers())) {
                field.setAccessible(true);
                return true;
            }
            return false;
        } catch (SecurityException securityException) {
            // JDK 8会抛出此异常
            return false;
        }
    }

    /**
     * 循环向上转型, 获取对象的 DeclaredField
     * getDeclaredFiled 仅能获取类本身的属性成员（包括私有、共有、保护）
     * @param object    对象
     * @param filedName 字段名
     * @return Field实例
     */
    public static Field getDeclaredField(Object object, String filedName) {
        for (Class<?> superClass = object.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                return superClass.getDeclaredField(filedName);
            } catch (NoSuchFieldException e) {
                //Field 不在当前类定义, 继续向上转型
            }
        }
        return null;
    }

    /**
     * 仅能获取类(及其父类)的public属性成员
     * @param obj       对象
     * @param fieldName 获取的字段名
     * @return Field对象
     */
    public static Field getField(Object obj, String fieldName) throws NoSuchFieldException {
        return Objects.requireNonNull(obj).getClass().getField(fieldName);
    }

    /**
     * 直接调用对象方法, 而忽略修饰符(private, protected)
     * @param object         对象
     * @param methodName     方法名
     * @param parameterTypes 参数类型
     * @param parameters     方法执行的参数
     * @return 方法返回值
     * @throws InvocationTargetException 执行方法出错
     * @throws IllegalArgumentException  执行方法出错
     */
    public static Object invokeMethod(Object object, String methodName, Class<?>[] parameterTypes,
                                      Object[] parameters) throws InvocationTargetException, IllegalAccessException {
        Method method = getDeclaredMethod(object, methodName, parameterTypes);
        if (method == null) {
            throw new IllegalArgumentException("Could not find method [" + methodName + "] on target [" + object + "]");
        }
        method.setAccessible(true);
        return method.invoke(object, parameters);
    }
}
