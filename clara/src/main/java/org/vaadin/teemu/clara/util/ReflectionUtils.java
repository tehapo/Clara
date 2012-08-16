package org.vaadin.teemu.clara.util;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import com.vaadin.ui.Component;

/**
 * Static utility methods leveraging the Java Reflection API.
 */
public class ReflectionUtils {

    // Private constructor to prevent instantiation and subclassing.
    private ReflectionUtils() {
        throw new AssertionError();
    }

    /**
     * Returns a {@link Set} of (overloaded) {@link Method}s from the given
     * {@code clazz} that are named {@code methodName} and have
     * {@code numberOfParams}.
     * 
     * @param clazz
     * @param methodName
     * @param numberOfParams
     * @return
     */
    public static Set<Method> getMethodsByNameAndParamCount(Class<?> clazz,
            String methodName, int numberOfParams) {
        Set<Method> methods = new HashSet<Method>();
        for (Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName)
                    && method.getParameterTypes().length == numberOfParams) {
                methods.add(method);
            }
        }
        return methods;
    }

    /**
     * Returns {@code true} if the given {@link Class} implements the
     * {@link Component} interface of Vaadin Framework otherwise {@code false}.
     * 
     * @param componentClass
     *            {@link Class} to check against {@link Component} interface.
     * @return {@code true} if the given {@link Class} is a {@link Component},
     *         {@code false} otherwise.
     */
    public static boolean isComponent(Class<?> componentClass) {
        if (componentClass != null) {
            return Component.class.isAssignableFrom(componentClass);
        } else {
            return false;
        }
    }
}
