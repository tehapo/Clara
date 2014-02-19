package org.vaadin.teemu.clara.inflater;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.vaadin.teemu.clara.Clara;
import org.vaadin.teemu.clara.inflater.filter.AttributeContext;
import org.vaadin.teemu.clara.inflater.filter.AttributeFilter;
import org.vaadin.teemu.clara.inflater.handler.AttributeHandlerException;

import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
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
    public void inflate_customComponent_deprecatedMethodNeverCalled() {
        try {
            inflater.inflate(getXml("custom-component.xml"));
        } catch (AttributeHandlerException e) {
            fail();
        }
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
    public void inflate_panel() {
        Panel panel = (Panel) inflater.inflate(getXml("panel.xml"));

        // check Panel content
        assertEquals(1, panel.getComponentCount());
        assertEquals(Button.class, panel.getContent().getClass());
    }

    @Test
    public void inflate_tabSheet_captionsAssigned() {
        TabSheet layout = (TabSheet) inflater.inflate(getXml("tabsheet.xml"));

        // check tab captions
        assertEquals("caption-tab1", layout.getTab(0).getCaption());
        assertEquals("caption-tab2", layout.getTab(1).getCaption());
    }

    @Test
    public void inflate_absoluteLayout() {
        AbsoluteLayout layout = (AbsoluteLayout) inflater
                .inflate(getXml("absolutelayout.xml"));

        Button button = (Button) layout.getComponentIterator().next();

        // check position attributes
        assertEquals(20, layout.getPosition(button).getTopValue(), 0);
        assertEquals(10, layout.getPosition(button).getLeftValue(), 0);
        assertEquals(Unit.PIXELS, layout.getPosition(button).getTopUnits());
        assertEquals(Unit.PERCENTAGE, layout.getPosition(button).getLeftUnits());
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

        // check margin="true false false true"
        assertEquals(new MarginInfo(true, false, false, true),
                ((VerticalLayout) layout).getMargin());
    }

    @Test
    public void inflate_singleLayout_sizeFull() {
        Component layout = inflater.inflate(getXml("single-layout.xml"));

        assertEquals(100, layout.getWidth(), 0);
        assertEquals(100, layout.getHeight(), 0);
        assertEquals(Unit.PERCENTAGE, layout.getWidthUnits());
        assertEquals(Unit.PERCENTAGE, layout.getHeightUnits());
    }

    @Test
    public void inflate_simpleMargin_layoutMarginTrue() {
        Component layout = inflater.inflate(getXml("simple-margin.xml"));

        // check margin="true"
        assertEquals(new MarginInfo(true),
                ((VerticalLayout) layout).getMargin());
    }

    @Test
    public void inflate_simpleMargin_innerLayoutMarginFalse() {
        VerticalLayout layout = (VerticalLayout) inflater
                .inflate(getXml("simple-margin.xml"));

        // check margin="false"
        assertEquals(new MarginInfo(false),
                ((VerticalLayout) layout.getComponent(0)).getMargin());
    }

    @Test
    public void inflate_alignmentTest_alignmentAssignedCorrectly() {
        VerticalLayout layout = (VerticalLayout) inflater
                .inflate(getXml("alignment-test.xml"));

        Component child = layout.getComponent(0);
        assertEquals(Alignment.TOP_RIGHT, layout.getComponentAlignment(child));
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
    public void inflate_componentWithEnumAttribute_attributeApplied() {
        Label htmlModeLabel = (Label) inflater
                .inflate(getXml("enum-label.xml"));

        // check width
        assertEquals(ContentMode.HTML, htmlModeLabel.getContentMode());
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
                Clara.findComponentById(view, "myButton").getClass());

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
