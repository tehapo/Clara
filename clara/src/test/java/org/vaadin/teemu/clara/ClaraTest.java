package org.vaadin.teemu.clara;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.vaadin.teemu.clara.binder.annotation.EventHandler;
import org.vaadin.teemu.clara.inflater.LayoutInflaterException;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

public class ClaraTest {

    private InputStream xml;
    private Controller controller;
    private AttributeInterceptor firstInterceptor;
    private AttributeInterceptor secondInterceptor;

    public static class Controller {
        private boolean clicked;

        @EventHandler("button200px")
        public void clicked(Button.ClickEvent event) {
            clicked = true;
        }
    }

    @Before
    public void setUp() {
        xml = getXml("integration-test.xml");
        controller = new Controller();
        firstInterceptor = getInterceptor();
        secondInterceptor = getSecondInterceptor();
    }

    @Test
    public void testCreateMethod_usingAllParametersWithTwoInterceptors_interceptorsAndControllerCalled() {
        Component layout = Clara.create(xml, controller, firstInterceptor,
                secondInterceptor);

        // check caption
        Button button200px = (Button) Clara.findComponentById(layout,
                "button200px");
        assertEquals("interceptedTwice", button200px.getCaption());

        // simulate click
        assertFalse(controller.clicked);
        button200px.click();
        assertTrue(controller.clicked);
    }

    @Test
    public void testCreateMethod_usingAllParameters_interceptorAndControllerCalled() {
        Component layout = Clara.create(xml, controller, firstInterceptor);

        // check caption
        Button button200px = (Button) Clara.findComponentById(layout,
                "button200px");
        assertEquals("interceptedValue", button200px.getCaption());

        // simulate click
        assertFalse(controller.clicked);
        button200px.click();
        assertTrue(controller.clicked);
    }

    @Test
    public void testCreateMethod_usingOnlyController_controllerCalled() {
        Component layout = Clara.create(xml, controller);

        // check caption
        Button button200px = (Button) Clara.findComponentById(layout,
                "button200px");
        assertEquals("{i18n:test}", button200px.getCaption());

        // simulate click
        assertFalse(controller.clicked);
        button200px.click();
        assertTrue(controller.clicked);
    }

    @Test
    public void testCreateMethod_usingRelativeFilenameInClasspath_xmlReadCorrectly() {
        Component component = Clara.create(
                "xml-file-for-classpath-testing.xml", controller);
        assertEquals(Button.class, component.getClass());
    }

    @Test
    public void testCreateMethod_usingAbsoluteFilenameInClasspath_xmlReadCorrectly() {
        Component component = Clara.create(
                "/org/vaadin/teemu/clara/xml-file-for-classpath-testing.xml",
                controller);
        assertEquals(Button.class, component.getClass());
    }

    @Test(expected = LayoutInflaterException.class)
    public void testCreateMethod_usingNonExistingFilenameInClasspath_exceptionThrown() {
        Clara.create("non-existing-file.xml", controller);
    }

    @Test
    public void testCreateMethod_usingNoParameters_componentInflatedCorrectly() {
        Component layout = Clara.create(xml);

        // check caption
        Button button200px = (Button) Clara.findComponentById(layout,
                "button200px");
        assertEquals("{i18n:test}", button200px.getCaption());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindComponentById_nullComponent_exceptionThrown() {
        Clara.findComponentById(null, "foobar");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testFindComponentById_nullComponentId_exceptionThrown() {
        Clara.findComponentById(new VerticalLayout(), null);
    }

    @Test
    public void testFindComponentById_componentExistInTree_componentFound() {
        @SuppressWarnings("serial")
        Layout layout = new VerticalLayout() {
            {
                Button b = new Button();
                b.setDebugId("foobar");
                addComponent(b);
            }
        };
        Component c = Clara.findComponentById(layout, "foobar");
        assertTrue(c instanceof Button);
        assertEquals("foobar", c.getDebugId());
    }

    @Test
    public void testFindComponentById_componentDoesntExistInTree_nullReturned() {
        @SuppressWarnings("serial")
        Layout layout = new VerticalLayout() {
            {
                Button b = new Button();
                b.setDebugId("button");
                addComponent(b);
            }
        };
        Component c = Clara.findComponentById(layout, "foobar");
        assertNull(c);
    }

    private InputStream getXml(String fileName) {
        return getClass().getClassLoader().getResourceAsStream(fileName);
    }

    public AttributeInterceptor getInterceptor() {
        return new AttributeInterceptor() {

            @Override
            public void intercept(AttributeContext attributeContext) {
                if (attributeContext.getValue().getClass() == String.class) {
                    String value = (String) attributeContext.getValue();
                    if (value.startsWith("{i18n:")) {
                        attributeContext.setValue("interceptedValue");
                    }
                }
                try {
                    attributeContext.proceed();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public AttributeInterceptor getSecondInterceptor() {
        return new AttributeInterceptor() {

            @Override
            public void intercept(AttributeContext attributeContext) {
                if (attributeContext.getValue().getClass() == String.class) {
                    String value = (String) attributeContext.getValue();
                    if (value.startsWith("interceptedValue")) {
                        attributeContext.setValue("interceptedTwice");
                    }
                }
                try {
                    attributeContext.proceed();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
