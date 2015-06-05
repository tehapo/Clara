package org.vaadin.teemu.clara;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.List;

import org.junit.Test;
import org.vaadin.teemu.clara.binder.annotation.UiDataSource;
import org.vaadin.teemu.clara.binder.annotation.UiField;
import org.vaadin.teemu.clara.binder.annotation.UiHandler;
import org.vaadin.teemu.clara.inflater.LayoutInflater;
import org.vaadin.teemu.clara.inflater.filter.AttributeContext;
import org.vaadin.teemu.clara.inflater.filter.AttributeFilter;
import org.vaadin.teemu.clara.inflater.filter.AttributeFilterException;
import org.vaadin.teemu.clara.inflater.parser.AttributeParser;

import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * Tests for {@link ClaraBuilder}.
 *
 * @author <a href="mailto:mrotteveel@bol.com">Mark Rotteveel</a>
 */
public class ClaraBuilderTest {

    private final ClaraBuilder builder = Clara.build();

    @Test
    public void withController_setsController() {
        assertNull("Controller should be null before withController call",
                builder.getController());
        final Object controllerInstance = new Object();

        ClaraBuilder returnedBuilder = builder
                .withController(controllerInstance);

        assertSame("Expected controller set after withController",
                controllerInstance, builder.getController());
        assertSameBuilder(returnedBuilder);
    }

    @Test
    public void withAttributeFilter_addsFilter() {
        assertTrue("AttributeFilters should initially be empty", builder
                .getAttributeFilters().isEmpty());
        final AttributeFilter filter = new DummyAttributeFilter();

        ClaraBuilder returnedBuilder = builder.withAttributeFilter(filter);

        assertSameObjectsInList("filter", builder.getAttributeFilters(), filter);
        assertSameBuilder(returnedBuilder);
    }

    @Test
    public void withAttributeFilters_addsFilters_inOrder() {
        assertTrue("AttributeFilters should initially be empty", builder
                .getAttributeFilters().isEmpty());
        final AttributeFilter filter1 = new DummyAttributeFilter();
        final AttributeFilter filter2 = new DummyAttributeFilter();

        ClaraBuilder returnedBuilder = builder.withAttributeFilters(filter1,
                filter2);

        assertSameObjectsInList("filter", builder.getAttributeFilters(),
                filter1, filter2);
        assertSameBuilder(returnedBuilder);
    }

    @Test
    public void combination_withAttributeFilter_withAttributeFilters_inOrder() {
        assertTrue("AttributeFilters should initially be empty", builder
                .getAttributeFilters().isEmpty());
        final AttributeFilter filter1 = new DummyAttributeFilter();
        final AttributeFilter filter2 = new DummyAttributeFilter();
        final AttributeFilter filter3 = new DummyAttributeFilter();
        final AttributeFilter filter4 = new DummyAttributeFilter();

        ClaraBuilder returnedBuilder = builder.withAttributeFilter(filter1)
                .withAttributeFilters(filter2, filter3)
                .withAttributeFilter(filter4);

        assertSameObjectsInList("filter", builder.getAttributeFilters(),
                filter1, filter2, filter3, filter4);
        assertSameBuilder(returnedBuilder);
    }

    @Test
    public void withAttributeParser_addsParser() {
        assertTrue("AttributeFilters should initially be empty", builder
                .getAttributeParsers().isEmpty());
        final AttributeParser parser = new DummyAttributeParser();

        ClaraBuilder returnedBuilder = builder.withAttributeParser(parser);

        assertSameObjectsInList("parser", builder.getAttributeParsers(), parser);
        assertSameBuilder(returnedBuilder);
    }

    @Test
    public void withAttributeParsers_addsParsers_inOrder() {
        assertTrue("AttributeFilters should initially be empty", builder
                .getAttributeParsers().isEmpty());
        final AttributeParser parser1 = new DummyAttributeParser();
        final AttributeParser parser2 = new DummyAttributeParser();

        ClaraBuilder returnedBuilder = builder.withAttributeParsers(parser1,
                parser2);

        assertSameObjectsInList("parser", builder.getAttributeParsers(),
                parser1, parser2);
        assertSameBuilder(returnedBuilder);
    }

    @Test
    public void combination_withAttributeParser_withAttributeParsers_inOrder() {
        assertTrue("AttributeFilters should initially be empty", builder
                .getAttributeParsers().isEmpty());
        final AttributeParser parser1 = new DummyAttributeParser();
        final AttributeParser parser2 = new DummyAttributeParser();
        final AttributeParser parser3 = new DummyAttributeParser();
        final AttributeParser parser4 = new DummyAttributeParser();

        ClaraBuilder returnedBuilder = builder.withAttributeParser(parser1)
                .withAttributeParsers(parser2, parser3)
                .withAttributeParser(parser4);

        assertSameObjectsInList("parser", builder.getAttributeParsers(),
                parser1, parser2, parser3, parser4);
        assertSameBuilder(returnedBuilder);
    }

    @Test
    public void createInflater_fullBuilder() {
        final Object controller = new Object();
        final AttributeFilter filter1 = new DummyAttributeFilter();
        final AttributeFilter filter2 = new DummyAttributeFilter();
        final AttributeParser parser1 = new DummyAttributeParser();
        final AttributeParser parser2 = new DummyAttributeParser();

        LayoutInflater inflater = builder.withController(controller)
                .withAttributeFilters(filter1, filter2)
                .withAttributeParsers(parser1, parser2).createInflater();

        assertNotNull(inflater);
        // ideally we'd like to test whether the info in the builder
        // also gets into the inflater; however this currently isn't exposed.
    }

    // ClaraBuilder#create(InputStream) is tested through ClaraTest

    @Test
    public void withIdPrefix_setsIdPrefix() {
        assertEquals("idPrefix should initially be empty", "",
                builder.getIdPrefix());
        final String idPrefix = "someIdPrefix";

        ClaraBuilder returnedBuilder = builder.withIdPrefix(idPrefix);

        assertEquals("Unexpected idPrefix", idPrefix, builder.getIdPrefix());
        assertSameBuilder(returnedBuilder);
    }

    @Test
    public void withIdPrefix_idPrefixTrimmed() {
        assertEquals("idPrefix should initially be empty", "",
                builder.getIdPrefix());
        final String idPrefix = "   someIdPrefix   ";

        ClaraBuilder returnedBuilder = builder.withIdPrefix(idPrefix);

        assertEquals("Unexpected idPrefix", idPrefix.trim(),
                builder.getIdPrefix());
        assertSameBuilder(returnedBuilder);
    }

    /**
     * This tests if a controller (annotated without id prefixes) is correctly
     * wired up if an id prefix is used
     */
    @Test
    public void testFieldBinding_withIdPrefix() {
        final SimpleTestController testController = new SimpleTestController();
        final String idPrefix = "myIdPrefix_";

        VerticalLayout layout = (VerticalLayout) Clara.build()
                .withController(testController).withIdPrefix(idPrefix)
                .createFrom(getXml("hierarchy-with-ids.xml"));

        assertSame("Unexpected layout", layout, testController.verticalLayout);
        assertEquals("Unexpected layout id", idPrefix + "id1", layout.getId());
        // Other ids tested in LayoutInflaterTest
        assertSame("Unexpected button", layout.getComponent(0),
                testController.button);
        assertSame("Unexpected label",
                ((Panel) layout.getComponent(1)).getContent(),
                testController.label);
        assertFalse("Button shouldn't have been pressed yet",
                testController.buttonPressed);
        testController.button.click();
        assertTrue("Expected click event handler to have been called",
                testController.buttonPressed);
        assertSame("Expected label to have property datasource set",
                testController.property,
                testController.label.getPropertyDataSource());
    }

    /**
     * Tests if a preassigned field in a controller (annotated without id
     * prefix) is correctly inserted in the component tree.
     */
    @Test
    public void preassignedField_withIdPrefix() {
        final SimpleTestController testController = new SimpleTestController();
        final Button testButton = new Button();
        testController.button = testButton;
        final String idPrefix = "myIdPrefix_";

        VerticalLayout layout = (VerticalLayout) Clara.build()
                .withController(testController).withIdPrefix(idPrefix)
                .createFrom(getXml("hierarchy-with-ids.xml"));

        assertSame("Unexpected layout", layout, testController.verticalLayout);
        assertSame("Button in controller should not have been overwritten",
                testButton, testController.button);
        assertSame("Button in layout should be the same as in the controller",
                testController.button, layout.getComponent(0));
        assertEquals("Unexpected id for button (should include prefix)",
                idPrefix + "id1_1", testButton.getId());
        assertFalse("Button shouldn't have been pressed yet",
                testController.buttonPressed);
        testController.button.click();
        assertTrue("Expected click event handler to have been called",
                testController.buttonPressed);
    }

    @Test
    public void inflaterListener_componentReuse() {
        VerticalLayout layout = (VerticalLayout) Clara.build().createFrom(
                "/org/vaadin/teemu/clara/component-reuse.xml");

        assertEquals(2, layout.getComponentCount());
        assertCustomComponentInflaterListener("custom1",
                (CustomComponentInflaterListener) layout.getComponent(0));
        assertCustomComponentInflaterListener("custom2",
                (CustomComponentInflaterListener) layout.getComponent(1));
    }

    @Test
    public void create_noIdPrefix_prefixOnAllComponentsWithId() {
        VerticalLayout layout = (VerticalLayout) Clara.build().createFrom(
                getXml("hierarchy-with-ids.xml"));

        assertHierarchyWithIds("", layout);
    }

    @Test
    public void create_usingIdPrefix_prefixOnAllComponentsWithId() {
        final String idPrefix = "myIdPrefix_";

        VerticalLayout layout = (VerticalLayout) Clara.build()
                .withIdPrefix(idPrefix)
                .createFrom(getXml("hierarchy-with-ids.xml"));

        assertHierarchyWithIds(idPrefix, layout);
    }

    private void assertHierarchyWithIds(String idPrefix, VerticalLayout layout) {
        assertEquals(idPrefix + "id1", layout.getId());

        Button button = (Button) layout.getComponent(0);
        assertEquals(idPrefix + "id1_1", button.getId());

        Panel panel = (Panel) layout.getComponent(1);
        assertEquals(idPrefix + "id1_2", panel.getId());

        Label label = (Label) panel.getContent();
        assertEquals(idPrefix + "id1_2_1", label.getId());

        HorizontalLayout horizontalLayout = (HorizontalLayout) layout
                .getComponent(2);
        assertNull(horizontalLayout.getId());
    }

    private void assertSameBuilder(ClaraBuilder returnedBuilder) {
        assertSame("Expected same builder", builder, returnedBuilder);
    }

    private void assertSameObjectsInList(String objectTypeName,
            List<?> objectsToCheck, Object... expectedObjects) {
        assertEquals(String.format("Unexpected number of %ss", objectTypeName),
                expectedObjects.length, objectsToCheck.size());

        for (int idx = 0; idx < expectedObjects.length; idx++) {
            assertSame(String.format("Unexpected %s object for position %d",
                    objectTypeName, idx), expectedObjects[idx],
                    objectsToCheck.get(idx));
        }
    }

    private void assertCustomComponentInflaterListener(String expectedRootId,
            CustomComponentInflaterListener component) {
        assertEquals("Unexpected id for root", expectedRootId,
                component.getId());
        VerticalLayout layout = (VerticalLayout) component.getCompositionRoot();
        assertEquals("Unexpected id for layout", expectedRootId + "_" + "id1",
                layout.getId());
    }

    private InputStream getXml(String fileName) {
        return getClass().getClassLoader().getResourceAsStream(fileName);
    }

    private static class DummyAttributeFilter implements AttributeFilter {
        @Override
        public void filter(AttributeContext attributeContext)
                throws AttributeFilterException {
            // Do nothing
        }
    }

    private static class DummyAttributeParser implements AttributeParser {
        @Override
        public boolean isSupported(Class<?> valueType) {
            return false;
        }

        @Override
        public Object getValueAs(String value, Class<?> valueType,
                Component component) {
            return null;
        }
    }

    /**
     * Controller for binding to {@code hierarchy-with-ids.xml}.
     */
    public static class SimpleTestController {
        private static final String TEST_VALUE = "TestValue";

        private boolean buttonPressed;

        private Property<String> property = new ObjectProperty<String>(
                TEST_VALUE);

        @UiField("id1")
        private VerticalLayout verticalLayout;

        @UiField("id1_1")
        private Button button;

        @UiField("id1_2_1")
        private Label label;

        @UiHandler("id1_1")
        public void onButtonPressed(Button.ClickEvent event) {
            buttonPressed = true;
        }

        @UiDataSource("id1_2_1")
        public Property<String> propertyForLabel() {
            return property;
        }
    }
}
