package org.vaadin.teemu.clara.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.vaadin.teemu.clara.util.ReflectionUtils.getMethodsByNameAndParamCount;
import static org.vaadin.teemu.clara.util.ReflectionUtils.getMethodsByNameAndParamCountRange;
import static org.vaadin.teemu.clara.util.ReflectionUtils.getMethodsByNameAndParamTypes;
import static org.vaadin.teemu.clara.util.ReflectionUtils.getMethodsByParamTypes;
import static org.vaadin.teemu.clara.util.ReflectionUtils.isComponent;

import org.junit.Test;

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
    public void test_getMethodsByRegexAndParamTypes() {
        assertEquals(
                1,
                getMethodsByNameAndParamTypes(ClassToExamine.class,
                        "setFoo(.*)", String.class).size());
    }

    @Test
    public void test_getMethodsByParamTypes() {
        assertEquals(1,
                getMethodsByParamTypes(ClassToExamine.class, String.class)
                        .size());
    }

    @Test
    public void test_getMethodsByRegexAndParamCountRange() {
        assertEquals(
                4,
                getMethodsByNameAndParamCountRange(ClassToExamine.class,
                        "setFoo(.*)", 0, 2).size());
    }

    @Test
    public void test_getMethodsByNameAndParamCount_methodsWithZeroParams() {
        assertEquals(
                1,
                getMethodsByNameAndParamCount(ClassToExamine.class,
                        "setFooBar", 0).size());
    }

    @Test
    public void test_getMethodsByNameAndParamCount_methodsWithOneParam() {
        assertEquals(
                2,
                ReflectionUtils.getMethodsByNameAndParamCount(
                        ClassToExamine.class, "setFooBar", 1).size());
    }

    @Test
    public void test_getMethodsByNameAndParamCount_methodsWithTwoParams() {
        assertEquals(
                1,
                getMethodsByNameAndParamCount(ClassToExamine.class,
                        "setFooBar", 2).size());
    }

    @Test
    public void test_getMethodsByNameAndParamCount_methodsWithThreeParams() {
        assertEquals(
                0,
                getMethodsByNameAndParamCount(ClassToExamine.class,
                        "setFooBar", 3).size());
    }

    @Test
    public void test_getMethodsByNameAndParamCount_methodsWithNonExistingName() {
        assertEquals(
                0,
                getMethodsByNameAndParamCount(ClassToExamine.class,
                        "nonExistingMethod", 1).size());
    }

    @Test
    public void test_isComponent() {
        assertTrue(isComponent(Button.class));
        assertFalse(isComponent(ClassToExamine.class));
        assertFalse(isComponent(null));
    }
}
