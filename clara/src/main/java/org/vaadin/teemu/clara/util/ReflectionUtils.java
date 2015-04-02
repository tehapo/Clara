package org.vaadin.teemu.clara.util;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.vaadin.ui.Component;

/**
 * Static utility methods leveraging the Java Reflection API.
 */
public class ReflectionUtils {

    // Private constructor to prevent instantiation and subclassing.
    private ReflectionUtils() {
        throw new AssertionError();
    }

    public static class ParamCount {

        final int min, max;

        private ParamCount(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public static ParamCount fromTo(int from, int to) {
            return new ParamCount(from, to);
        }

        public static ParamCount constant(int minAndMax) {
            return new ParamCount(minAndMax, minAndMax);
        }

    }

    /**
     * Returns a {@link List} of {@link Method}s from the given {@code clazz}
     * {@link Class} that match the given {@code nameRegex} by method name and
     * has parameter count matching the given {@code numberOfParams}.
     * 
     * @param clazz
     *            {@link Class} to find the methods from.
     * @param nameRegex
     *            a regular expression to match against method names.
     * @param numberOfParams
     *            number of parameters expected.
     * @return {@link List} of methods or an empty {@link List}.
     */
    public static List<Method> findMethods(Class<?> clazz, String nameRegex,
            ParamCount numberOfParams) {
        List<Method> methods = new ArrayList<Method>();
        for (Method method : clazz.getMethods()) {
            if (method.getName().matches(nameRegex)
                    && method.getParameterTypes().length >= numberOfParams.min
                    && method.getParameterTypes().length <= numberOfParams.max) {
                methods.add(method);
            }
        }
        return methods;
    }

    /**
     * Returns a {@link List} of {@link Method}s from the given {@code clazz}
     * {@link Class} that match the given {@code nameRegex} by method name and
     * have the given {@code paramTypes} as method parameters. Use
     * {@link AnyClassOrPrimitive} to mark any parameter type.
     * 
     * @param clazz
     *            {@link Class} to find the methods from.
     * @param nameRegex
     *            a regular expression to match against method names.
     * @param paramTypes
     *            types of parameters expected, use {@link AnyClassOrPrimitive}
     *            for any parameter type.
     * @return {@link List} of methods or an empty {@link List}.
     */
    public static List<Method> findMethods(Class<?> clazz, String nameRegex,
            Class<?>... paramTypes) {
        int numberOfParams = paramTypes != null ? paramTypes.length : 0;
        List<Method> candidates = findMethods(clazz, nameRegex,
                ParamCount.constant(numberOfParams));

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
