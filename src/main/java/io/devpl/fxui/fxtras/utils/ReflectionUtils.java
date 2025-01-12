package io.devpl.fxui.fxtras.utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * This class is used to encapsulate operations for the handling of reflection.
 * @author manuel.mauky
 */
public class ReflectionUtils {

    /**
     * Returns all fields with the given annotation. Only fields that are declared in the actual class of the instance
     * are considered (i.e. no fields from super classes). This includes private fields.
     * @param target         the instance that's class is used to find annotations.
     * @param annotationType the type of the annotation that is searched for.
     * @return a List of Fields that are annotated with the given annotation.
     */
    public static List<Field> getFieldsWithAnnotation(Object target, Class<? extends Annotation> annotationType) {
        return ReflectionUtils.getFieldsFromClassHierarchy(target.getClass())
                              .stream()
                              .filter(field -> field.isAnnotationPresent(annotationType))
                              .collect(Collectors.toList());
    }

    /**
     * Returns all fields of the given type and all parent types (except Object).
     * <br>
     * The difference to {@link Class#getFields()} is that getFields only returns public fields while this method will
     * return all fields whatever the access modifier is.
     * <br>
     * <p>
     * The difference to {@link Class#getDeclaredFields()} is that getDeclaredFields returns all fields (with all
     * modifiers) from the given class but not from super classes. This method instead will return all fields of all
     * modifiers from all super classes up in the class hierarchy, except from Object itself.
     * @param type the type whose fields will be searched.
     * @return a list of field instances.
     */
    public static List<Field> getFieldsFromClassHierarchy(Class<?> type) {

        final List<Field> classFields = new ArrayList<>(Arrays.asList(type.getDeclaredFields()));
        final Class<?> parentClass = type.getSuperclass();

        if (parentClass != null && !(parentClass.equals(Object.class))) {
            List<Field> parentClassFields = getFieldsFromClassHierarchy(parentClass);
            classFields.addAll(parentClassFields);
        }
        return classFields;
    }

    /**
     * Helper method to execute a callback on a given member. This method encapsulates the error handling logic and the
     * handling of accessibility of the member.
     * <p>
     * After the callback is executed the accessibility of the member will be reset to the originally state.
     * @param member       the member that is made accessible to run the callback
     * @param callable     the callback that will be executed.
     * @param errorMessage the error message that is used in the exception when something went wrong.
     * @return the return value of the given callback.
     * @throws IllegalStateException when something went wrong.
     */
    public static <T> T accessMember(final AccessibleObject member, final Callable<T> callable, String errorMessage) {
        if (callable == null) {
            return null;
        }
        return AccessController.doPrivileged((PrivilegedAction<T>) () -> {
            boolean wasAccessible = member.isAccessible();
            try {
                member.setAccessible(true);
                return callable.call();
            } catch (Exception exception) {
                throw new IllegalStateException(errorMessage, exception);
            } finally {
                member.setAccessible(wasAccessible);
            }
        });
    }

    /**
     * This method can be used to set (private/public) fields to a given value by reflection. Handling of accessibility
     * and errors is encapsulated.
     * @param field  the field that's value should be set.
     * @param target the instance of which the field will be set.
     * @param value  the new value that the field should be set to.
     */
    public static void setField(final Field field, Object target, Object value) {
    }

    /**
     * Helper method to execute a callback on a given member. This method encapsulates the error handling logic and the
     * handling of accessibility of the member. The difference to
     * {@link ReflectionUtils#accessMember(AccessibleObject, Callable, String)} is that this method takes a callback that doesn't
     * return anything but only creates a sideeffect.
     * <p>
     * After the callback is executed the accessibility of the member will be reset to the originally state.
     * @param member       the member that is made accessible to run the callback
     * @param errorMessage the error message that is used in the exception when something went wrong.
     * @throws IllegalStateException when something went wrong.
     */
    public static void accessMember(final AccessibleObject member, String errorMessage) {
        AccessController.doPrivileged((PrivilegedAction<?>) () -> {
            boolean wasAccessible = member.isAccessible();
            try {
                member.setAccessible(true);
            } catch (Exception exception) {
                throw new IllegalStateException(errorMessage, exception);
            } finally {
                member.setAccessible(wasAccessible);
            }
            return null;
        });
    }

    public static <V> V getFieldValue(Object obj, String filedName, Class<V> valueType) {
        try {
            Field filed = findFiled(obj.getClass(), filedName);
            if (filed != null) {
                filed.setAccessible(true);
                return (V) filed.get(obj);
            }
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    public static <V> boolean setFieldValue(Object obj, String filedName, V newValue) {
        try {
            Field filed = findFiled(obj.getClass(), filedName);
            if (filed != null) {
                filed.setAccessible(true);
                filed.set(obj, newValue);
            }
            return true;
        } catch (IllegalAccessException e) {
            return false;
        }
    }

    public static Field findFiled(Class<?> clazz, String filedName) {
        Field field;
        try {
            field = clazz.getDeclaredField(filedName);
        } catch (NoSuchFieldException e) {
            try {
                field = clazz.getField(filedName);
            } catch (NoSuchFieldException ex) {
                return null;
            }
        }
        return field;
    }
}
