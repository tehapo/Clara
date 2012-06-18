package org.vaadin.teemu.clara;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.VerticalLayout;

public class ViewInflaterTest {

    private ViewInflater inflater;

    @Before
    public void setUp() {
        inflater = new ViewInflater();
    }

    private InputStream getXml(String fileName) {
        return getClass().getClassLoader().getResourceAsStream(fileName);
    }

    @Test
    public void inflate_singleButton_buttonInstantiated() {
        CustomComponent view = inflater.inflate(getXml("single-button.xml"));
        Component button = view.getComponentIterator().next();

        // check that the composition root is actually a Button
        assertEquals(com.vaadin.ui.Button.class, button.getClass());

        // check attributes
        assertEquals("My Button", button.getCaption());
        assertEquals(true, button.isReadOnly());
    }

    @Test
    public void inflate_singleLayout_layoutWithMarginsInstantiated() {
        CustomComponent view = inflater.inflate(getXml("single-layout.xml"));
        Component layout = view.getComponentIterator().next();

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
        CustomComponent view = inflater
                .inflate(getXml("layout-attributes.xml"));
        Component layout = view.getComponentIterator().next();

        // check that the composition root is actually a VerticalLayout
        assertEquals(com.vaadin.ui.VerticalLayout.class, layout.getClass());

        // check expandRatio
        VerticalLayout verticalLayout = (VerticalLayout) layout;
        Component button = verticalLayout.getComponentIterator().next();
        assertEquals(1.0f, verticalLayout.getExpandRatio(button), 0.0f);
    }

    @Test
    public void inflate_singleButton_findByIdWorks() {
        InflatedCustomComponent view = inflater
                .inflate(getXml("single-button.xml"));

        // check that the id my-button returns a Button
        assertEquals(com.vaadin.ui.Button.class, view.findById("my-button")
                .getClass());

        // check that non-existing id returns null
        assertEquals(null, view.findById("non-existing-id"));
    }

    @Test(expected = ViewInflaterException.class)
    public void inflate_nonComponent_exceptionThrown() {
        inflater.inflate(getXml("non-component.xml"));
    }

    @Test(expected = ViewInflaterException.class)
    public void inflate_duplicateId_exceptionThrown() {
        inflater.inflate(getXml("duplicate-id.xml"));
    }

    @Test(expected = ViewInflaterException.class)
    public void inflate_IOException_exceptionThrown() {
        inflater.inflate(new InputStream() {

            @Override
            public int read() throws IOException {
                throw new IOException();
            }
        });
    }

    @Test(expected = ViewInflaterException.class)
    public void inflate_invalidXml_exceptionThrown() {
        inflater.inflate(new ByteArrayInputStream("THIS IS NOT XML!".getBytes()));
    }
}
