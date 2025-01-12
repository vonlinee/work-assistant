package io.devpl.fxui.utils;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SingletonFactory {

    private static final Map<Class<?>, Object> instaces = new ConcurrentHashMap<>();
    private static final Map<Class<?>, WeakReference<Object>> weakReferenceInstaces = new ConcurrentHashMap<>();

    /**
     * 创建可不被回收的单例模式,当没有对象引用，单例对象将被gc掉
     * @param className
     * @return
     * @throws InstantiationException 实例化对象失败
     * @throws IllegalAccessException
     */
    @SuppressWarnings("unchecked")
    public static <E> E getInstance(Class<E> className) {
        Object instace = instaces.get(className);
        if (instace == null) {
            synchronized (SingletonFactory.class) {
                instace = instaces.get(className);
                if (instace == null) {
                    try {
                        instace = className.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    instaces.put(className, instace);
                }
            }
        }
        return (E) instace;
    }

    /**
     * 创建可回收的单例模式,当没有对象引用，单例对象将被gc掉
     * @param className
     * @return
     * @throws InstantiationException InstantiationException
     * @throws IllegalAccessException IllegalAccessException
     */
    public static <E> E getWeakInstance(Class<E> className) {
        WeakReference<Object> reference = weakReferenceInstaces.get(className);
        Object instace = reference == null ? null : reference.get();
        if (instace == null) {
            synchronized (SingletonFactory.class) {
                reference = weakReferenceInstaces.get(className);
                instace = reference == null ? null : reference.get();
                if (instace == null) {
                    try {
                        instace = className.newInstance();
                    } catch (InstantiationException | IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    weakReferenceInstaces.put(className, new WeakReference<Object>(instace));
                }
            }
        }
        return (E) instace;
    }
}
