package org.vaadin.teemu.clara.util;

import java.lang.reflect.Method;
import java.util.Comparator;

/**
 * {@link Comparator} to sort {@link Method}s so that deprecated methods come
 * last.
 */
public class MethodsByDeprecationComparator implements Comparator<Method> {

    private boolean isDeprecated(Method method) {
        return method.isAnnotationPresent(Deprecated.class);
    }

    @Override
    public int compare(Method method1, Method method2) {
        if (!isDeprecated(method1) && isDeprecated(method2)) {
            return -1;
        }
        if (isDeprecated(method1) && !isDeprecated(method2)) {
            return 1;
        }
        return 0;
    }

}
