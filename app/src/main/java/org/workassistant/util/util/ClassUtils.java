package org.workassistant.util.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.URLUtil;
import org.workassistant.util.Assert;
import org.workassistant.util.StringUtils;
import org.workassistant.util.lang.RuntimeReflectiveOperationException;

import java.beans.Introspector;
import java.io.Closeable;
import java.io.Externalizable;
import java.io.Serializable;
import java.lang.reflect.*;
import java.net.URL;
import java.util.*;

/**
 * Miscellaneous {@code java.lang.Class} utility methods. Mainly for internal
 * use within the framework.
 *
 * @author Juergen Hoeller
 * @author Keith Donald
 * @author Rob Harrop
 * @author Sam Brannen
 * @see ReflectionUtils
 */
public abstract class ClassUtils {

    /**
     * Suffix for array class names: {@code "[]"}.
     */
    public static final String ARRAY_SUFFIX = "[]";
    /**
     * The CGLIB class separator: {@code "$$"}.
     */
    public static final String CGLIB_CLASS_SEPARATOR = "$$";
    /**
     * The ".class" file suffix.
     */
    public static final String CLASS_FILE_SUFFIX = ".class";
    /**
     * Prefix for internal array class names: {@code "["}.
     */
    private static final String INTERNAL_ARRAY_PREFIX = "[";
    /**
     * Prefix for internal non-primitive array class names: {@code "[L"}.
     */
    private static final String NON_PRIMITIVE_ARRAY_PREFIX = "[L";
    /**
     * A reusable empty class array constant.
     */
    private static final Class<?>[] EMPTY_CLASS_ARRAY = {};
    /**
     * The package separator character: {@code '.'}.
     */
    private static final char PACKAGE_SEPARATOR = '.';
    /**
     * The path separator character: {@code '/'}.
     */
    private static final char PATH_SEPARATOR = '/';
    /**
     * The nested class separator character: {@code '$'}.
     */
    private static final char NESTED_CLASS_SEPARATOR = '$';
    /**
     * Map with primitive wrapper type as key and corresponding primitive type as
     * value, for example: Integer.class -> int.class.
     */
    private static final Map<Class<?>, Class<?>> primitiveWrapperTypeMap = new IdentityHashMap<>(9);

    /**
     * Map with primitive type as key and corresponding wrapper type as value, for
     * example: int.class -> Integer.class.
     */
    private static final Map<Class<?>, Class<?>> primitiveTypeToWrapperMap = new IdentityHashMap<>(9);

    /**
     * Map with primitive type name as key and corresponding primitive type as
     * value, for example: "int" -> "int.class".
     */
    private static final Map<String, Class<?>> primitiveTypeNameMap = new HashMap<>(32);

    /**
     * Map with common Java language class name as key and corresponding Class as
     * value. Primarily for efficient deserialization of remote invocations.
     */
    private static final Map<String, Class<?>> commonClassCache = new HashMap<>(64);

    /**
     * Common Java language interfaces which are supposed to be ignored when
     * searching for 'primary' user-level interfaces.
     */
    private static final Set<Class<?>> javaLanguageInterfaces;

    /**
     * Cache for equivalent methods on an interface implemented by the declaring
     * class. org.springframework.util.ConcurrentReferenceHashMap<K, V>
     */
//	private static final Map<Method, Method> interfaceMethodCache = new ConcurrentReferenceHashMap<>(256);
    private static final Map<Method, Method> interfaceMethodCache = new HashMap<>(256);
    /**
     * 代理 class 的名称
     */
    private static final List<String> PROXY_CLASS_NAMES = java.util.Arrays.asList("net.sf.cglib.proxy.Factory"
        // cglib
        , "org.springframework.cglib.proxy.Factory", "javassist.util.proxy.ProxyObject"
        // javassist
        , "org.apache.ibatis.javassist.util.proxy.ProxyObject");

    static {
        primitiveWrapperTypeMap.put(Boolean.class, boolean.class);
        primitiveWrapperTypeMap.put(Byte.class, byte.class);
        primitiveWrapperTypeMap.put(Character.class, char.class);
        primitiveWrapperTypeMap.put(Double.class, double.class);
        primitiveWrapperTypeMap.put(Float.class, float.class);
        primitiveWrapperTypeMap.put(Integer.class, int.class);
        primitiveWrapperTypeMap.put(Long.class, long.class);
        primitiveWrapperTypeMap.put(Short.class, short.class);
        primitiveWrapperTypeMap.put(Void.class, void.class);

        // Map entry iteration is less expensive to initialize than forEach with lambdas
        for (Map.Entry<Class<?>, Class<?>> entry : primitiveWrapperTypeMap.entrySet()) {
            primitiveTypeToWrapperMap.put(entry.getValue(), entry.getKey());
            registerCommonClasses(entry.getKey());
        }

        Set<Class<?>> primitiveTypes = new HashSet<>(32);
        primitiveTypes.addAll(primitiveWrapperTypeMap.values());
        Collections.addAll(primitiveTypes, boolean[].class, byte[].class, char[].class, double[].class, float[].class, int[].class, long[].class, short[].class);
        for (Class<?> primitiveType : primitiveTypes) {
            primitiveTypeNameMap.put(primitiveType.getName(), primitiveType);
        }

        registerCommonClasses(Boolean[].class, Byte[].class, Character[].class, Double[].class, Float[].class, Integer[].class, Long[].class, Short[].class);
        registerCommonClasses(Number.class, Number[].class, String.class, String[].class, Class.class, Class[].class, Object.class, Object[].class);
        registerCommonClasses(Throwable.class, Exception.class, RuntimeException.class, Error.class, StackTraceElement.class, StackTraceElement[].class);
        registerCommonClasses(Enum.class, Iterable.class, Iterator.class, Enumeration.class, Collection.class, List.class, Set.class, Map.class, Map.Entry.class, Optional.class);

        Class<?>[] javaLanguageInterfaceArray = {Serializable.class, Externalizable.class, Closeable.class, AutoCloseable.class, Cloneable.class, Comparable.class};
        registerCommonClasses(javaLanguageInterfaceArray);
        javaLanguageInterfaces = new HashSet<>(java.util.Arrays.asList(javaLanguageInterfaceArray));
    }

    private ClassUtils() {
    }

    /**
     * Register the given common classes with the ClassUtils cache.
     */
    private static void registerCommonClasses(Class<?>... commonClasses) {
        for (Class<?> clazz : commonClasses) {
            commonClassCache.put(clazz.getName(), clazz);
        }
    }

    /**
     * Return the default ClassLoader to use: typically the thread context
     * ClassLoader, if available; the ClassLoader that loaded the ClassUtils class
     * will be used as fallback.
     * <p>
     * Call this method if you intend to use the thread context ClassLoader in a
     * scenario where you clearly prefer a non-null ClassLoader reference: for
     * example, for class path resource loading (but not necessarily for
     * {@code Class.forName}, which accepts a {@code null} ClassLoader reference as
     * well).
     *
     * @return the default ClassLoader (only {@code null} if even the system
     * ClassLoader isn't accessible)
     * @see Thread#getContextClassLoader()
     * @see ClassLoader#getSystemClassLoader()
     */

    public static ClassLoader getDefaultClassLoader() {
        ClassLoader cl = null;
        try {
            cl = Thread.currentThread().getContextClassLoader();
        } catch (Throwable ex) {
            // Cannot access thread context ClassLoader - falling back...
        }
        if (cl == null) {
            // No thread context class loader -> use class loader of this class.
            cl = ClassUtils.class.getClassLoader();
            if (cl == null) {
                // getClassLoader() returning null indicates the bootstrap ClassLoader
                try {
                    cl = ClassLoader.getSystemClassLoader();
                } catch (Throwable ex) {
                    // Cannot access system ClassLoader - oh well, maybe the caller can live with
                    // null...
                }
            }
        }
        return cl;
    }

    /**
     * Override the thread context ClassLoader with the environment's bean
     * ClassLoader if necessary, i.e. if the bean ClassLoader is not equivalent to
     * the thread context ClassLoader already.
     *
     * @param classLoaderToUse the actual ClassLoader to use for the thread context
     * @return the original thread context ClassLoader, or {@code null} if not
     * overridden
     */

    public static ClassLoader overrideThreadContextClassLoader(ClassLoader classLoaderToUse) {
        Thread currentThread = Thread.currentThread();
        ClassLoader threadContextClassLoader = currentThread.getContextClassLoader();
        if (classLoaderToUse != null && !classLoaderToUse.equals(threadContextClassLoader)) {
            currentThread.setContextClassLoader(classLoaderToUse);
            return threadContextClassLoader;
        } else {
            return null;
        }
    }

    /**
     * Replacement for {@code Class.forName()} that also returns Class instances for
     * primitives (e.g. "int") and array class names (e.g. "String[]"). Furthermore,
     * it is also capable of resolving nested class names in Java source style (e.g.
     * "java.lang.Thread.State" instead of "java.lang.Thread$State").
     *
     * @param name        the name of the Class
     * @param classLoader the class loader to use (maybe {@code null}, which
     *                    indicates the default class loader)
     * @return a class instance for the supplied name
     * @throws ClassNotFoundException if the class was not found
     * @throws LinkageError           if the class file could not be loaded
     * @see Class#forName(String, boolean, ClassLoader)
     */
    public static Class<?> forName(String name, ClassLoader classLoader) throws ClassNotFoundException, LinkageError {

        Assert.notNull(name, "Name must not be null");

        Class<?> clazz = resolvePrimitiveClassName(name);
        if (clazz == null) {
            clazz = commonClassCache.get(name);
        }
        if (clazz != null) {
            return clazz;
        }

        // "java.lang.String[]" style arrays
        if (name.endsWith(ARRAY_SUFFIX)) {
            String elementClassName = name.substring(0, name.length() - ARRAY_SUFFIX.length());
            Class<?> elementClass = forName(elementClassName, classLoader);
            return java.lang.reflect.Array.newInstance(elementClass, 0).getClass();
        }

        // "[Ljava.lang.String;" style arrays
        if (name.startsWith(NON_PRIMITIVE_ARRAY_PREFIX) && name.endsWith(";")) {
            String elementName = name.substring(NON_PRIMITIVE_ARRAY_PREFIX.length(), name.length() - 1);
            Class<?> elementClass = forName(elementName, classLoader);
            return java.lang.reflect.Array.newInstance(elementClass, 0).getClass();
        }

        // "[[I" or "[[Ljava.lang.String;" style arrays
        if (name.startsWith(INTERNAL_ARRAY_PREFIX)) {
            String elementName = name.substring(INTERNAL_ARRAY_PREFIX.length());
            Class<?> elementClass = forName(elementName, classLoader);
            return java.lang.reflect.Array.newInstance(elementClass, 0).getClass();
        }

        ClassLoader clToUse = classLoader;
        if (clToUse == null) {
            clToUse = getDefaultClassLoader();
        }
        try {
            return Class.forName(name, false, clToUse);
        } catch (ClassNotFoundException ex) {
            int lastDotIndex = name.lastIndexOf(PACKAGE_SEPARATOR);
            if (lastDotIndex != -1) {
                String nestedClassName = name.substring(0, lastDotIndex) + NESTED_CLASS_SEPARATOR + name.substring(lastDotIndex + 1);
                try {
                    return Class.forName(nestedClassName, false, clToUse);
                } catch (ClassNotFoundException ex2) {
                    // Swallow - let original exception get through
                }
            }
            throw ex;
        }
    }

    /**
     * Resolve the given class name into a Class instance. Supports primitives (like
     * "int") and array class names (like "String[]").
     * <p>
     * This is effectively equivalent to the {@code forName} method with the same
     * arguments, with the only difference being the exceptions thrown in case of
     * class loading failure.
     *
     * @param className   the name of the Class
     * @param classLoader the class loader to use (maybe {@code null}, which
     *                    indicates the default class loader)
     * @return a class instance for the supplied name
     * @throws IllegalArgumentException if the class name was not resolvable (that
     *                                  is, the class could not be found or the
     *                                  class file could not be loaded)
     * @throws IllegalStateException    if the corresponding class is resolvable but
     *                                  there was a readability mismatch in the
     *                                  inheritance hierarchy of the class
     *                                  (typically a missing dependency declaration
     *                                  in a Jigsaw module definition for a
     *                                  superclass or interface implemented by the
     *                                  class to be loaded here)
     * @see #forName(String, ClassLoader)
     */
    public static Class<?> resolveClassName(String className, ClassLoader classLoader) throws IllegalArgumentException {

        try {
            return forName(className, classLoader);
        } catch (IllegalAccessError err) {
            throw new IllegalStateException("Readability mismatch in inheritance hierarchy of class [" + className + "]: " + err.getMessage(), err);
        } catch (LinkageError err) {
            throw new IllegalArgumentException("Unresolvable class definition for class [" + className + "]", err);
        } catch (ClassNotFoundException ex) {
            throw new IllegalArgumentException("Could not find class [" + className + "]", ex);
        }
    }

    /**
     * Determine whether the {@link Class} identified by the supplied name is
     * present and can be loaded. Will return {@code false} if either the class or
     * one of its dependencies is not present or cannot be loaded.
     *
     * @param className   the name of the class to check
     * @param classLoader the class loader to use (maybe {@code null} which
     *                    indicates the default class loader)
     * @return whether the specified class is present (including all of its
     * superclasses and interfaces)
     * @throws IllegalStateException if the corresponding class is resolvable but
     *                               there was a readability mismatch in the
     *                               inheritance hierarchy of the class (typically a
     *                               missing dependency declaration in a Jigsaw
     *                               module definition for a superclass or interface
     *                               implemented by the class to be checked here)
     */
    public static boolean isPresent(String className, ClassLoader classLoader) {
        try {
            forName(className, classLoader);
            return true;
        } catch (IllegalAccessError err) {
            throw new IllegalStateException("Readability mismatch in inheritance hierarchy of class [" + className + "]: " + err.getMessage(), err);
        } catch (Throwable ex) {
            // Typically ClassNotFoundException or NoClassDefFoundError...
            return false;
        }
    }

    /**
     * Check whether the given class is visible in the given ClassLoader.
     *
     * @param clazz       the class to check (typically an interface)
     * @param classLoader the ClassLoader to check against (maybe {@code null} in
     *                    which case this method will always return {@code true})
     */
    public static boolean isVisible(Class<?> clazz, ClassLoader classLoader) {
        if (classLoader == null) {
            return true;
        }
        try {
            if (clazz.getClassLoader() == classLoader) {
                return true;
            }
        } catch (SecurityException ex) {
            // Fall through to loadable check below
        }

        // Visible if same Class can be loaded from given ClassLoader
        return isLoadable(clazz, classLoader);
    }

    /**
     * Check whether the given class is cache-safe in the given context, i.e.
     * whether it is loaded by the given ClassLoader or a parent of it.
     *
     * @param clazz       the class to analyze
     * @param classLoader the ClassLoader to potentially cache metadata in (maybe
     *                    {@code null} which indicates the system class loader)
     */
    public static boolean isCacheSafe(Class<?> clazz, ClassLoader classLoader) {
        Assert.notNull(clazz, "Class must not be null");
        try {
            ClassLoader target = clazz.getClassLoader();
            // Common cases
            if (target == classLoader || target == null) {
                return true;
            }
            if (classLoader == null) {
                return false;
            }
            // Check for match in ancestors -> positive
            ClassLoader current = classLoader;
            while (current != null) {
                current = current.getParent();
                if (current == target) {
                    return true;
                }
            }
            // Check for match in children -> negative
            while (target != null) {
                target = target.getParent();
                if (target == classLoader) {
                    return false;
                }
            }
        } catch (SecurityException ex) {
            // Fall through to loadable check below
        }

        // Fallback for ClassLoaders without parent/child relationship:
        // safe if same Class can be loaded from given ClassLoader
        return (classLoader != null && isLoadable(clazz, classLoader));
    }

    /**
     * Check whether the given class is loadable in the given ClassLoader.
     *
     * @param clazz       the class to check (typically an interface)
     * @param classLoader the ClassLoader to check against
     * @since 5.0.6
     */
    private static boolean isLoadable(Class<?> clazz, ClassLoader classLoader) {
        try {
            return (clazz == classLoader.loadClass(clazz.getName()));
            // Else: different class with same name found
        } catch (ClassNotFoundException ex) {
            // No corresponding class found at all
            return false;
        }
    }

    /**
     * Resolve the given class name as primitive class, if appropriate, according to
     * the JVM's naming rules for primitive classes.
     * <p>
     * Also supports the JVM's internal class names for primitive arrays. Does
     * <i>not</i> support the "[]" suffix notation for primitive arrays; this is
     * only supported by {@link #forName(String, ClassLoader)}.
     *
     * @param name the name of the potentially primitive class
     * @return the primitive class, or {@code null} if the name does not denote a
     * primitive class or primitive array class
     */

    public static Class<?> resolvePrimitiveClassName(String name) {
        Class<?> result = null;
        // Most class names will be quite long, considering that they
        // SHOULD sit in a package, so a length check is worthwhile.
        if (name != null && name.length() <= 7) {
            // Could be a primitive - likely.
            result = primitiveTypeNameMap.get(name);
        }
        return result;
    }

    /**
     * Check if the given class represents a primitive wrapper, i.e. Boolean, Byte,
     * Character, Short, Integer, Long, Float, Double, or Void.
     *
     * @param clazz the class to check
     * @return whether the given class is a primitive wrapper class
     */
    public static boolean isPrimitiveWrapper(Class<?> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        return primitiveWrapperTypeMap.containsKey(clazz);
    }

    /**
     * Check if the given class represents a primitive (i.e. boolean, byte, char,
     * short, int, long, float, or double), {@code void}, or a wrapper for those
     * types (i.e. Boolean, Byte, Character, Short, Integer, Long, Float, Double, or
     * Void).
     *
     * @param clazz the class to check
     * @return {@code true} if the given class represents a primitive, void, or a
     * wrapper class
     */
    public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        return (clazz.isPrimitive() || isPrimitiveWrapper(clazz));
    }

    /**
     * Check if the given class represents an array of primitives, i.e. boolean,
     * byte, char, short, int, long, float, or double.
     *
     * @param clazz the class to check
     * @return whether the given class is a primitive array class
     */
    public static boolean isPrimitiveArray(Class<?> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        return (clazz.isArray() && clazz.getComponentType().isPrimitive());
    }

    /**
     * Check if the given class represents an array of primitive wrappers, i.e.
     * Boolean, Byte, Character, Short, Integer, Long, Float, or Double.
     *
     * @param clazz the class to check
     * @return whether the given class is a primitive wrapper array class
     */
    public static boolean isPrimitiveWrapperArray(Class<?> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        return (clazz.isArray() && isPrimitiveWrapper(clazz.getComponentType()));
    }

    /**
     * Resolve the given class if it is a primitive class, returning the
     * corresponding primitive wrapper type instead.
     *
     * @param clazz the class to check
     * @return the original class, or a primitive wrapper for the original primitive
     * type
     */
    public static Class<?> resolvePrimitiveIfNecessary(Class<?> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        return (clazz.isPrimitive() && clazz != void.class ? primitiveTypeToWrapperMap.get(clazz) : clazz);
    }

    /**
     * Check if the right-hand side type may be assigned to the left-hand side type,
     * assuming setting by reflection. Considers primitive wrapper classes as
     * assignable to the corresponding primitive types.
     *
     * @param lhsType the target type
     * @param rhsType the value type that should be assigned to the target type
     * @return if the target type is assignable from the value type
     * @see // TypeUtils#isAssignable(java.lang.reflect.AnyType, java.lang.reflect.AnyType)
     */
    public static boolean isAssignable(Class<?> lhsType, Class<?> rhsType) {
        Assert.notNull(lhsType, "Left-hand side type must not be null");
        Assert.notNull(rhsType, "Right-hand side type must not be null");
        if (lhsType.isAssignableFrom(rhsType)) {
            return true;
        }
        if (lhsType.isPrimitive()) {
            Class<?> resolvedPrimitive = primitiveWrapperTypeMap.get(rhsType);
            return (lhsType == resolvedPrimitive);
        } else {
            Class<?> resolvedWrapper = primitiveTypeToWrapperMap.get(rhsType);
            return (resolvedWrapper != null && lhsType.isAssignableFrom(resolvedWrapper));
        }
    }

    /**
     * Determine if the given type is assignable from the given value, assuming
     * setting by reflection. Considers primitive wrapper classes as assignable to
     * the corresponding primitive types.
     *
     * @param type  the target type
     * @param value the value that should be assigned to the type
     * @return if the type is assignable from the value
     */
    public static boolean isAssignableValue(Class<?> type, Object value) {
        Assert.notNull(type, "AnyType must not be null");
        return (value != null ? isAssignable(type, value.getClass()) : !type.isPrimitive());
    }

    /**
     * Convert a "/"-based resource path to a "."-based fully qualified class name.
     *
     * @param resourcePath the resource path pointing to a class
     * @return the corresponding fully qualified class name
     */
    public static String convertResourcePathToClassName(String resourcePath) {
        Assert.notNull(resourcePath, "Resource path must not be null");
        return resourcePath.replace(PATH_SEPARATOR, PACKAGE_SEPARATOR);
    }

    /**
     * Convert a "."-based fully qualified class name to a "/"-based resource path.
     *
     * @param className the fully qualified class name
     * @return the corresponding resource path, pointing to the class
     */
    public static String convertClassNameToResourcePath(String className) {
        Assert.notNull(className, "Class name must not be null");
        return className.replace(PACKAGE_SEPARATOR, PATH_SEPARATOR);
    }

    /**
     * Return a path suitable for use with {@code ClassLoader.getResource} (also
     * suitable for use with {@code Class.getResource} by prepending a slash ('/')
     * to the return value). Built by taking the package of the specified class
     * file, converting all dots ('.') to slashes ('/'), adding a trailing slash if
     * necessary, and concatenating the specified resource name to this. <br/>
     * As such, this function may be used to build a path suitable for loading a
     * resource file that is in the same package as a class file
     *
     * @param clazz        the Class whose package will be used as the base
     * @param resourceName the resource name to append. A leading slash is optional.
     * @return the built-up resource path
     * @see ClassLoader#getResource
     * @see Class#getResource
     */
    public static String addResourcePathToPackagePath(Class<?> clazz, String resourceName) {
        Assert.notNull(resourceName, "Resource name must not be null");
        if (!resourceName.startsWith("/")) {
            return classPackageAsResourcePath(clazz) + '/' + resourceName;
        }
        return classPackageAsResourcePath(clazz) + resourceName;
    }

    /**
     * Given an input class object, return a string which consists of the class's
     * package name as a pathname, i.e., all dots ('.') are replaced by slashes
     * ('/'). Neither a leading nor trailing slash is added. The result could be
     * concatenated with a slash and the name of a resource and fed directly to
     * {@code ClassLoader.getResource()}. For it to be fed to
     * {@code Class.getResource} instead, a leading slash would also have to be
     * prepended to the returned value.
     *
     * @param clazz the input class. A {@code null} value or the default (empty)
     *              package will result in an empty string ("") being returned.
     * @return a path which represents the package name
     * @see ClassLoader#getResource
     * @see Class#getResource
     */
    public static String classPackageAsResourcePath(Class<?> clazz) {
        if (clazz == null) {
            return "";
        }
        String className = clazz.getName();
        int packageEndIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
        if (packageEndIndex == -1) {
            return "";
        }
        String packageName = className.substring(0, packageEndIndex);
        return packageName.replace(PACKAGE_SEPARATOR, PATH_SEPARATOR);
    }

    /**
     * Build a String that consists of the names of the classes/interfaces in the
     * given array.
     * <p>
     * Basically like {@code AbstractCollection.toString()}, but stripping the
     * "class "/"interface " prefix before every class name.
     *
     * @param classes an array of Class objects
     * @return a String of form "[com.foo.Bar, com.foo.Baz]"
     * @see java.util.AbstractCollection#toString()
     */
    public static String classNamesToString(Class<?>... classes) {
        return classNamesToString(ArrayUtils.asArrayList(classes));
    }

    /**
     * Build a String that consists of the names of the classes/interfaces in the
     * given collection.
     * <p>
     * Basically like {@code AbstractCollection.toString()}, but stripping the
     * "class "/"interface " prefix before every class name.
     *
     * @param classes a Collection of Class objects (maybe {@code null})
     * @return a String of form "[com.foo.Bar, com.foo.Baz]"
     * @see java.util.AbstractCollection#toString()
     */
    public static String classNamesToString(Collection<Class<?>> classes) {
        if (CollectionUtils.isEmpty(classes)) {
            return "[]";
        }
        StringJoiner stringJoiner = new StringJoiner(", ", "[", "]");
        for (Class<?> clazz : classes) {
            stringJoiner.add(clazz.getName());
        }
        return stringJoiner.toString();
    }

    /**
     * Copy the given {@code Collection} into a {@code Class} array.
     * <p>
     * The {@code Collection} must contain {@code Class} elements only.
     *
     * @param collection the {@code Collection} to copy
     * @return the {@code Class} array
     * @see StringUtils#toStringArray
     * @since 3.1
     */
    public static Class<?>[] toClassArray(Collection<Class<?>> collection) {
        return (!CollectionUtils.isEmpty(collection) ? collection.toArray(EMPTY_CLASS_ARRAY) : EMPTY_CLASS_ARRAY);
    }

    /**
     * Return all interfaces that the given instance implements as an array,
     * including ones implemented by superclasses.
     *
     * @param instance the instance to analyze for interfaces
     * @return all interfaces that the given instance implements as an array
     */
    public static Class<?>[] getAllInterfaces(Object instance) {
        Assert.notNull(instance, "Instance must not be null");
        return getAllInterfacesForClass(instance.getClass());
    }

    /**
     * Return all interfaces that the given class implements as an array, including
     * ones implemented by superclasses.
     * <p>
     * If the class itself is an interface, it gets returned as sole interface.
     *
     * @param clazz the class to analyze for interfaces
     * @return all interfaces that the given object implements as an array
     */
    public static Class<?>[] getAllInterfacesForClass(Class<?> clazz) {
        return getAllInterfacesForClass(clazz, null);
    }

    /**
     * Return all interfaces that the given class implements as an array, including
     * ones implemented by superclasses.
     * <p>
     * If the class itself is an interface, it gets returned as sole interface.
     *
     * @param clazz       the class to analyze for interfaces
     * @param classLoader the ClassLoader that the interfaces need to be visible in
     *                    (maybe {@code null} when accepting all declared
     *                    interfaces)
     * @return all interfaces that the given object implements as an array
     */
    public static Class<?>[] getAllInterfacesForClass(Class<?> clazz, ClassLoader classLoader) {
        return toClassArray(getAllInterfacesForClassAsSet(clazz, classLoader));
    }

    /**
     * Return all interfaces that the given instance implements as a Set, including
     * ones implemented by superclasses.
     *
     * @param instance the instance to analyze for interfaces
     * @return all interfaces that the given instance implements as a Set
     */
    public static Set<Class<?>> getAllInterfacesAsSet(Object instance) {
        Assert.notNull(instance, "Instance must not be null");
        return getAllInterfacesForClassAsSet(instance.getClass());
    }

    /**
     * Return all interfaces that the given class implements as a Set, including
     * ones implemented by superclasses.
     * <p>
     * If the class itself is an interface, it gets returned as sole interface.
     *
     * @param clazz the class to analyze for interfaces
     * @return all interfaces that the given object implements as a Set
     */
    public static Set<Class<?>> getAllInterfacesForClassAsSet(Class<?> clazz) {
        return getAllInterfacesForClassAsSet(clazz, null);
    }

    /**
     * Return all interfaces that the given class implements as a Set, including
     * ones implemented by superclasses.
     * <p>
     * If the class itself is an interface, it gets returned as sole interface.
     *
     * @param clazz       the class to analyze for interfaces
     * @param classLoader the ClassLoader that the interfaces need to be visible in
     *                    (maybe {@code null} when accepting all declared
     *                    interfaces)
     * @return all interfaces that the given object implements as a Set
     */
    public static Set<Class<?>> getAllInterfacesForClassAsSet(Class<?> clazz, ClassLoader classLoader) {
        Assert.notNull(clazz, "Class must not be null");
        if (clazz.isInterface() && isVisible(clazz, classLoader)) {
            return Collections.singleton(clazz);
        }
        Set<Class<?>> interfaces = new LinkedHashSet<>();
        Class<?> current = clazz;
        while (current != null) {
            Class<?>[] ifcs = current.getInterfaces();
            for (Class<?> ifc : ifcs) {
                if (isVisible(ifc, classLoader)) {
                    interfaces.add(ifc);
                }
            }
            current = current.getSuperclass();
        }
        return interfaces;
    }

    /**
     * Create a composite interface Class for the given interfaces, implementing the
     * given interfaces in one single Class.
     * <p>
     * This implementation builds a JDK proxy class for the given interfaces.
     *
     * @param interfaces  the interfaces to merge
     * @param classLoader the ClassLoader to create the composite Class in
     * @return the merged interface as Class
     * @throws IllegalArgumentException if the specified interfaces expose
     *                                  conflicting method signatures (or a similar
     *                                  constraint is violated)
     * @see Proxy#getProxyClass
     */
    @SuppressWarnings("deprecation") // on JDK 9
    public static Class<?> createCompositeInterface(Class<?>[] interfaces, ClassLoader classLoader) {
        Assert.notEmpty(interfaces, "Interface array must not be empty");
        return Proxy.getProxyClass(classLoader, interfaces);
    }

    /**
     * Determine the common ancestor of the given classes, if any.
     *
     * @param clazz1 the class to introspect
     * @param clazz2 the other class to introspect
     * @return the common ancestor (i.e. common superclass, one interface extending
     * the other), or {@code null} if none found. If any of the given
     * classes is {@code null}, the other class will be returned.
     * @since 3.2.6
     */

    public static Class<?> determineCommonAncestor(Class<?> clazz1, Class<?> clazz2) {
        if (clazz1 == null) {
            return clazz2;
        }
        if (clazz2 == null) {
            return clazz1;
        }
        if (clazz1.isAssignableFrom(clazz2)) {
            return clazz1;
        }
        if (clazz2.isAssignableFrom(clazz1)) {
            return clazz2;
        }
        Class<?> ancestor = clazz1;
        do {
            ancestor = ancestor.getSuperclass();
            if (ancestor == null || Object.class == ancestor) {
                return null;
            }
        } while (!ancestor.isAssignableFrom(clazz2));
        return ancestor;
    }

    /**
     * Determine whether the given interface is a common Java language interface:
     * {@link Serializable}, {@link Externalizable}, {@link Closeable},
     * {@link AutoCloseable}, {@link Cloneable}, {@link Comparable} - all of which
     * can be ignored when looking for 'primary' user-level interfaces. Common
     * characteristics: no service-level operations, no bean property methods, no
     * default methods.
     *
     * @param ifc the interface to check
     * @since 5.0.3
     */
    public static boolean isJavaLanguageInterface(Class<?> ifc) {
        return javaLanguageInterfaces.contains(ifc);
    }

    /**
     * Determine if the supplied class is an <em>inner class</em>, i.e. a non-static
     * member of an enclosing class.
     *
     * @return {@code true} if the supplied class is an inner class
     * @see Class#isMemberClass()
     * @since 5.0.5
     */
    public static boolean isInnerClass(Class<?> clazz) {
        return (clazz.isMemberClass() && !Modifier.isStatic(clazz.getModifiers()));
    }

    /**
     * Check whether the given object is a CGLIB proxy.
     *
     * @param object the object to check
     * @see #isCglibProxyClass(Class)
     * @deprecated as of 5.2, in favor of custom (possibly narrower) checks
     */
    @Deprecated
    public static boolean isCglibProxy(Object object) {
        return isCglibProxyClass(object.getClass());
    }

    /**
     * Check whether the specified class is a CGLIB-generated class.
     *
     * @param clazz the class to check
     * @see #isCglibProxyClassName(String)
     * @deprecated as of 5.2, in favor of custom (possibly narrower) checks
     */
    @Deprecated
    public static boolean isCglibProxyClass(Class<?> clazz) {
        return (clazz != null && isCglibProxyClassName(clazz.getName()));
    }

    /**
     * Check whether the specified class name is a CGLIB-generated class.
     *
     * @param className the class name to check
     * @deprecated as of 5.2, in favor of custom (possibly narrower) checks
     */
    @Deprecated
    public static boolean isCglibProxyClassName(String className) {
        return (className != null && className.contains(CGLIB_CLASS_SEPARATOR));
    }

    /**
     * Return the user-defined class for the given instance: usually simply the
     * class of the given instance, but the original class in case of a
     * CGLIB-generated subclass.
     *
     * @param instance the instance to check
     * @return the user-defined class
     */
    public static Class<?> getUserClass(Object instance) {
        Assert.notNull(instance, "Instance must not be null");
        return getUserClass(instance.getClass());
    }

    /**
     * Return the user-defined class for the given class: usually simply the given
     * class, but the original class in case of a CGLIB-generated subclass.
     *
     * @param clazz the class to check
     * @return the user-defined class
     */
    public static Class<?> getUserClass(Class<?> clazz) {
        if (clazz.getName().contains(CGLIB_CLASS_SEPARATOR)) {
            Class<?> superclass = clazz.getSuperclass();
            if (superclass != null && superclass != Object.class) {
                return superclass;
            }
        }
        return clazz;
    }

    /**
     * Return a descriptive name for the given object's type: usually simply the
     * class name, but component type class name + "[]" for arrays, and an appended
     * list of implemented interfaces for JDK proxies.
     *
     * @param value the value to introspect
     * @return the qualified name of the class
     */

    public static String getDescriptiveType(Object value) {
        if (value == null) {
            return null;
        }
        Class<?> clazz = value.getClass();
        if (Proxy.isProxyClass(clazz)) {
            String prefix = clazz.getName() + " implementing ";
            StringJoiner result = new StringJoiner(",", prefix, "");
            for (Class<?> ifc : clazz.getInterfaces()) {
                result.add(ifc.getName());
            }
            return result.toString();
        } else {
            return clazz.getTypeName();
        }
    }

    /**
     * Check whether the given class matches the user-specified type name.
     *
     * @param clazz    the class to check
     * @param typeName the type name to match
     */
    public static boolean matchesTypeName(Class<?> clazz, String typeName) {
        return (typeName != null && (typeName.equals(clazz.getTypeName()) || typeName.equals(clazz.getSimpleName())));
    }

    /**
     * Get the class name without the qualified package name.
     *
     * @param className the className to get the short name for
     * @return the class name of the class without the package name
     * @throws IllegalArgumentException if the className is empty
     */
    public static String getShortName(String className) {
        Assert.hasLength(className, "Class name must not be empty");
        int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
        int nameEndIndex = className.indexOf(CGLIB_CLASS_SEPARATOR);
        if (nameEndIndex == -1) {
            nameEndIndex = className.length();
        }
        String shortName = className.substring(lastDotIndex + 1, nameEndIndex);
        shortName = shortName.replace(NESTED_CLASS_SEPARATOR, PACKAGE_SEPARATOR);
        return shortName;
    }

    /**
     * Get the class name without the qualified package name.
     *
     * @param clazz the class to get the short name for
     * @return the class name of the class without the package name
     */
    public static String getShortName(Class<?> clazz) {
        return getShortName(getQualifiedName(clazz));
    }

    /**
     * Return the short string name of a Java class in un capitalized JavaBeans
     * property format. Strips the outer class name in case of a nested class.
     *
     * @param clazz the class
     * @return the short name rendered in a standard JavaBeans property format
     * @see Introspector#decapitalize(String)
     */
    public static String getShortNameAsProperty(Class<?> clazz) {
        String shortName = getShortName(clazz);
        int dotIndex = shortName.lastIndexOf(PACKAGE_SEPARATOR);
        shortName = (dotIndex != -1 ? shortName.substring(dotIndex + 1) : shortName);
        return Introspector.decapitalize(shortName);
    }

    /**
     * Determine the name of the class file, relative to the containing package:
     * e.g. "String.class"
     *
     * @param clazz the class
     * @return the file name of the ".class" file
     */
    public static String getClassFileName(Class<?> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        String className = clazz.getName();
        int lastDotIndex = className.lastIndexOf(PACKAGE_SEPARATOR);
        return className.substring(lastDotIndex + 1) + CLASS_FILE_SUFFIX;
    }

    /**
     * Determine the name of the package of the given class, e.g. "java.lang" for
     * the {@code java.lang.String} class.
     *
     * @param clazz the class
     * @return the package name, or the empty String if the class is defined in the
     * default package
     */
    public static String getPackageName(Class<?> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        return getPackageName(clazz.getName());
    }

    /**
     * Determine the name of the package of the given fully-qualified class name,
     * e.g. "java.lang" for the {@code java.lang.String} class name.
     *
     * @param fqClassName the fully-qualified class name
     * @return the package name, or the empty String if the class is defined in the
     * default package
     */
    public static String getPackageName(String fqClassName) {
        Assert.notNull(fqClassName, "Class name must not be null");
        int lastDotIndex = fqClassName.lastIndexOf(PACKAGE_SEPARATOR);
        return (lastDotIndex != -1 ? fqClassName.substring(0, lastDotIndex) : "");
    }

    /**
     * Return the qualified name of the given class: usually simply the class name,
     * but component type class name + "[]" for arrays.
     *
     * @param clazz the class
     * @return the qualified name of the class
     */
    public static String getQualifiedName(Class<?> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        return clazz.getTypeName();
    }

    /**
     * Return the qualified name of the given method, consisting of fully qualified
     * interface/class name + "." + method name.
     *
     * @param method the method
     * @return the qualified name of the method
     */
    public static String getQualifiedMethodName(Method method) {
        return getQualifiedMethodName(method, null);
    }

    /**
     * Return the qualified name of the given method, consisting of fully qualified
     * interface/class name + "." + method name.
     *
     * @param method the method
     * @param clazz  the clazz that the method is being invoked on (maybe
     *               {@code null} to indicate the method's declaring class)
     * @return the qualified name of the method
     * @since 4.3.4
     */
    public static String getQualifiedMethodName(Method method, Class<?> clazz) {
        Assert.notNull(method, "Method must not be null");
        return (clazz != null ? clazz : method.getDeclaringClass()).getName() + '.' + method.getName();
    }

    /**
     * Determine whether the given class has a public constructor with the given
     * signature.
     * <p>
     * Essentially translates {@code NoSuchMethodException} to "false".
     *
     * @param clazz      the clazz to analyze
     * @param paramTypes the parameter types of the method
     * @return whether the class has a corresponding constructor
     * @see Class#getConstructor
     */
    public static boolean hasConstructor(Class<?> clazz, Class<?>... paramTypes) {
        return (getConstructorIfAvailable(clazz, paramTypes) != null);
    }

    /**
     * Determine whether the given class has a public constructor with the given
     * signature, and return it if available (else return {@code null}).
     * <p>
     * Essentially translates {@code NoSuchMethodException} to {@code null}.
     *
     * @param clazz      the clazz to analyze
     * @param paramTypes the parameter types of the method
     * @return the constructor, or {@code null} if not found
     * @see Class#getConstructor
     */

    public static <T> Constructor<T> getConstructorIfAvailable(Class<T> clazz, Class<?>... paramTypes) {
        Assert.notNull(clazz, "Class must not be null");
        try {
            return clazz.getConstructor(paramTypes);
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    /**
     * Determine whether the given class has a public method with the given
     * signature.
     *
     * @param clazz  the clazz to analyze
     * @param method the method to look for
     * @return whether the class has a corresponding method
     * @since 5.2.3
     */
    public static boolean hasMethod(Class<?> clazz, Method method) {
        Assert.notNull(clazz, "Class must not be null");
        Assert.notNull(method, "Method must not be null");
        if (clazz == method.getDeclaringClass()) {
            return true;
        }
        String methodName = method.getName();
        Class<?>[] paramTypes = method.getParameterTypes();
        return getMethodOrNull(clazz, methodName, paramTypes) != null;
    }

    /**
     * Determine whether the given class has a public method with the given
     * signature.
     * <p>
     * Essentially translates {@code NoSuchMethodException} to "false".
     *
     * @param clazz      the clazz to analyze
     * @param methodName the name of the method
     * @param paramTypes the parameter types of the method
     * @return whether the class has a corresponding method
     * @see Class#getMethod
     */
    public static boolean hasMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        return (getMethodIfAvailable(clazz, methodName, paramTypes) != null);
    }

    /**
     * Determine whether the given class has a public method with the given
     * signature, and return it if available (else throws an
     * {@code IllegalStateException}).
     * <p>
     * In case of any signature specified, only returns the method if there is a
     * unique candidate, i.e. a single public method with the specified name.
     * <p>
     * Essentially translates {@code NoSuchMethodException} to
     * {@code IllegalStateException}.
     *
     * @param clazz      the clazz to analyze
     * @param methodName the name of the method
     * @param paramTypes the parameter types of the method (maybe {@code null} to
     *                   indicate any signature)
     * @return the method (never {@code null})
     * @throws IllegalStateException if the method has not been found
     * @see Class#getMethod
     */
    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        Assert.notNull(clazz, "Class must not be null");
        Assert.notNull(methodName, "Method name must not be null");
        if (paramTypes != null) {
            try {
                return clazz.getMethod(methodName, paramTypes);
            } catch (NoSuchMethodException ex) {
                throw new IllegalStateException("Expected method not found: " + ex);
            }
        } else {
            Set<Method> candidates = findMethodCandidatesByName(clazz, methodName);
            if (candidates.size() == 1) {
                return candidates.iterator().next();
            } else if (candidates.isEmpty()) {
                throw new IllegalStateException("Expected method not found: " + clazz.getName() + '.' + methodName);
            } else {
                throw new IllegalStateException("No unique method found: " + clazz.getName() + '.' + methodName);
            }
        }
    }

    /**
     * Determine whether the given class has a public method with the given
     * signature, and return it if available (else return {@code null}).
     * <p>
     * In case of any signature specified, only returns the method if there is a
     * unique candidate, i.e. a single public method with the specified name.
     * <p>
     * Essentially translates {@code NoSuchMethodException} to {@code null}.
     *
     * @param clazz      the clazz to analyze
     * @param methodName the name of the method
     * @param paramTypes the parameter types of the method (maybe {@code null} to
     *                   indicate any signature)
     * @return the method, or {@code null} if not found
     * @see Class#getMethod
     */

    public static Method getMethodIfAvailable(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        Assert.notNull(clazz, "Class must not be null");
        Assert.notNull(methodName, "Method name must not be null");
        if (paramTypes != null) {
            return getMethodOrNull(clazz, methodName, paramTypes);
        } else {
            Set<Method> candidates = findMethodCandidatesByName(clazz, methodName);
            if (candidates.size() == 1) {
                return candidates.iterator().next();
            }
            return null;
        }
    }

    /**
     * Return the number of methods with a given name (with any argument types), for
     * the given class and/or its superclasses. Includes non-public methods.
     *
     * @param clazz      the clazz to check
     * @param methodName the name of the method
     * @return the number of methods with the given name
     */
    public static int getMethodCountForName(Class<?> clazz, String methodName) {
        Assert.notNull(clazz, "Class must not be null");
        Assert.notNull(methodName, "Method name must not be null");
        int count = 0;
        Method[] declaredMethods = clazz.getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (methodName.equals(method.getName())) {
                count++;
            }
        }
        Class<?>[] ifcs = clazz.getInterfaces();
        for (Class<?> ifc : ifcs) {
            count += getMethodCountForName(ifc, methodName);
        }
        if (clazz.getSuperclass() != null) {
            count += getMethodCountForName(clazz.getSuperclass(), methodName);
        }
        return count;
    }

    /**
     * Does the given class or one of its superclasses at least have one or more
     * methods with the supplied name (with any argument types)? Includes non-public
     * methods.
     *
     * @param clazz      the clazz to check
     * @param methodName the name of the method
     * @return whether there is at least one method with the given name
     */
    public static boolean hasAtLeastOneMethodWithName(Class<?> clazz, String methodName) {
        Assert.notNull(clazz, "Class must not be null");
        Assert.notNull(methodName, "Method name must not be null");
        Method[] declaredMethods = clazz.getDeclaredMethods();
        for (Method method : declaredMethods) {
            if (method.getName().equals(methodName)) {
                return true;
            }
        }
        Class<?>[] ifcs = clazz.getInterfaces();
        for (Class<?> ifc : ifcs) {
            if (hasAtLeastOneMethodWithName(ifc, methodName)) {
                return true;
            }
        }
        return (clazz.getSuperclass() != null && hasAtLeastOneMethodWithName(clazz.getSuperclass(), methodName));
    }

    /**
     * Given a method, which may come from an interface, and a target class used in
     * the current reflective invocation, find the corresponding target method if
     * there is one. E.g. the method may be {@code IFoo.bar()} and the target class
     * may be {@code DefaultFoo}. In this case, the method may be
     * {@code DefaultFoo.bar()}. This enables attributes on that method to be found.
     * <p>
     * <b>NOTE:</b> In contrast to org.springframework.aop.support.AopUtils#getMostSpecificMethod, this
     * method does <i>not</i> resolve Java 5 bridge methods automatically. Call
     * org.springframework.core.BridgeMethodResolver#findBridgedMethod if
     * bridge method resolution is desirable (e.g. for obtaining metadata from the
     * original method definition).
     * <p>
     * <b>NOTE:</b> Since Spring 3.1.1, if Java security settings disallow
     * reflective access (e.g. calls to {@code Class#getDeclaredMethods} etc., this
     * implementation will fall back to returning the originally provided method.
     *
     * @param method      the method to be invoked, which may come from an interface
     * @param targetClass the target class for the current invocation (maybe
     *                    {@code null} or may not even implement the method)
     * @return the specific target method, or the original method if the
     * {@code targetClass} does not implement it
     * @see #getInterfaceMethodIfPossible(Method, Class)
     */
    public static Method getMostSpecificMethod(Method method, Class<?> targetClass) {
        if (targetClass != null && targetClass != method.getDeclaringClass() && isOverridable(method, targetClass)) {
            try {
                if (Modifier.isPublic(method.getModifiers())) {
                    try {
                        return targetClass.getMethod(method.getName(), method.getParameterTypes());
                    } catch (NoSuchMethodException ex) {
                        return method;
                    }
                } else {
                    Method specificMethod = ReflectionUtils.findMethod(targetClass, method.getName(), method.getParameterTypes());
                    return (specificMethod != null ? specificMethod : method);
                }
            } catch (SecurityException ex) {
                // Security settings are disallowing reflective access; fall back to 'method'
                // below.
            }
        }
        return method;
    }

    /**
     * Determine a corresponding interface method for the given method handle, if
     * possible.
     * <p>
     * This is particularly useful for arriving at a public exported type on Jigsaw
     * which can be reflectively invoked without an illegal access warning.
     *
     * @param method the method to be invoked, potentially from an implementation
     *               class
     * @return the corresponding interface method, or the original method if none
     * found
     * @since 5.1
     * @deprecated in favor of {@link #getInterfaceMethodIfPossible(Method, Class)}
     */
    @Deprecated
    public static Method getInterfaceMethodIfPossible(Method method) {
        return getInterfaceMethodIfPossible(method, null);
    }

    /**
     * Determine a corresponding interface method for the given method handle, if
     * possible.
     * <p>
     * This is particularly useful for arriving at a public exported type on Jigsaw
     * which can be reflectively invoked without an illegal access warning.
     *
     * @param method      the method to be invoked, potentially from an
     *                    implementation class
     * @param targetClass the target class to check for declared interfaces
     * @return the corresponding interface method, or the original method if none
     * found
     * @see #getMostSpecificMethod
     * @since 5.3.16
     */
    public static Method getInterfaceMethodIfPossible(Method method, Class<?> targetClass) {
        if (!Modifier.isPublic(method.getModifiers()) || method.getDeclaringClass().isInterface()) {
            return method;
        }
        // Try cached version of method in its declaring class
        Method result = interfaceMethodCache.computeIfAbsent(method, key -> findInterfaceMethodIfPossible(key, key.getDeclaringClass(), Object.class));
        if (result == method && targetClass != null) {
            // No interface method found yet -> try given target class (possibly a subclass
            // of the
            // declaring class, late-binding a base class method to a subclass-declared
            // interface:
            // see e.g. HashMap.HashIterator.hasNext)
            result = findInterfaceMethodIfPossible(method, targetClass, method.getDeclaringClass());
        }
        return result;
    }

    private static Method findInterfaceMethodIfPossible(Method method, Class<?> startClass, Class<?> endClass) {
        Class<?> current = startClass;
        while (current != null && current != endClass) {
            Class<?>[] ifcs = current.getInterfaces();
            for (Class<?> ifc : ifcs) {
                try {
                    return ifc.getMethod(method.getName(), method.getParameterTypes());
                } catch (NoSuchMethodException ex) {
                    // ignore
                }
            }
            current = current.getSuperclass();
        }
        return method;
    }

    /**
     * Determine whether the given method is declared by the user or at least
     * pointing to a user-declared method.
     * <p>
     * Checks {@link Method#isSynthetic()} (for implementation methods) as well as
     * the {@code GroovyObject} interface (for interface methods; on an
     * implementation class, implementations of the {@code GroovyObject} methods
     * will be marked as synthetic anyway). Note that, despite being synthetic,
     * bridge methods ({@link Method#isBridge()}) are considered as user-level
     * methods since they are eventually pointing to a user-declared generic method.
     *
     * @param method the method to check
     * @return {@code true} if the method can be considered as user-declared;
     * {@code false} otherwise
     */
    public static boolean isUserLevelMethod(Method method) {
        Assert.notNull(method, "Method must not be null");
        return (method.isBridge() || (!method.isSynthetic() && !isGroovyObjectMethod(method)));
    }

    private static boolean isGroovyObjectMethod(Method method) {
        return method.getDeclaringClass().getName().equals("groovy.lang.GroovyObject");
    }

    /**
     * Determine whether the given method is overridable in the given target class.
     *
     * @param method      the method to check
     * @param targetClass the target class to check against
     */
    private static boolean isOverridable(Method method, Class<?> targetClass) {
        if (Modifier.isPrivate(method.getModifiers())) {
            return false;
        }
        if (Modifier.isPublic(method.getModifiers()) || Modifier.isProtected(method.getModifiers())) {
            return true;
        }
        return (targetClass == null || getPackageName(method.getDeclaringClass()).equals(getPackageName(targetClass)));
    }

    /**
     * Return a public static method of a class.
     *
     * @param clazz      the class which defines the method
     * @param methodName the static method name
     * @param args       the parameter types to the method
     * @return the static method, or {@code null} if no static method was found
     * @throws IllegalArgumentException if the method name is blank or the clazz is
     *                                  null
     */
    public static Method getStaticMethod(Class<?> clazz, String methodName, Class<?>... args) {
        Assert.notNull(clazz, "Class must not be null");
        Assert.notNull(methodName, "Method name must not be null");
        try {
            Method method = clazz.getMethod(methodName, args);
            return Modifier.isStatic(method.getModifiers()) ? method : null;
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    private static Method getMethodOrNull(Class<?> clazz, String methodName, Class<?>[] paramTypes) {
        try {
            return clazz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    private static Set<Method> findMethodCandidatesByName(Class<?> clazz, String methodName) {
        Set<Method> candidates = new HashSet<>(1);
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (methodName.equals(method.getName())) {
                candidates.add(method);
            }
        }
        return candidates;
    }

    /**
     * <p>
     * 根据指定的class实例化一个对象，根据构造参数来实例化
     * </p>
     * 在java9 及其之后的版本 Class.newInstance() 方法已被废弃
     *
     * @param clazz 需要实例化的对象
     * @param <T>   类型，由输入类型决定
     * @return 返回新的实例
     */
    public static <T> T newInstance(Class<T> clazz) throws RuntimeReflectiveOperationException {
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeReflectiveOperationException(String.format("实例化对象时出现错误,请尝试给 %s 添加无参的构造方法", clazz.getName()), e);
        }
    }

    /**
     * getConstructor 方法入参是可变长参数列表，对应类中构造方法的入参类型，这里使用无参构造。
     * newInstance 返回的是泛型 T，取决于 clazz 的类型 Class<T>。这里直接用 Object 接收了。
     * 调用默认方法创建对象实例
     *
     * @param clazz Class对象
     * @return 创建的对象实例
     */
    public static <T> T instantiate(Class<T> clazz) throws RuntimeException {
        try {
            final Constructor<T> constructor = clazz.getConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeReflectiveOperationException("failed to instantiate class " + clazz + " cause:", e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeReflectiveOperationException("failed to instantiate class " + clazz + " cause: no default constructor in Class[" + clazz + "]", e);
        }
    }

    /**
     * 获取类名
     *
     * @param className className 全类名，格式aaa.bbb.ccc.xxx
     * @return ignore
     */
    public static String getSimpleName(String className) {
        return !StringUtils.hasText(className) ? null : className.substring(className.lastIndexOf(".") + 1);
    }

    /**
     * 开头不能是数字，不能包含.
     *
     * @param identifier 类名
     * @return 是否是合法的Java标识符
     */
    public static boolean isValidJavaIdentifier(String identifier) {
        // 确定是否允许将指定字符作为 Java 标识符中的首字符。
        if (identifier == null || identifier.isEmpty() || !Character.isJavaIdentifierStart(identifier.charAt(0))) {
            return false;
        }
        int len = identifier.length();
        for (int i = 1; i < len; i++) {
            char c = identifier.charAt(i);
            if (c == '.') {
                return false;
            }
            // 确定指定字符是否可以是 Java 标识符中首字符以外的部分。
            if (!Character.isJavaIdentifierPart(identifier.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 不用正则判断
     * [a-zA-Z]+[0-9a-zA-Z_]*(\.[a-zA-Z]+[0-9a-zA-Z_]*)*\.[a-zA-Z]+[0-9a-zA-Z_]*(\$[a-zA-Z]+[0-9a-zA-Z_]*)*
     * 不能有空格，不能连续两个.
     * 支持内部类，不支持lambda
     * <a href="https://blog.csdn.net/zhanglianyu00/article/details/77499295">...</a>
     *
     * @param str 字符串
     * @return 是否是全限定类名
     */
    public static boolean isValidQualifiedClassName(String str) {
        // 空字符串或者以.开头或结尾
        if (str == null || str.isEmpty() || str.startsWith(".") || str.endsWith(".")) {
            return false;
        }
        boolean flag = true;
        char[] chars = str.toCharArray();
        int curDotIndex = 0;
        // O(n)
        for (int i = 1; i < chars.length - 1; i++) {
            if (chars[i] == '.') {
                curDotIndex = i;
                if (chars[i - 1] == '.' || Character.isJavaIdentifierPart(chars[i - 1])) {
                    return false;
                }
                break;
            } else if (chars[i] == ' ') {
                // 空格
                return false;
            }
        }
        return curDotIndex == 0;
    }

    /**
     * <p>
     * 请仅在确定类存在的情况下调用该方法
     * </p>
     *
     * @param name 类名称
     * @return 返回转换后的 Class
     */
    public static Class<?> forName(String name) {
        try {
            return Class.forName(name, false, getDefaultClassLoader());
        } catch (ClassNotFoundException e) {
            try {
                return Class.forName(name);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException("找不到指定的class！请仅在明确确定会有 class 的时候，调用该方法", e);
            }
        }
    }

    /**
     * 判断是否为代理对象
     *
     * @param clazz 传入 class 对象
     * @return 如果对象class是代理 class，返回 true
     */
    public static boolean isProxy(Class<?> clazz) {
        if (clazz != null) {
            for (Class<?> cls : clazz.getInterfaces()) {
                if (PROXY_CLASS_NAMES.contains(cls.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param path 例如 com/mysql/cj/conf/ConnectionUrl
     * @return 以点分隔的类名
     */
    public static String toClassName(String path) {
        String replace = path.replace("/", ".");
        if (replace.endsWith(".class")) {
            int index = replace.lastIndexOf(".");
            replace = replace.substring(0, index);
        }
        return replace;
    }

    public static String getClassPath() {
        return getClassPath(false);
    }

    /**
     * 获得ClassPath，这个ClassPath路径会文件路径被标准化处理
     *
     * @param isEncoded 是否编码路径中的中文
     * @return ClassPath
     * @since 3.2.1
     */
    public static String getClassPath(boolean isEncoded) {
        final URL classPathURL = ClassUtil.getClassPathURL();
        String url = isEncoded ? classPathURL.getPath() : URLUtil.getDecodedPath(classPathURL);
        return FileUtil.normalize(url);
    }

    public static Object getDefaultValue(Class<?> clazz) {
        // 原始类型
        if (clazz.isPrimitive()) {
            return getPrimitiveDefaultValue(clazz);
        }
        return null;
    }

    /**
     * 获取指定原始类型分的默认值<br>
     * 默认值规则为：
     *
     * <pre>
     * 1、如果为原始类型，返回0
     * 2、非原始类型返回{@code null}
     * </pre>
     *
     * @param clazz 类
     * @return 默认值
     * @since 5.8.0
     */
    public static Object getPrimitiveDefaultValue(Class<?> clazz) {
        if (long.class == clazz) {
            return 0L;
        } else if (int.class == clazz) {
            return 0;
        } else if (short.class == clazz) {
            return (short) 0;
        } else if (char.class == clazz) {
            return (char) 0;
        } else if (byte.class == clazz) {
            return (byte) 0;
        } else if (double.class == clazz) {
            return 0D;
        } else if (float.class == clazz) {
            return 0f;
        } else if (boolean.class == clazz) {
            return false;
        }
        return null;
    }
}
