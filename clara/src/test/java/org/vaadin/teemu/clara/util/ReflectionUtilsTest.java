package org.vaadin.teemu.clara.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.vaadin.teemu.clara.util.ReflectionUtils.findMethods;
import static org.vaadin.teemu.clara.util.ReflectionUtils.isComponent;

import org.junit.Test;
import org.vaadin.teemu.clara.util.ReflectionUtils.ParamCount;

import com.vaadin.ui.Button;

public class ReflectionUtilsTest {

    public static class ClassToExamine {

        public void setFooBar() {
            // NOP
        }

        public void setFooBar(String foo) {
            // NOP
        }

        public void setFooBar(int foo) {
            // NOP
        }

        public void setFooBar(String foo, int bar) {
            // NOP
        }

    }

    @Test
    public void test_findMethodsByRegexAndType() {
        assertEquals(1,
                findMethods(ClassToExamine.class, "setFoo(.*)", String.class)
                        .size());
    }

    @Test
    public void test_findMethodsByRegexAndTypeUsingAny() {
        assertEquals(
                1,
                findMethods(
                        ClassToExamine.class,
                        "setFoo(.*)",
                        new Class<?>[] { String.class,
                                AnyClassOrPrimitive.class }).size());
    }

    @Test
    public void test_findMethodsByRegexAndTypeAsNull() {
        assertEquals(
                1,
                findMethods(ClassToExamine.class, "setFoo(.*)",
                        (Class<?>[]) null).size());
    }

    @Test
    public void test_findMethodsByRegExAndParamCount() {
        assertEquals(
                4,
                findMethods(ClassToExamine.class, "setFoo(.*)",
                        ParamCount.fromTo(0, 2)).size());
    }

    @Test
    public void test_findMethodsByConstantParamCount_constant0() {
        assertEquals(
                1,
                findMethods(ClassToExamine.class, "setFooBar",
                        ParamCount.constant(0)).size());
    }

    @Test
    public void test_findMethodsByConstantParamCount_constant1() {
        assertEquals(
                2,
                ReflectionUtils.findMethods(ClassToExamine.class, "setFooBar",
                        ParamCount.constant(1)).size());
    }

    @Test
    public void test_findMethodsByConstantParamCount_constant2() {
        assertEquals(
                1,
                ReflectionUtils.findMethods(ClassToExamine.class, "setFooBar",
                        ParamCount.constant(2)).size());
    }

    @Test
    public void test_findMethodsByConstantParamCount_constant3() {
        assertEquals(
                0,
                ReflectionUtils.findMethods(ClassToExamine.class, "setFooBar",
                        ParamCount.constant(3)).size());
    }

    @Test
    public void test_isComponent() {
        assertTrue(isComponent(Button.class));
        assertFalse(isComponent(ClassToExamine.class));
        assertFalse(isComponent(null));
    }
}
