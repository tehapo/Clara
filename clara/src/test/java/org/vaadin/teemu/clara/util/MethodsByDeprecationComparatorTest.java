package org.vaadin.teemu.clara.util;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

public class MethodsByDeprecationComparatorTest {

    private interface InterfaceToTest {
        void test();

        @Deprecated
        void test2();

        void test3();
    }

    @Test
    public void testMethodComparator() {
        List<Method> methods = Arrays
                .asList(InterfaceToTest.class.getMethods());
        Collections.sort(methods, new MethodsByDeprecationComparator());

        // Deprecated method is now last.
        assertEquals(methods.get(2).getName(), "test2");
    }
}
