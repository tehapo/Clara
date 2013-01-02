package org.vaadin.teemu.clara;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.vaadin.teemu.clara.binder.annotation.UiHandler;
import org.vaadin.teemu.clara.inflater.AttributeContext;
import org.vaadin.teemu.clara.inflater.AttributeFilter;
import org.vaadin.teemu.clara.inflater.LayoutInflaterException;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

public class ClaraTest {

    private InputStream xml;
    private Controller controller;
    private AttributeFilter firstFilter;
    private AttributeFilter secondFilter;

    public static class Controller {
        private boolean clicked;

        @UiHandler("button200px")
        public void clicked(Button.ClickEvent event) {
            clicked = true;
        }
    }

    public static class EmptyController {
        // This class is used as a dummy controller while testing XML loading
        // from classpath.
    }

    @Before
    public void setUp() {
        xml = getXml("integration-test.xml");
        controller = new Controller();
        firstFilter = getFilter();
        secondFilter = getSecondFilter();
    }

    @Test
    public void testCreateMethod_usingAllParametersWithTwoFilters_filtersAndControllerCalled() {
        Component layout = Clara.create(xml, controller, firstFilter,
                secondFilter);

        // check caption
        Button button200px = (Button) Clara.findComponentById(layout,
                "button200px");
        assertEquals("filteredTwice", button200px.getCaption());

        // simulate click
        assertFalse(controller.clicked);
        button200px.click();
        assertTrue(controller.clicked);
    }

    @Test
    public void testCreateMethod_usingAllParameters_filterAndControllerCalled() {
        Component layout = Clara.create(xml, controller, firstFilter);

        // check caption
        Button button200px = (Button) Clara.findComponentById(layout,
                "button200px");
        assertEquals("filteredValue", button200px.getCaption());

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
                "xml-file-for-classpath-testing.xml", new EmptyController());
        assertEquals(Button.class, component.getClass());
    }

    @Test
    public void testCreateMethod_usingAbsoluteFilenameInClasspath_xmlReadCorrectly() {
        Component component = Clara.create(
                "/org/vaadin/teemu/clara/xml-file-for-classpath-testing.xml",
                new EmptyController());
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

    public AttributeFilter getFilter() {
        return new AttributeFilter() {

            @Override
            public void filter(AttributeContext attributeContext) {
                if (attributeContext.getValue().getClass() == String.class) {
                    String value = (String) attributeContext.getValue();
                    if (value.startsWith("{i18n:")) {
                        attributeContext.setValue("filteredValue");
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

    public AttributeFilter getSecondFilter() {
        return new AttributeFilter() {

            @Override
            public void filter(AttributeContext attributeContext) {
                if (attributeContext.getValue().getClass() == String.class) {
                    String value = (String) attributeContext.getValue();
                    if (value.startsWith("filteredValue")) {
                        attributeContext.setValue("filteredTwice");
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
