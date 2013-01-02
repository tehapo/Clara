package org.vaadin.teemu.clara;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.vaadin.teemu.clara.inflater.AttributeContext;
import org.vaadin.teemu.clara.inflater.AttributeFilter;
import org.vaadin.teemu.clara.inflater.LayoutInflater;
import org.vaadin.teemu.clara.inflater.LayoutInflaterException;

import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

public class LayoutInflaterTest {

    private LayoutInflater inflater;

    @Before
    public void setUp() {
        inflater = new LayoutInflater();
    }

    private InputStream getXml(String fileName) {
        return getClass().getClassLoader().getResourceAsStream(fileName);
    }

    @Test
    public void inflate_singleButton_buttonInstantiated() {
        Button button = (Button) inflater.inflate(getXml("single-button.xml"));

        // check that the composition root is actually a Button
        assertEquals(com.vaadin.ui.Button.class, button.getClass());

        // check attributes
        assertEquals("My Button", button.getCaption());
        assertEquals(true, button.isReadOnly());
    }

    @Test
    public void inflate_singleButtonNoNamespace_buttonInstantiated() {
        Component button = inflater
                .inflate(getXml("single-button-no-namespace.xml"));

        // check that the composition root is actually a Button
        assertEquals(com.vaadin.ui.Button.class, button.getClass());

        // check attributes
        assertEquals("My Button", button.getCaption());
        assertEquals(true, button.isReadOnly());
    }

    @Test
    public void inflate_singleLayout_layoutWithMarginsInstantiated() {
        Component layout = inflater.inflate(getXml("single-layout.xml"));

        // check that the composition root is actually a VerticalLayout
        assertEquals(com.vaadin.ui.VerticalLayout.class, layout.getClass());

        // check margin="true false false true"
        assertTrue(((VerticalLayout) layout).getMargin().hasTop());
        assertFalse(((VerticalLayout) layout).getMargin().hasRight());
        assertFalse(((VerticalLayout) layout).getMargin().hasBottom());
        assertTrue(((VerticalLayout) layout).getMargin().hasLeft());
    }

    @Test
    public void inflate_layoutAttributes_layoutAttributesApplied() {
        Component layout = inflater.inflate(getXml("layout-attributes.xml"));

        // check that the composition root is actually a VerticalLayout
        assertEquals(com.vaadin.ui.VerticalLayout.class, layout.getClass());

        // check expandRatio
        VerticalLayout verticalLayout = (VerticalLayout) layout;
        Component button = verticalLayout.getComponentIterator().next();
        assertEquals(1.0f, verticalLayout.getExpandRatio(button), 0.0f);
    }

    @Test
    public void inflate_componentHasWidth_widthAttributeApplied() {
        Component layout = inflater.inflate(getXml("component-width.xml"));

        // check width
        Button button200px = (Button) Clara.findComponentById(layout,
                "button200px");
        assertEquals(200.0f, button200px.getWidth(), 0.0f);
    }

    @Test
    public void inflate_addAttributeFilter_valueFilteredCorrectly() {
        LayoutInflater filteringInflater = new LayoutInflater();
        AttributeFilter filter = new AttributeFilter() {

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

        filteringInflater.addAttributeFilter(filter);
        Component filteredView = filteringInflater
                .inflate(getXml("attributefilter-test.xml"));
        Component view = inflater.inflate(getXml("attributefilter-test.xml"));

        // check caption
        Button button200px = (Button) Clara.findComponentById(filteredView,
                "button200px");
        assertEquals("filteredValue", button200px.getCaption());
        button200px = (Button) Clara.findComponentById(view, "button200px");
        assertEquals("{i18n:test}", button200px.getCaption());
    }

    @Test
    public void inflate_singleButton_findByIdWorks() {
        Component view = inflater.inflate(getXml("single-button.xml"));

        // check that the id my-button returns a Button
        assertEquals(com.vaadin.ui.Button.class,
                Clara.findComponentById(view, "my-button").getClass());

        // check that non-existing id returns null
        assertEquals(null, Clara.findComponentById(view, "non-existing-id"));
    }

    @Test(expected = LayoutInflaterException.class)
    public void inflate_nonComponent_exceptionThrown() {
        inflater.inflate(getXml("non-component.xml"));
    }

    @Test(expected = LayoutInflaterException.class)
    public void inflate_duplicateId_exceptionThrown() {
        inflater.inflate(getXml("duplicate-id.xml"));
    }

    @Test(expected = LayoutInflaterException.class)
    public void inflate_IOException_exceptionThrown() {
        inflater.inflate(new InputStream() {

            @Override
            public int read() throws IOException {
                throw new IOException();
            }
        });
    }

    @Test(expected = LayoutInflaterException.class)
    public void inflate_invalidXml_exceptionThrown() {
        inflater.inflate(new ByteArrayInputStream("THIS IS NOT XML!".getBytes()));
    }
}
