package org.vaadin.teemu.clara.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
     * Returns a {@link Set} of {@link Method}s from the given {@code clazz}
     * {@link Class} that match the given {@code methodNameRegex} by method name
     * and have {@code numberOfParams} number of parameters.
     * 
     * @param clazz
     * @param methodNameRegex
     * @param numberOfParams
     * @return {@link Set} of methods or an empty {@link Set}.
     */
    public static List<Method> getMethodsByNameAndParamCount(Class<?> clazz,
            String methodNameRegex, int numberOfParams) {
        return getMethodsByNameAndParamCountRange(clazz, methodNameRegex,
                numberOfParams, numberOfParams);
    }

    /**
     * Returns a {@link Set} of {@link Method}s from the given {@code clazz}
     * {@link Class} that match the given {@code methodNameRegex} by method name
     * and have at least {@code minNumberOfParams} and at maximum
     * {@code maxNumberOfParams} number of parameters.
     * 
     * @param clazz
     * @param methodNameRegex
     * @param minNumberOfParams
     * @param maxNumberOfParams
     * @return {@link Set} of methods or an empty {@link Set}.
     */
    public static List<Method> getMethodsByNameAndParamCountRange(
            Class<?> clazz, String methodNameRegex, int minNumberOfParams,
            int maxNumberOfParams) {
        List<Method> methods = new ArrayList<Method>();
        for (Method method : clazz.getMethods()) {
            if (method.getName().matches(methodNameRegex)
                    && method.getParameterTypes().length >= minNumberOfParams
                    && method.getParameterTypes().length <= maxNumberOfParams) {
                methods.add(method);
            }
        }
        return methods;
    }

    /**
     * Returns a {@link Set} of {@link Method}s from the given {@code clazz}
     * {@link Class} that have the given {@code paramTypes} as method
     * parameters.
     * 
     * @param clazz
     * @param paramTypes
     * @return {@link Set} of methods or an empty {@link Set}.
     */
    public static List<Method> getMethodsByParamTypes(Class<?> clazz,
            Class<?>... paramTypes) {
        return getMethodsByNameAndParamTypes(clazz, ".*", paramTypes);
    }

    /**
     * Returns a {@link Set} of {@link Method}s from the given {@code clazz}
     * {@link Class} that match the given {@code methodNameRegex} by method name
     * and have the given {@code paramTypes} as method parameters.
     * 
     * @param clazz
     * @param methodNameRegex
     * @param paramTypes
     * @return {@link Set} of methods or an empty {@link Set}.
     */
    public static List<Method> getMethodsByNameAndParamTypes(Class<?> clazz,
            String methodNameRegex, Class<?>... paramTypes) {
        List<Method> candidates = getMethodsByNameAndParamCount(clazz,
                methodNameRegex, paramTypes.length);

        // Iterate candidate methods to remove ones with wrong parameter types.
        for (Iterator<Method> it = candidates.iterator(); it.hasNext();) {
            Class<?>[] actualParamTypes = it.next().getParameterTypes();

            // Check that each parameter type is what we expected.
            for (int j = 0; j < actualParamTypes.length; j++) {
                Class<?> expected = paramTypes[j];
                Class<?> actual = actualParamTypes[j];
                if (!(expected == AnyClassOrPrimitive.class)
                        && !expected.isAssignableFrom(actual)) {
                    it.remove();
                }
            }
        }
        return candidates;
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
