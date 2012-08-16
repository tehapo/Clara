package org.vaadin.teemu.clara.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
    public void test_getMethodsByNameAndParamCount() {
        assertEquals(
                1,
                ReflectionUtils.getMethodsByNameAndParamCount(
                        ClassToExamine.class, "setFooBar", 0).size());
        assertEquals(
                2,
                ReflectionUtils.getMethodsByNameAndParamCount(
                        ClassToExamine.class, "setFooBar", 1).size());
        assertEquals(
                1,
                ReflectionUtils.getMethodsByNameAndParamCount(
                        ClassToExamine.class, "setFooBar", 2).size());
        assertEquals(
                0,
                ReflectionUtils.getMethodsByNameAndParamCount(
                        ClassToExamine.class, "setFooBar", 3).size());
        assertEquals(
                0,
                ReflectionUtils.getMethodsByNameAndParamCount(
                        ClassToExamine.class, "nonExistingMethod", 1).size());
    }

    @Test
    public void test_isComponent() {
        assertTrue(ReflectionUtils.isComponent(Button.class));
        assertFalse(ReflectionUtils.isComponent(ClassToExamine.class));
        assertFalse(ReflectionUtils.isComponent(null));
    }
}
