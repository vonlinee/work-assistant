package org.workassistant.util.util;

import cn.hutool.core.bean.BeanUtil;

import java.lang.reflect.*;
import java.util.*;

/**
 * BeanUtils
 */
public class BeanUtils {

    public static final Short DEFAULT_SHORT = (short) 0;
    public static final Integer DEFAULT_INTEGER = 0;
    public static final Long DEFAULT_LONG = 0L;
    public static final Float DEFAULT_FLOAT = (float) 0.0;
    public static final Double DEFAULT_DOUBLE = 0.0;
    public static final Byte DEFAULT_BYTE = (byte) 0;
    public static final Character DEFAULT_CHAR = (char) 0;

    /**
     * 判断方法名是否是符合JavaBean规范的getter方法名
     *
     * @param name 方法名称
     * @return 是否是getter方法名
     */
    public static boolean isGetterName(String name) {
        return name.startsWith("get") || name.startsWith("is") || name.startsWith("has");
    }

    /**
     * 从getter方法中获取属性名称
     *
     * @param getterName getter方法名称
     * @return 属性名称
     */
    public static String getPropertyNameFromGetter(String getterName) {
        if (getterName.startsWith("get") || getterName.startsWith("has")) {
            return Character.toLowerCase(getterName.charAt(3)) + getterName.substring(4);
        } else if (getterName.startsWith("is")) {
            return Character.toLowerCase(getterName.charAt(2)) + getterName.substring(3);
        }
        // Unrecognized getter name
        return null;
    }

    /**
     * 从getter方法中获取对应的setter方法名称
     *
     * @param getterName getter方法名称
     * @return setter方法名称
     */
    public static String getSetterName(String getterName) {
        if (getterName.startsWith("get")) {
            return "set" + getterName.substring(3);
        } else if (getterName.startsWith("is")) {
            return "set" + getterName.substring(2);
        } else if (getterName.startsWith("has")) {
            return "set" + getterName.substring(3);
        } else {
            return null; // Unrecognized getter name
        }
    }

    /**
     * Returns a set method matching the property name.
     */
    public static Method getSetMethod(Class<?> cl, String propertyName) {
        Method method = getSetMethod(cl, propertyName, false);

        if (method != null) {
            return method;
        }

        return getSetMethod(cl, propertyName, true);
    }

    /**
     * Returns a set method matching the property name.
     */
    public static Method getSetMethod(Class<?> cl, String propertyName, boolean ignoreCase) {
        String setName = "set" + propertyNameToMethodName(propertyName);

        return getSetMethod(cl.getMethods(), setName, ignoreCase);
    }

    /**
     * Returns a get method matching the property name.
     */
    public static Method getGetMethod(Class<?> cl, String propertyName) {
        Method method = getGetMethod(cl, propertyName, false);

        return method != null ? method : getGetMethod(cl, propertyName, true);
    }

    /**
     * Returns a get method matching the property name.
     */
    public static Method getGetMethod(Class<?> cl, String propertyName, boolean ignoreCase) {
        String methodName = propertyNameToMethodName(propertyName);
        return getGetMethod(cl.getMethods(), "get" + methodName, "is" + methodName, ignoreCase);
    }

    /**
     * Converts a user's property name to a bean method name.
     *
     * @param propertyName the user property name
     * @return the equivalent bean method name
     */
    public static String propertyNameToMethodName(String propertyName) {
        char ch = propertyName.charAt(0);
        if (Character.isLowerCase(ch)) propertyName = Character.toUpperCase(ch) + propertyName.substring(1);

        return propertyName;
    }

    /**
     * Converts a user's property name to a bean method name.
     *
     * @param methodName the method name
     * @return the equivalent property name
     */
    public static String methodNameToPropertyName(String methodName) {
        if (methodName.startsWith("get")) methodName = methodName.substring(3);
        else if (methodName.startsWith("set")) methodName = methodName.substring(3);
        else if (methodName.startsWith("is")) methodName = methodName.substring(2);

        if (methodName.isEmpty()) return null;

        char ch = methodName.charAt(0);
        if (Character.isUpperCase(ch) && (methodName.length() == 1 || !Character.isUpperCase(methodName.charAt(1)))) {
            methodName = Character.toLowerCase(ch) + methodName.substring(1);
        }

        return methodName;
    }

    public static boolean isArrayType(Type type) {
        return (type instanceof Class && ((Class<?>) type).isArray());
    }

    public static boolean isCollectionType(Type type) {
        if (type instanceof Class && Collection.class.isAssignableFrom((Class<?>) type)) {
            return true;
        }
        if (type instanceof ParameterizedType pt) {
            if (pt.getActualTypeArguments().length == 1) {
                return true;
            }
        }
        return isArrayType(type);
    }

    public static Class<?> getCollectionType(Type type) {
        if (type instanceof ParameterizedType pt) {
            if (pt.getActualTypeArguments().length == 1) {
                final Type argType = pt.getActualTypeArguments()[0];
                if (argType instanceof Class) {
                    return (Class<?>) argType;
                } else if (argType instanceof WildcardType) {
                    final Type[] upperBounds = ((WildcardType) argType).getUpperBounds();
                    if (upperBounds.length > 0 && upperBounds[0] instanceof Class) {
                        return (Class<?>) upperBounds[0];
                    }
                    final Type[] lowerBounds = ((WildcardType) argType).getLowerBounds();
                    if (lowerBounds.length > 0 && lowerBounds[0] instanceof Class) {
                        return (Class<?>) lowerBounds[0];
                    }
                }
            }
        }
        return null;
    }

    public static Object readObjectProperty(Object object, String propName) throws IllegalAccessException, InvocationTargetException {
        if (propName.indexOf('.') == -1) {
            Method getter = getGetMethod(object.getClass(), propName);
            return getter == null ? null : getter.invoke(object);
        }
        // Parse property path
        StringTokenizer st = new StringTokenizer(propName, ".");
        Object value = object;
        while (value != null && st.hasMoreTokens()) {
            String pathItem = st.nextToken();
            Method getter = getGetMethod(value.getClass(), pathItem);
            if (getter == null) {
                return null;
            }
            value = getter.invoke(value);
        }
        return value;
    }

    /**
     * Finds the matching set method
     */
    private static Method getGetMethod(Method[] methods, String getName, String isName, boolean ignoreCase) {
        for (Method method : methods) {
            // The method must be public
            if ((!Modifier.isPublic(method.getModifiers())) || (!Modifier.isPublic(method.getDeclaringClass().getModifiers())) || (method.getParameterTypes().length != 0) || (method.getReturnType().equals(void.class))) {
                continue;
            } else if (!ignoreCase && method.getName().equals(getName)) {
                // If it matches the get name, it's the right method
                return method;
            } else if (ignoreCase && method.getName().equalsIgnoreCase(getName)) {
                // If it matches the get name, it's the right method
                return method;
            } else if (!method.getReturnType().equals(boolean.class)) {
                // The is methods must return boolean
                continue;
            } else if (!ignoreCase && method.getName().equals(isName)) {
                // If it matches the is name, it must return boolean
                return method;
            } else if (ignoreCase && method.getName().equalsIgnoreCase(isName)) {
                // If it matches the is name, it must return boolean
                return method;
            }
        }
        return null;
    }

    /**
     * Finds the matching set method
     *
     * @param setName the method name
     */
    private static Method getSetMethod(Method[] methods, String setName, boolean ignoreCase) {
        for (Method method : methods) {
            // The method name must match
            if (!(ignoreCase ? method.getName().equalsIgnoreCase(setName) : method.getName().equals(setName)) || !Modifier.isPublic(method.getModifiers()) || !Modifier.isPublic(method.getDeclaringClass().getModifiers()) || method.getParameterTypes().length != 1)
                continue;

            return method;
        }

        return null;
    }

    public static boolean isBooleanType(Type paramClass) {
        return paramClass == Boolean.class || paramClass == Boolean.TYPE;
    }

    public static Object getDefaultPrimitiveValue(Class<?> paramClass) {
        if (paramClass == Boolean.TYPE) {
            return Boolean.FALSE;
        } else if (paramClass == Short.TYPE) {
            return DEFAULT_SHORT;
        } else if (paramClass == Integer.TYPE) {
            return DEFAULT_INTEGER;
        } else if (paramClass == Long.TYPE) {
            return DEFAULT_LONG;
        } else if (paramClass == Float.TYPE) {
            return DEFAULT_FLOAT;
        } else if (paramClass == Double.TYPE) {
            return DEFAULT_DOUBLE;
        } else if (paramClass == Byte.TYPE) {
            return DEFAULT_BYTE;
        } else if (paramClass == Character.TYPE) {
            return DEFAULT_CHAR;
        } else {
            throw new IllegalArgumentException("Class " + paramClass.getName() + " is not primitive type");
        }
    }

    public static boolean isNumericType(Class<?> paramClass) {
        return Number.class.isAssignableFrom(paramClass) || paramClass == Short.TYPE || paramClass == Integer.TYPE || paramClass == Long.TYPE || paramClass == Double.TYPE || paramClass == Float.TYPE || paramClass == Byte.TYPE;
    }

    public static Object invokeObjectMethod(Object object, String name, Class<?>[] paramTypes, Object[] args) throws Throwable {
        Method method = object.getClass().getMethod(name, paramTypes);
        method.setAccessible(true);
        try {
            return method.invoke(object, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    public static Object invokeObjectMethod(Object object, String name) throws Throwable {
        Method method = object.getClass().getMethod(name);
        method.setAccessible(true);
        try {
            return method.invoke(object);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    public static Object invokeObjectDeclaredMethod(Object object, String methodName, Class<?>[] paramTypes, Object[] args) throws Throwable {
        for (Class<?> cls = object.getClass(); cls != null; cls = cls.getSuperclass()) {
            for (Method method : cls.getDeclaredMethods()) {
                if (method.getName().equals(methodName) && Arrays.equals(method.getParameterTypes(), paramTypes)) {
                    method.setAccessible(true);
                    try {
                        return method.invoke(object, args);
                    } catch (InvocationTargetException e) {
                        throw e.getTargetException();
                    }
                }
            }
        }
        throw new NoSuchMethodException("Cannot find declared method " + methodName + "(" + Arrays.toString(paramTypes) + ")");
    }

    public static Object invokeStaticMethod(Class<?> objectType, String name, Class<?>[] paramTypes, Object[] args) throws Throwable {
        Method method = objectType.getMethod(name, paramTypes);
        method.setAccessible(true);
        try {
            return method.invoke(null, args);
        } catch (InvocationTargetException e) {
            throw e.getTargetException();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<? extends T> findAssignableType(Class<?>[] types, Class<T> type) {
        for (Class<?> childType : types) {
            if (type.isAssignableFrom(childType)) {
                return (Class<? extends T>) childType;
            }
        }
        return null;
    }

    /**
     * Determines the distance of the given object to the given class in the
     * inheritance tree.
     * <p>
     *
     * @param object the root object
     * @param clazz  the class to determine the distance to
     * @return the distance of the object to the class. If the object is not an
     * instance of the class {@code -1} will return.
     */
    public static int getInheritanceDistance(Object object, Class<?> clazz) {
        if (clazz.isInstance(object)) {
            int distance = 0;
            Class<?> compared = object.getClass();
            while (compared != clazz) {
                compared = compared.getSuperclass();
                distance++;
                if (compared == Object.class) {
                    break;
                }
            }
            return distance;
        } else {
            return -1;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFieldValue(Object object, String name) throws Throwable {
        final Field field = object.getClass().getDeclaredField(name);
        if (!field.canAccess(object)) {
            field.setAccessible(true);
        }
        return (T) field.get(object);
    }

    /**
     * 对象转Map
     *
     * @param obj 对象
     * @return Map
     */
    public static Map<String, Object> toMap(Object obj) {
        if (obj == null) {
            return Collections.emptyMap();
        }
        Class<?> clazz = obj.getClass();
        Map<String, Object> map = new HashMap<>();
        for (Field field : clazz.getDeclaredFields()) {
            ReflectionUtils.makeAccessible(field);
            try {
                map.put(field.getName(), field.get(obj));
            } catch (IllegalAccessException ignore) {
            }
        }
        return map;
    }

    public static <T> void copyProperties(T source, Object target) {
        org.springframework.beans.BeanUtils.copyProperties(source, target);
    }

    public static <T> T toBean(Map<String, Object> map, Class<T> beanClass) {
        return BeanUtil.toBean(map, beanClass);
    }

    public static <T> T toBean(Object object, Class<T> beanClass) {
        return BeanUtil.toBean(object, beanClass);
    }

    public static <T> List<T> copyToList(Collection<?> collection, Class<T> targetType) {
        return BeanUtil.copyToList(collection, targetType);
    }
}
