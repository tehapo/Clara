package org.vaadin.teemu.clara.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.vaadin.teemu.clara.util.ReflectionUtils.findMethods;
import static org.vaadin.teemu.clara.util.ReflectionUtils.getAllDeclaredFields;
import static org.vaadin.teemu.clara.util.ReflectionUtils.getAllDeclaredFieldsAnnotatedWith;
import static org.vaadin.teemu.clara.util.ReflectionUtils.getAllDeclaredMethods;
import static org.vaadin.teemu.clara.util.ReflectionUtils.getAllDeclaredMethodsAnnotatedWith;
import static org.vaadin.teemu.clara.util.ReflectionUtils.isComponent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.junit.Test;
import org.vaadin.teemu.clara.binder.annotation.UiDataSource;
import org.vaadin.teemu.clara.binder.annotation.UiField;
import org.vaadin.teemu.clara.binder.annotation.UiHandler;
import org.vaadin.teemu.clara.util.ReflectionUtils.ParamCount;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.TextField;

public class ReflectionUtilsTest {

    public static class BaseClassToExamine {

        @SuppressWarnings("unused")
        private String stringBase;

        @UiField("textFieldFromBase")
        private TextField textFieldBase;

        public void setFooBar(String foo) {
            // NOP
        }

        @UiHandler("handlerFromBase")
        public void onValueChangedBase(Property.ValueChangeEvent e) {
            // NOP
        }

        @UiDataSource("dataSourceFromBase")
        public Container getDataFromBase() {
            return null;
        }

    }

    public static class SubClassToExamine extends BaseClassToExamine {

        @SuppressWarnings("unused")
        private String stringSub;

        @UiField("textFieldFromSub")
        private TextField textFieldSub;

        public void setFooBar() {
            // NOP
        }

        public void setFooBar(int foo) {
            // NOP
        }

        public void setFooBar(String foo, int bar) {
            // NOP
        }

        @UiHandler("handlerFromSub")
        public void onValueChangedSub(Property.ValueChangeEvent e) {
            // NOP
        }

        @UiDataSource("dataSourceFromSub")
        public Container getDataFromSub() {
            return null;
        }
    }

    private static class FieldByNameComparator implements Comparator<Field> {
        @Override
        public int compare(Field left, Field right) {
            return left.getName().compareTo(right.getName());
        }
    }

    private static class MethodByNameComparator implements Comparator<Method> {
        @Override
        public int compare(Method left, Method right) {
            return left.getName().compareTo(right.getName());
        }
    }

    @Test
    public void test_findMethodsByRegexAndType() {
        assertEquals(
                1,
                findMethods(SubClassToExamine.class, "setFoo(.*)", String.class)
                        .size());
    }

    @Test
    public void test_findMethodsByRegexAndTypeUsingAny() {
        assertEquals(
                1,
                findMethods(
                        SubClassToExamine.class,
                        "setFoo(.*)",
                        new Class<?>[] { String.class,
                                AnyClassOrPrimitive.class }).size());
    }

    @Test
    public void test_findMethodsByRegexAndTypeAsNull() {
        assertEquals(
                1,
                findMethods(SubClassToExamine.class, "setFoo(.*)",
                        (Class<?>[]) null).size());
    }

    @Test
    public void test_findMethodsByRegExAndParamCount() {
        assertEquals(
                4,
                findMethods(SubClassToExamine.class, "setFoo(.*)",
                        ParamCount.fromTo(0, 2)).size());
    }

    @Test
    public void test_findMethodsByConstantParamCount_constant0() {
        assertEquals(
                1,
                findMethods(SubClassToExamine.class, "setFooBar",
                        ParamCount.constant(0)).size());
    }

    @Test
    public void test_findMethodsByConstantParamCount_constant1() {
        assertEquals(
                2,
                findMethods(SubClassToExamine.class, "setFooBar",
                        ParamCount.constant(1)).size());
    }

    @Test
    public void test_findMethodsByConstantParamCount_constant2() {
        assertEquals(
                1,
                findMethods(SubClassToExamine.class, "setFooBar",
                        ParamCount.constant(2)).size());
    }

    @Test
    public void test_findMethodsByConstantParamCount_constant3() {
        assertEquals(
                0,
                findMethods(SubClassToExamine.class, "setFooBar",
                        ParamCount.constant(3)).size());
    }

    @Test
    public void test_isComponent() {
        assertTrue(isComponent(Button.class));
        assertFalse(isComponent(SubClassToExamine.class));
        assertFalse(isComponent(null));
    }

    @Test
    public void test_getAllDeclaredFieldsFromBaseClass() {
        List<Field> fields = getAllDeclaredFields(BaseClassToExamine.class);
        assertEquals(2, fields.size());

        Collections.sort(fields, new FieldByNameComparator());

        assertEquals("stringBase", fields.get(0).getName());
        assertEquals("textFieldBase", fields.get(1).getName());
    }

    @Test
    public void test_getAllDeclaredFieldsFromSubClass() {
        List<Field> fields = getAllDeclaredFields(SubClassToExamine.class);
        assertEquals(4, fields.size());

        Collections.sort(fields, new FieldByNameComparator());

        assertEquals("stringBase", fields.get(0).getName());
        assertEquals("stringSub", fields.get(1).getName());
        assertEquals("textFieldBase", fields.get(2).getName());
        assertEquals("textFieldSub", fields.get(3).getName());
    }

    @Test
    public void test_getAllDeclaredMethodsFromBaseClass() {
        List<Method> methods = getAllDeclaredMethods(BaseClassToExamine.class);
        assertEquals(15, methods.size());

        methods.removeAll(getAllDeclaredMethods(Object.class));
        assertEquals(3, methods.size());

        Collections.sort(methods, new MethodByNameComparator());

        assertEquals("getDataFromBase", methods.get(0).getName());
        assertEquals("onValueChangedBase", methods.get(1).getName());
        assertEquals("setFooBar", methods.get(2).getName());
    }

    @Test
    public void test_getAllDeclaredMethodsFromSubClass() {
        List<Method> methods = getAllDeclaredMethods(SubClassToExamine.class);
        assertEquals(20, methods.size());

        methods.removeAll(getAllDeclaredMethods(Object.class));
        assertEquals(8, methods.size());
        Collections.sort(methods, new MethodByNameComparator());

        assertEquals("getDataFromBase", methods.get(0).getName());
        assertEquals("getDataFromSub", methods.get(1).getName());
        assertEquals("onValueChangedBase", methods.get(2).getName());
        assertEquals("onValueChangedSub", methods.get(3).getName());
        assertEquals("setFooBar", methods.get(4).getName());
        assertEquals("setFooBar", methods.get(5).getName());
        assertEquals("setFooBar", methods.get(6).getName());
        assertEquals("setFooBar", methods.get(7).getName());
    }

    @Test
    public void test_getAllDeclaredFieldsFromBaseClassByAnnotation() {
        List<Field> fields = getAllDeclaredFieldsAnnotatedWith(
                BaseClassToExamine.class, UiField.class);
        assertEquals(1, fields.size());
        assertEquals("textFieldBase", fields.get(0).getName());
    }

    @Test
    public void test_getAllDeclaredFieldsFromSubClassByAnnotation() {
        List<Field> fields = getAllDeclaredFieldsAnnotatedWith(
                SubClassToExamine.class, UiField.class);
        assertEquals(2, fields.size());

        Collections.sort(fields, new FieldByNameComparator());

        assertEquals("textFieldBase", fields.get(0).getName());
        assertEquals("textFieldSub", fields.get(1).getName());
    }

    @Test
    public void test_getAllDeclaredMethodsFromBaseClassByAnnotation() {
        List<Method> methods = getAllDeclaredMethodsAnnotatedWith(
                BaseClassToExamine.class, UiHandler.class);
        assertEquals(1, methods.size());
        assertEquals("onValueChangedBase", methods.get(0).getName());
    }

    @Test
    public void test_getAllDeclaredMethodsFromSubClassByAnnotation() {
        List<Method> methods = getAllDeclaredMethodsAnnotatedWith(
                SubClassToExamine.class, UiDataSource.class);
        assertEquals(2, methods.size());

        Collections.sort(methods, new MethodByNameComparator());

        assertEquals("getDataFromBase", methods.get(0).getName());
        assertEquals("getDataFromSub", methods.get(1).getName());
    }

}
