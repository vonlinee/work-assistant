package org.workassistant.util.util;

import org.jetbrains.annotations.Nullable;
import org.workassistant.util.Assert;
import org.workassistant.util.StringUtils;

import java.io.File;
import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.TimeZone;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class ObjectUtils {

    private static final int INITIAL_HASH = 7;
    private static final int MULTIPLIER = 31;

    private static final String EMPTY_STRING = "";
    private static final String NULL_STRING = "null";
    private static final String ARRAY_START = "{";
    private static final String ARRAY_END = "}";
    private static final String EMPTY_ARRAY = ARRAY_START + ARRAY_END;
    private static final String ARRAY_ELEMENT_SEPARATOR = ", ";
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private static final String NON_EMPTY_ARRAY = ARRAY_START + "..." + ARRAY_END;
    private static final String EMPTY_COLLECTION = "[]";
    private static final String NON_EMPTY_COLLECTION = "[...]";

    /**
     * Return whether the given throwable is a checked exception:
     * that is, neither a RuntimeException nor an Error.
     *
     * @param ex the throwable to check
     * @return whether the throwable is a checked exception
     * @see java.lang.Exception
     * @see java.lang.RuntimeException
     * @see java.lang.Error
     */
    public static boolean isCheckedException(Throwable ex) {
        return !(ex instanceof RuntimeException || ex instanceof Error);
    }

    /**
     * Check whether the given exception is compatible with the specified
     * exception types, as declared in a throws' clause.
     *
     * @param ex                 the exception to check
     * @param declaredExceptions the exception types declared in the throws clause
     * @return whether the given exception is compatible
     */
    public static boolean isCompatibleWithThrowsClause(Throwable ex, @Nullable Class<?>... declaredExceptions) {
        if (!isCheckedException(ex)) {
            return true;
        }
        if (declaredExceptions != null) {
            for (Class<?> declaredException : declaredExceptions) {
                if (declaredException == null) {
                    return false;
                }
                if (declaredException.isInstance(ex)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determine whether the given object is an array:
     * either an Object array or a primitive array.
     *
     * @param obj the object to check
     */
    public static boolean isArray(@Nullable Object obj) {
        return (obj != null && obj.getClass().isArray());
    }

    /**
     * Determine whether the given array is empty:
     * i.e. {@code null} or of zero length.
     *
     * @param array the array to check
     * @see #isEmpty(Object)
     */
    public static boolean isEmpty(@Nullable Object[] array) {
        return (array == null || array.length == 0);
    }

    /**
     * Determine whether the given object is empty.
     * <p>This method supports the following object types.
     * <ul>
     * <li>{@code Optional}: considered empty if not {@link Optional#isPresent()}</li>
     * <li>{@code Array}: considered empty if its length is zero</li>
     * <li>{@link CharSequence}: considered empty if its length is zero</li>
     * <li>{@link Collection}: delegates to {@link Collection#isEmpty()}</li>
     * <li>{@link Map}: delegates to {@link Map#isEmpty()}</li>
     * </ul>
     * <p>If the given object is non-null and not one of the aforementioned
     * supported types, this method returns {@code false}.
     *
     * @param obj the object to check
     * @return {@code true} if the object is {@code null} or <em>empty</em>
     * @see Optional#isPresent()
     * @see ObjectUtils#isEmpty(Object[])
     * @see StringUtils#hasLength(CharSequence)
     * @see CollectionUtils#isEmpty(java.util.Collection)
     * @see CollectionUtils#isEmpty(java.util.Map)
     * @since 4.2
     */
    public static boolean isEmpty(@Nullable Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof Optional<?> optional) {
            return optional.isEmpty();
        }
        if (obj instanceof CharSequence charSequence) {
            return charSequence.isEmpty();
        }
        if (obj.getClass().isArray()) {
            return Array.getLength(obj) == 0;
        }
        if (obj instanceof Collection<?> collection) {
            return collection.isEmpty();
        }
        if (obj instanceof Map<?, ?> map) {
            return map.isEmpty();
        }
        // else
        return false;
    }

    /**
     * Unwrap the given object which is potentially a {@link java.util.Optional}.
     *
     * @param obj the candidate object
     * @return either the value held within the {@code Optional}, {@code null}
     * if the {@code Optional} is empty, or simply the given object as-is
     * @since 5.0
     */
    @Nullable
    public static Object unwrapOptional(@Nullable Object obj) {
        if (obj instanceof Optional<?> optional) {
            if (optional.isEmpty()) {
                return null;
            }
            Object result = optional.get();
            Assert.isTrue(!(result instanceof Optional), "Multi-level Optional usage not supported");
            return result;
        }
        return obj;
    }

    /**
     * Check whether the given array contains the given element.
     *
     * @param array   the array to check (maybe {@code null},
     *                in which case the return value will always be {@code false})
     * @param element the element to check for
     * @return whether the element has been found in the given array
     */
    public static boolean containsElement(@Nullable Object[] array, Object element) {
        if (array == null) {
            return false;
        }
        for (Object arrayEle : array) {
            if (nullSafeEquals(arrayEle, element)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether the given array of enum constants contains a constant with the given name,
     * ignoring case when determining a match.
     *
     * @param enumValues the enum values to check, typically obtained via {@code MyEnum.values()}
     * @param constant   the constant name to find (must not be null or empty string)
     * @return whether the constant has been found in the given array
     */
    public static boolean containsConstant(Enum<?>[] enumValues, String constant) {
        return containsConstant(enumValues, constant, false);
    }

    /**
     * Check whether the given array of enum constants contains a constant with the given name.
     *
     * @param enumValues    the enum values to check, typically obtained via {@code MyEnum.values()}
     * @param constant      the constant name to find (must not be null or empty string)
     * @param caseSensitive whether case is significant in determining a match
     * @return whether the constant has been found in the given array
     */
    public static boolean containsConstant(Enum<?>[] enumValues, String constant, boolean caseSensitive) {
        for (Enum<?> candidate : enumValues) {
            if (caseSensitive ? candidate.toString().equals(constant) : candidate.toString().equalsIgnoreCase(constant)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Case insensitive alternative to {@link Enum#valueOf(Class, String)}.
     *
     * @param <E>        the concrete Enum type
     * @param enumValues the array of all Enum constants in question, usually per {@code Enum.values()}
     * @param constant   the constant to get the enum value of
     * @throws IllegalArgumentException if the given constant is not found in the given array
     *                                  of enum values. Use {@link #containsConstant(Enum[], String)} as a guard to avoid this exception.
     */
    public static <E extends Enum<?>> E caseInsensitiveValueOf(E[] enumValues, String constant) {
        for (E candidate : enumValues) {
            if (candidate.toString().equalsIgnoreCase(constant)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException("Constant [" + constant + "] does not exist in enum type " + enumValues.getClass().getComponentType().getName());
    }

    /**
     * Append the given object to the given array, returning a new array
     * consisting of the input array contents plus the given object.
     *
     * @param array the array to append to (can be {@code null})
     * @param obj   the object to append
     * @return the new array (of the same component type; never {@code null})
     */
    public static <A, O extends A> A[] addObjectToArray(@Nullable A[] array, @Nullable O obj) {
        return addObjectToArray(array, obj, (array != null ? array.length : 0));
    }

    /**
     * Add the given object to the given array at the specified position, returning
     * a new array consisting of the input array contents plus the given object.
     *
     * @param array    the array to add to (can be {@code null})
     * @param obj      the object to append
     * @param position the position at which to add the object
     * @return the new array (of the same component type; never {@code null})
     * @since 6.0
     */
    public static <A, O extends A> A[] addObjectToArray(@Nullable A[] array, @Nullable O obj, int position) {
        Class<?> componentType = Object.class;
        if (array != null) {
            componentType = array.getClass().getComponentType();
        } else if (obj != null) {
            componentType = obj.getClass();
        }
        int newArrayLength = (array != null ? array.length + 1 : 1);
        @SuppressWarnings("unchecked") A[] newArray = (A[]) Array.newInstance(componentType, newArrayLength);
        if (array != null) {
            System.arraycopy(array, 0, newArray, 0, position);
            System.arraycopy(array, position, newArray, position + 1, array.length - position);
        }
        newArray[position] = obj;
        return newArray;
    }

    /**
     * Convert the given array (which may be a primitive array) to an
     * object array (if necessary of primitive wrapper objects).
     * <p>A {@code null} source value will be converted to an
     * empty Object array.
     *
     * @param source the (potentially primitive) array
     * @return the corresponding object array (never {@code null})
     * @throws IllegalArgumentException if the parameter is not an array
     */
    public static Object[] toObjectArray(@Nullable Object source) {
        if (source instanceof Object[] objects) {
            return objects;
        }
        if (source == null) {
            return EMPTY_OBJECT_ARRAY;
        }
        if (!source.getClass().isArray()) {
            throw new IllegalArgumentException("Source is not an array: " + source);
        }
        int length = Array.getLength(source);
        if (length == 0) {
            return EMPTY_OBJECT_ARRAY;
        }
        Class<?> wrapperType = Array.get(source, 0).getClass();
        Object[] newArray = (Object[]) Array.newInstance(wrapperType, length);
        for (int i = 0; i < length; i++) {
            newArray[i] = Array.get(source, i);
        }
        return newArray;
    }


    //---------------------------------------------------------------------
    // Convenience methods for content-based equality/hash-code handling
    //---------------------------------------------------------------------

    /**
     * Determine if the given objects are equal, returning {@code true} if
     * both are {@code null} or {@code false} if only one is {@code null}.
     * <p>Compares arrays with {@code Arrays.equals}, performing an equality
     * check based on the array elements rather than the array reference.
     *
     * @param o1 first Object to compare
     * @param o2 second Object to compare
     * @return whether the given objects are equal
     * @see Object#equals(Object)
     * @see java.util.Arrays#equals
     */
    public static boolean nullSafeEquals(@Nullable Object o1, @Nullable Object o2) {
        if (o1 == o2) {
            return true;
        }
        if (o1 == null || o2 == null) {
            return false;
        }
        if (o1.equals(o2)) {
            return true;
        }
        if (o1.getClass().isArray() && o2.getClass().isArray()) {
            return arrayEquals(o1, o2);
        }
        return false;
    }

    /**
     * 将对象与其他对象进行equals比较，有一个返回true则返回true
     *
     * @param obj     对象
     * @param targets 其他对象
     * @return 是否相等，null和null也是相等的，返回true
     */
    public static boolean equalsAny(@Nullable Object obj, @Nullable Object... targets) {
        if (isEmpty(targets)) {
            return false;
        }
        for (Object object : targets) {
            if (Objects.equals(obj, object)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param obj       对象
     * @param condition 注意参数可能为null
     * @param targets   其他对象
     * @param <T>       对象类型
     * @return 是否相等，null和null也是相等的，返回true
     */
    @SafeVarargs
    public static <T> boolean equalsAny(@Nullable T obj, Predicate<T> condition, @Nullable T... targets) {
        if (isEmpty(targets)) {
            return false;
        }
        for (T target : targets) {
            if (condition.test(target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Compare the given arrays with {@code Arrays.equals}, performing an equality
     * check based on the array elements rather than the array reference.
     *
     * @param o1 first array to compare
     * @param o2 second array to compare
     * @return whether the given objects are equal
     * @see #nullSafeEquals(Object, Object)
     * @see java.util.Arrays#equals
     */
    private static boolean arrayEquals(Object o1, Object o2) {
        if (o1 instanceof Object[] objects1 && o2 instanceof Object[] objects2) {
            return java.util.Arrays.equals(objects1, objects2);
        }
        if (o1 instanceof boolean[] booleans1 && o2 instanceof boolean[] booleans2) {
            return java.util.Arrays.equals(booleans1, booleans2);
        }
        if (o1 instanceof byte[] bytes1 && o2 instanceof byte[] bytes2) {
            return java.util.Arrays.equals(bytes1, bytes2);
        }
        if (o1 instanceof char[] chars1 && o2 instanceof char[] chars2) {
            return java.util.Arrays.equals(chars1, chars2);
        }
        if (o1 instanceof double[] doubles1 && o2 instanceof double[] doubles2) {
            return java.util.Arrays.equals(doubles1, doubles2);
        }
        if (o1 instanceof float[] floats1 && o2 instanceof float[] floats2) {
            return java.util.Arrays.equals(floats1, floats2);
        }
        if (o1 instanceof int[] ints1 && o2 instanceof int[] ints2) {
            return java.util.Arrays.equals(ints1, ints2);
        }
        if (o1 instanceof long[] longs1 && o2 instanceof long[] longs2) {
            return java.util.Arrays.equals(longs1, longs2);
        }
        if (o1 instanceof short[] shorts1 && o2 instanceof short[] shorts2) {
            return java.util.Arrays.equals(shorts1, shorts2);
        }
        return false;
    }

    /**
     * Return as hash code for the given object; typically the value of
     * {@code Object#hashCode()}}. If the object is an array,
     * this method will delegate to any of the {@code nullSafeHashCode}
     * methods for arrays in this class. If the object is {@code null},
     * this method returns 0.
     *
     * @see Object#hashCode()
     * @see #nullSafeHashCode(Object[])
     * @see #nullSafeHashCode(boolean[])
     * @see #nullSafeHashCode(byte[])
     * @see #nullSafeHashCode(char[])
     * @see #nullSafeHashCode(double[])
     * @see #nullSafeHashCode(float[])
     * @see #nullSafeHashCode(int[])
     * @see #nullSafeHashCode(long[])
     * @see #nullSafeHashCode(short[])
     */
    public static int nullSafeHashCode(@Nullable Object obj) {
        if (obj == null) {
            return 0;
        }
        if (obj.getClass().isArray()) {
            if (obj instanceof Object[] objects) {
                return nullSafeHashCode(objects);
            }
            if (obj instanceof boolean[] booleans) {
                return nullSafeHashCode(booleans);
            }
            if (obj instanceof byte[] bytes) {
                return nullSafeHashCode(bytes);
            }
            if (obj instanceof char[] chars) {
                return nullSafeHashCode(chars);
            }
            if (obj instanceof double[] doubles) {
                return nullSafeHashCode(doubles);
            }
            if (obj instanceof float[] floats) {
                return nullSafeHashCode(floats);
            }
            if (obj instanceof int[] ints) {
                return nullSafeHashCode(ints);
            }
            if (obj instanceof long[] longs) {
                return nullSafeHashCode(longs);
            }
            if (obj instanceof short[] shorts) {
                return nullSafeHashCode(shorts);
            }
        }
        return obj.hashCode();
    }

    /**
     * Return a hash code based on the contents of the specified array.
     * If {@code array} is {@code null}, this method returns 0.
     */
    public static int nullSafeHashCode(@Nullable Object[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        for (Object element : array) {
            hash = MULTIPLIER * hash + nullSafeHashCode(element);
        }
        return hash;
    }

    /**
     * Return a hash code based on the contents of the specified array.
     * If {@code array} is {@code null}, this method returns 0.
     */
    public static int nullSafeHashCode(boolean[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        for (boolean element : array) {
            hash = MULTIPLIER * hash + Boolean.hashCode(element);
        }
        return hash;
    }

    /**
     * Return a hash code based on the contents of the specified array.
     * If {@code array} is {@code null}, this method returns 0.
     */
    public static int nullSafeHashCode(byte[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        for (byte element : array) {
            hash = MULTIPLIER * hash + element;
        }
        return hash;
    }

    /**
     * Return a hash code based on the contents of the specified array.
     * If {@code array} is {@code null}, this method returns 0.
     */
    public static int nullSafeHashCode(char[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        for (char element : array) {
            hash = MULTIPLIER * hash + element;
        }
        return hash;
    }

    /**
     * Return a hash code based on the contents of the specified array.
     * If {@code array} is {@code null}, this method returns 0.
     */
    public static int nullSafeHashCode(double[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        for (double element : array) {
            hash = MULTIPLIER * hash + Double.hashCode(element);
        }
        return hash;
    }

    /**
     * Return a hash code based on the contents of the specified array.
     * If {@code array} is {@code null}, this method returns 0.
     */
    public static int nullSafeHashCode(float[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        for (float element : array) {
            hash = MULTIPLIER * hash + Float.hashCode(element);
        }
        return hash;
    }

    /**
     * Return a hash code based on the contents of the specified array.
     * If {@code array} is {@code null}, this method returns 0.
     */
    public static int nullSafeHashCode(int[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        for (int element : array) {
            hash = MULTIPLIER * hash + element;
        }
        return hash;
    }

    /**
     * Return a hash code based on the contents of the specified array.
     * If {@code array} is {@code null}, this method returns 0.
     */
    public static int nullSafeHashCode(long[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        for (long element : array) {
            hash = MULTIPLIER * hash + Long.hashCode(element);
        }
        return hash;
    }

    /**
     * Return a hash code based on the contents of the specified array.
     * If {@code array} is {@code null}, this method returns 0.
     */
    public static int nullSafeHashCode(short[] array) {
        if (array == null) {
            return 0;
        }
        int hash = INITIAL_HASH;
        for (short element : array) {
            hash = MULTIPLIER * hash + element;
        }
        return hash;
    }

    //---------------------------------------------------------------------
    // Convenience methods for toString output
    //---------------------------------------------------------------------

    /**
     * Return a String representation of an object's overall identity.
     *
     * @param obj the object (maybe {@code null})
     * @return the object's identity as String representation,
     * or an empty String if the object was {@code null}
     */

    public static String identityToString(@Nullable Object obj) {
        if (obj == null) {
            return EMPTY_STRING;
        }
        return obj.getClass().getName() + "@" + getIdentityHexString(obj);
    }

    /**
     * Return a hex String form of an object's identity hash code.
     *
     * @param obj the object
     * @return the object's identity code in hex notation
     */

    public static String getIdentityHexString(Object obj) {
        return Integer.toHexString(System.identityHashCode(obj));
    }

    /**
     * Return a content-based String representation if {@code obj} is
     * not {@code null}; otherwise returns an empty String.
     * <p>Differs from {@link #nullSafeToString(Object)} in that it returns
     * an empty String rather than "null" for a {@code null} value.
     *
     * @param obj the object to build a display String for
     * @return a display String representation of {@code obj}
     * @see #nullSafeToString(Object)
     */

    public static String getDisplayString(@Nullable Object obj) {
        if (obj == null) {
            return EMPTY_STRING;
        }
        return nullSafeToString(obj);
    }

    /**
     * Determine the class name for the given object.
     * <p>Returns a {@code "null"} String if {@code obj} is {@code null}.
     *
     * @param obj the object to introspect (maybe {@code null})
     * @return the corresponding class name
     */

    public static String nullSafeClassName(@Nullable Object obj) {
        return (obj != null ? obj.getClass().getName() : NULL_STRING);
    }

    /**
     * Return a String representation of the specified Object.
     * <p>Builds a String representation of the contents in case of an array.
     * Returns a {@code "null"} String if {@code obj} is {@code null}.
     *
     * @param obj the object to build a String representation for
     * @return a String representation of {@code obj}
     * @see #nullSafeConciseToString(Object)
     */

    public static String nullSafeToString(@Nullable Object obj) {
        if (obj == null) {
            return NULL_STRING;
        }
        if (obj instanceof String string) {
            return string;
        }
        if (obj instanceof Object[] objects) {
            return nullSafeToString(objects);
        }
        if (obj instanceof boolean[] booleans) {
            return nullSafeToString(booleans);
        }
        if (obj instanceof byte[] bytes) {
            return nullSafeToString(bytes);
        }
        if (obj instanceof char[] chars) {
            return nullSafeToString(chars);
        }
        if (obj instanceof double[] doubles) {
            return nullSafeToString(doubles);
        }
        if (obj instanceof float[] floats) {
            return nullSafeToString(floats);
        }
        if (obj instanceof int[] ints) {
            return nullSafeToString(ints);
        }
        if (obj instanceof long[] longs) {
            return nullSafeToString(longs);
        }
        if (obj instanceof short[] shorts) {
            return nullSafeToString(shorts);
        }
        String str = obj.toString();
        return (str != null ? str : EMPTY_STRING);
    }

    /**
     * Return a String representation of the contents of the specified array.
     * <p>The String representation consists of a list of the array's elements,
     * enclosed in curly braces ({@code "{}"}). Adjacent elements are separated
     * by the characters {@code ", "} (a comma followed by a space).
     * Returns a {@code "null"} String if {@code array} is {@code null}.
     *
     * @param array the array to build a String representation for
     * @return a String representation of {@code array}
     */

    public static String nullSafeToString(@Nullable Object[] array) {
        if (array == null) {
            return NULL_STRING;
        }
        int length = array.length;
        if (length == 0) {
            return EMPTY_ARRAY;
        }
        StringJoiner stringJoiner = new StringJoiner(ARRAY_ELEMENT_SEPARATOR, ARRAY_START, ARRAY_END);
        for (Object o : array) {
            stringJoiner.add(String.valueOf(o));
        }
        return stringJoiner.toString();
    }

    /**
     * Return a String representation of the contents of the specified array.
     * <p>The String representation consists of a list of the array's elements,
     * enclosed in curly braces ({@code "{}"}). Adjacent elements are separated
     * by the characters {@code ", "} (a comma followed by a space).
     * Returns a {@code "null"} String if {@code array} is {@code null}.
     *
     * @param array the array to build a String representation for
     * @return a String representation of {@code array}
     */

    public static String nullSafeToString(boolean[] array) {
        if (array == null) {
            return NULL_STRING;
        }
        int length = array.length;
        if (length == 0) {
            return EMPTY_ARRAY;
        }
        StringJoiner stringJoiner = new StringJoiner(ARRAY_ELEMENT_SEPARATOR, ARRAY_START, ARRAY_END);
        for (boolean b : array) {
            stringJoiner.add(String.valueOf(b));
        }
        return stringJoiner.toString();
    }

    /**
     * Return a String representation of the contents of the specified array.
     * <p>The String representation consists of a list of the array's elements,
     * enclosed in curly braces ({@code "{}"}). Adjacent elements are separated
     * by the characters {@code ", "} (a comma followed by a space).
     * Returns a {@code "null"} String if {@code array} is {@code null}.
     *
     * @param array the array to build a String representation for
     * @return a String representation of {@code array}
     */

    public static String nullSafeToString(byte[] array) {
        if (array == null) {
            return NULL_STRING;
        }
        int length = array.length;
        if (length == 0) {
            return EMPTY_ARRAY;
        }
        StringJoiner stringJoiner = new StringJoiner(ARRAY_ELEMENT_SEPARATOR, ARRAY_START, ARRAY_END);
        for (byte b : array) {
            stringJoiner.add(String.valueOf(b));
        }
        return stringJoiner.toString();
    }

    /**
     * Return a String representation of the contents of the specified array.
     * <p>The String representation consists of a list of the array's elements,
     * enclosed in curly braces ({@code "{}"}). Adjacent elements are separated
     * by the characters {@code ", "} (a comma followed by a space).
     * Returns a {@code "null"} String if {@code array} is {@code null}.
     *
     * @param array the array to build a String representation for
     * @return a String representation of {@code array}
     */

    public static String nullSafeToString(char[] array) {
        if (array == null) {
            return NULL_STRING;
        }
        int length = array.length;
        if (length == 0) {
            return EMPTY_ARRAY;
        }
        StringJoiner stringJoiner = new StringJoiner(ARRAY_ELEMENT_SEPARATOR, ARRAY_START, ARRAY_END);
        for (char c : array) {
            stringJoiner.add('\'' + String.valueOf(c) + '\'');
        }
        return stringJoiner.toString();
    }

    /**
     * Return a String representation of the contents of the specified array.
     * <p>The String representation consists of a list of the array's elements,
     * enclosed in curly braces ({@code "{}"}). Adjacent elements are separated
     * by the characters {@code ", "} (a comma followed by a space).
     * Returns a {@code "null"} String if {@code array} is {@code null}.
     *
     * @param array the array to build a String representation for
     * @return a String representation of {@code array}
     */

    public static String nullSafeToString(double[] array) {
        if (array == null) {
            return NULL_STRING;
        }
        int length = array.length;
        if (length == 0) {
            return EMPTY_ARRAY;
        }
        StringJoiner stringJoiner = new StringJoiner(ARRAY_ELEMENT_SEPARATOR, ARRAY_START, ARRAY_END);
        for (double d : array) {
            stringJoiner.add(String.valueOf(d));
        }
        return stringJoiner.toString();
    }

    /**
     * Return a String representation of the contents of the specified array.
     * <p>The String representation consists of a list of the array's elements,
     * enclosed in curly braces ({@code "{}"}). Adjacent elements are separated
     * by the characters {@code ", "} (a comma followed by a space).
     * Returns a {@code "null"} String if {@code array} is {@code null}.
     *
     * @param array the array to build a String representation for
     * @return a String representation of {@code array}
     */

    public static String nullSafeToString(float[] array) {
        if (array == null) {
            return NULL_STRING;
        }
        int length = array.length;
        if (length == 0) {
            return EMPTY_ARRAY;
        }
        StringJoiner stringJoiner = new StringJoiner(ARRAY_ELEMENT_SEPARATOR, ARRAY_START, ARRAY_END);
        for (float f : array) {
            stringJoiner.add(String.valueOf(f));
        }
        return stringJoiner.toString();
    }

    /**
     * Return a String representation of the contents of the specified array.
     * <p>The String representation consists of a list of the array's elements,
     * enclosed in curly braces ({@code "{}"}). Adjacent elements are separated
     * by the characters {@code ", "} (a comma followed by a space).
     * Returns a {@code "null"} String if {@code array} is {@code null}.
     *
     * @param array the array to build a String representation for
     * @return a String representation of {@code array}
     */

    public static String nullSafeToString(int[] array) {
        if (array == null) {
            return NULL_STRING;
        }
        int length = array.length;
        if (length == 0) {
            return EMPTY_ARRAY;
        }
        StringJoiner stringJoiner = new StringJoiner(ARRAY_ELEMENT_SEPARATOR, ARRAY_START, ARRAY_END);
        for (int i : array) {
            stringJoiner.add(String.valueOf(i));
        }
        return stringJoiner.toString();
    }

    /**
     * Return a String representation of the contents of the specified array.
     * <p>The String representation consists of a list of the array's elements,
     * enclosed in curly braces ({@code "{}"}). Adjacent elements are separated
     * by the characters {@code ", "} (a comma followed by a space).
     * Returns a {@code "null"} String if {@code array} is {@code null}.
     *
     * @param array the array to build a String representation for
     * @return a String representation of {@code array}
     */

    public static String nullSafeToString(long[] array) {
        if (array == null) {
            return NULL_STRING;
        }
        int length = array.length;
        if (length == 0) {
            return EMPTY_ARRAY;
        }
        StringJoiner stringJoiner = new StringJoiner(ARRAY_ELEMENT_SEPARATOR, ARRAY_START, ARRAY_END);
        for (long l : array) {
            stringJoiner.add(String.valueOf(l));
        }
        return stringJoiner.toString();
    }

    /**
     * Return a String representation of the contents of the specified array.
     * <p>The String representation consists of a list of the array's elements,
     * enclosed in curly braces ({@code "{}"}). Adjacent elements are separated
     * by the characters {@code ", "} (a comma followed by a space).
     * Returns a {@code "null"} String if {@code array} is {@code null}.
     *
     * @param array the array to build a String representation for
     * @return a String representation of {@code array}
     */

    public static String nullSafeToString(short[] array) {
        if (array == null) {
            return NULL_STRING;
        }
        int length = array.length;
        if (length == 0) {
            return EMPTY_ARRAY;
        }
        StringJoiner stringJoiner = new StringJoiner(ARRAY_ELEMENT_SEPARATOR, ARRAY_START, ARRAY_END);
        for (short s : array) {
            stringJoiner.add(String.valueOf(s));
        }
        return stringJoiner.toString();
    }

    /**
     * Generate a null-safe, concise string representation of the supplied object
     * as described below.
     * <p>Favor this method over {@link #nullSafeToString(Object)} when you need
     * the length of the generated string to be limited.
     * <p>Returns:
     * <ul>
     * <li>{@code "null"} if {@code obj} is {@code null}</li>
     * <li>{@code Optional.empty"} if {@code obj} is an empty {@link Optional}</li>
     * <li>{@code Optional[<concise-string>]"} if {@code obj} is a non-empty {@code Optional},
     * where {@code <concise-string>} is the result of invoking {#nullSafeConciseToString}
     * on the object contained in the {@code Optional}</li>
     * <li>{@code "{}"} if {@code obj} is an empty array or {@link Map}</li>
     * <li>{@code "{...}"} if {@code obj} is a non-empty array or {@link Map}</li>
     * <li>{@code "[]"} if {@code obj} is an empty {@link Collection}</li>
     * <li>{@code "[...]"} if {@code obj} is a non-empty {@link Collection}</li>
     * <li>{@linkplain Class#getName() Class name} if {@code obj} is a {@link Class}</li>
     * <li>{@linkplain Charset#name() Charset name} if {@code obj} is a {@link Charset}</li>
     * <li>{@linkplain TimeZone#getID() TimeZone ID} if {@code obj} is a {@link TimeZone}</li>
     * <li>{@linkplain ZoneId#getId() Zone ID} if {@code obj} is a {@link ZoneId}</li>
     * <li>Potentially {@linkplain StringUtils#truncate(CharSequence) truncated string}
     * if {@code obj} is a {@link String} or {@link CharSequence}</li>
     * <li>Potentially {@linkplain StringUtils#truncate(CharSequence) truncated string}
     * if {@code obj} is a <em>simple value type</em> whose {@code toString()} method
     * returns a non-null value</li>
     * <li>Otherwise, a string representation of the object's type name concatenated
     * with {@code "@"} and a hex string form of the object's identity hash code</li>
     * </ul>
     * <p>In the context of this method, a <em>simple value type</em> is any of the following:
     * primitive wrapper (excluding {@link Void}), {@link Enum}, {@link Number},
     * {@link Date}, {@link Temporal}, {@link File}, {@link Path}, {@link URI},
     * {@link URL}, {@link InetAddress}, {@link Currency}, {@link Locale},
     * {@link UUID}, {@link Pattern}.
     *
     * @param obj the object to build a string representation for
     * @return a concise string representation of the supplied object
     * @see #nullSafeToString(Object)
     * @see StringUtils#truncate(CharSequence)
     * @since 5.3.27
     */

    public static String nullSafeConciseToString(@Nullable Object obj) {
        if (obj == null) {
            return "null";
        }
        if (obj instanceof Optional<?> optional) {
            return (optional.isEmpty() ? "Optional.empty" : "Optional[%s]".formatted(nullSafeConciseToString(optional.get())));
        }
        if (obj.getClass().isArray()) {
            return (Array.getLength(obj) == 0 ? EMPTY_ARRAY : NON_EMPTY_ARRAY);
        }
        if (obj instanceof Collection<?> collection) {
            return (collection.isEmpty() ? EMPTY_COLLECTION : NON_EMPTY_COLLECTION);
        }
        if (obj instanceof Map<?, ?> map) {
            // EMPTY_ARRAY and NON_EMPTY_ARRAY are also used for maps.
            return (map.isEmpty() ? EMPTY_ARRAY : NON_EMPTY_ARRAY);
        }
        if (obj instanceof Class<?> clazz) {
            return clazz.getName();
        }
        if (obj instanceof Charset charset) {
            return charset.name();
        }
        if (obj instanceof TimeZone timeZone) {
            return timeZone.getID();
        }
        if (obj instanceof ZoneId zoneId) {
            return zoneId.getId();
        }
        if (obj instanceof CharSequence charSequence) {
            return StringUtils.truncate(charSequence);
        }
        Class<?> type = obj.getClass();
        if (isSimpleValueType(type)) {
            String str = obj.toString();
            if (str != null) {
                return StringUtils.truncate(str);
            }
        }
        return type.getTypeName() + "@" + getIdentityHexString(obj);
    }

    /**
     * <p>As of 5.3.28, considering {@link UUID} in addition to the bean-level check.
     * <p>As of 5.3.29, additionally considering {@link File}, {@link Path},
     * {@link InetAddress}, {@link Charset}, {@link Currency}, {@link TimeZone},
     * {@link ZoneId}, {@link Pattern}.
     */
    private static boolean isSimpleValueType(Class<?> type) {
        return (Void.class != type && void.class != type && (ClassUtils.isPrimitiveOrWrapper(type) || Enum.class.isAssignableFrom(type) || CharSequence.class.isAssignableFrom(type) || Number.class.isAssignableFrom(type) || Date.class.isAssignableFrom(type) || Temporal.class.isAssignableFrom(type) || ZoneId.class.isAssignableFrom(type) || TimeZone.class.isAssignableFrom(type) || File.class.isAssignableFrom(type) || Path.class.isAssignableFrom(type) || Charset.class.isAssignableFrom(type) || Currency.class.isAssignableFrom(type) || InetAddress.class.isAssignableFrom(type) || URI.class == type || URL.class == type || UUID.class == type || Locale.class == type || Pattern.class == type || Class.class == type));
    }

    // 基本类型和包装类型之间的转换方法

    /**
     * @param arr        Integer数组
     * @param nullToZero null是否转为0
     * @param removeNull 是否移除null  nullToZero和removeNull同时为true，nullToZero优先
     * @return 基本类型数组
     */
    public static int[] nullSafeToPrimitiveInt(Integer[] arr, boolean nullToZero, boolean removeNull) {
        if (isEmpty(arr)) {
            return new int[0];
        }
        int[] nums = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == null && nullToZero) {
                nums[i] = 0;
            }
        }
        return nums;
    }

    /**
     * 所有对象都不为null
     *
     * @param objects 对象列表
     * @return 是否所有对象都不为null
     */
    public static boolean nonNull(Object... objects) {
        if (objects == null) {
            return false;
        }
        for (Object object : objects) {
            if (!Objects.nonNull(object)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks that the given argument is neither null nor empty.
     * If it is, throws {@link NullPointerException} or {@link IllegalArgumentException}.
     * Otherwise, returns the argument.
     */
    public static String requireNonEmpty(final String value, final String name) {
        if (Objects.requireNonNull(value, name).isEmpty()) {
            throw new IllegalArgumentException("Param '" + name + "' must not be empty");
        }
        return value;
    }

    /**
     * 至少一个对象为null返回true
     *
     * @param objects 对象列表
     * @return 是否所有对象都不为null
     */
    public static boolean anyNull(Object... objects) {
        if (objects == null) {
            return true;
        }
        for (Object object : objects) {
            if (!Objects.nonNull(object)) {
                return true;
            }
        }
        return false;
    }

    public static boolean allNotNull(Object... objects) {
        if (objects == null) {
            return true;
        }
        for (Object object : objects) {
            if (object != null) {
                return false;
            }
        }
        return true;
    }

}
