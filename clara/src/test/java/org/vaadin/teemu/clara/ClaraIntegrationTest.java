package org.vaadin.teemu.clara;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.vaadin.teemu.clara.binder.annotation.EventHandler;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;

public class ClaraIntegrationTest {

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
    public void testCreateMethod_usingNoParameters_componentInflatedCorrectly() {
        Component layout = Clara.create(xml);

        // check caption
        Button button200px = (Button) Clara.findComponentById(layout,
                "button200px");
        assertEquals("{i18n:test}", button200px.getCaption());
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
