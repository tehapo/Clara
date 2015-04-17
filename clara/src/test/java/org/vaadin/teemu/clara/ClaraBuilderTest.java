package org.vaadin.teemu.clara;

import com.vaadin.ui.Component;
import org.junit.Test;
import org.vaadin.teemu.clara.inflater.LayoutInflater;
import org.vaadin.teemu.clara.inflater.filter.AttributeContext;
import org.vaadin.teemu.clara.inflater.filter.AttributeFilter;
import org.vaadin.teemu.clara.inflater.filter.AttributeFilterException;
import org.vaadin.teemu.clara.inflater.parser.AttributeParser;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Tests for {@link ClaraBuilder}.
 *
 * @author <a href="mailto:mrotteveel@bol.com>Mark Rotteveel</a>
 */
public class ClaraBuilderTest {

    private final ClaraBuilder builder = new ClaraBuilder();

    @Test
    public void withController_setsController() {
        assertNull("Controller should be null before withController call",
                builder.getController());
        final Object controllerInstance = new Object();

        ClaraBuilder returnedBuilder = builder.withController(controllerInstance);

        assertSame("Expected controller set after withController",
                controllerInstance, builder.getController());
        assertSameBuilder(returnedBuilder);
    }

    @Test
    public void withAttributeFilter_addsFilter() {
        assertTrue("AttributeFilters should initially be empty",
                builder.getAttributeFilters().isEmpty());
        final AttributeFilter filter = new DummyAttributeFilter();

        ClaraBuilder returnedBuilder = builder.withAttributeFilter(filter);

        assertSameObjectsInList("filter", builder.getAttributeFilters(), filter);
        assertSameBuilder(returnedBuilder);
    }

    @Test
    public void withAttributeFilters_addsFilters_inOrder() {
        assertTrue("AttributeFilters should initially be empty",
                builder.getAttributeFilters().isEmpty());
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
        assertTrue("AttributeFilters should initially be empty",
                builder.getAttributeFilters().isEmpty());
        final AttributeFilter filter1 = new DummyAttributeFilter();
        final AttributeFilter filter2 = new DummyAttributeFilter();
        final AttributeFilter filter3 = new DummyAttributeFilter();
        final AttributeFilter filter4 = new DummyAttributeFilter();

        ClaraBuilder returnedBuilder = builder
                .withAttributeFilter(filter1)
                .withAttributeFilters(filter2, filter3)
                .withAttributeFilter(filter4);

        assertSameObjectsInList("filter", builder.getAttributeFilters(),
                filter1, filter2, filter3, filter4);
        assertSameBuilder(returnedBuilder);
    }

    @Test
    public void withAttributeParser_addsParser() {
        assertTrue("AttributeFilters should initially be empty",
                builder.getAttributeParsers().isEmpty());
        final AttributeParser parser = new DummyAttributeParser();

        ClaraBuilder returnedBuilder = builder.withAttributeParser(parser);

        assertSameObjectsInList("parser", builder.getAttributeParsers(),
                parser);
        assertSameBuilder(returnedBuilder);
    }

    @Test
    public void withAttributeParsers_addsParsers_inOrder() {
        assertTrue("AttributeFilters should initially be empty",
                builder.getAttributeParsers().isEmpty());
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
        assertTrue("AttributeFilters should initially be empty",
                builder.getAttributeParsers().isEmpty());
        final AttributeParser parser1 = new DummyAttributeParser();
        final AttributeParser parser2 = new DummyAttributeParser();
        final AttributeParser parser3 = new DummyAttributeParser();
        final AttributeParser parser4 = new DummyAttributeParser();

        ClaraBuilder returnedBuilder = builder
                .withAttributeParser(parser1)
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

        LayoutInflater inflater = builder
                .withController(controller)
                .withAttributeFilters(filter1, filter2)
                .withAttributeParsers(parser1, parser2)
                .createInflater();

        assertNotNull(inflater);
        // ideally we'd like to test whether the info in the builder
        // also gets into the inflater; however this currently isn't exposed.
    }

    // ClaraBuilder#create(InputStream) is tested through ClaraTest

    private void assertSameBuilder(ClaraBuilder returnedBuilder) {
        assertSame("Expected same builder", builder, returnedBuilder);
    }

    private void assertSameObjectsInList(String objectTypeName,
            List<?> objectsToCheck, Object... expectedObjects) {
        assertEquals(String.format("Unexpected number of %ss", objectTypeName),
                expectedObjects.length, objectsToCheck.size());

        for (int idx = 0; idx < expectedObjects.length; idx++) {
            assertSame(String.format("Unexpected %s object for position %d",
                            objectTypeName, idx),
                    expectedObjects[idx], objectsToCheck.get(idx));
        }
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
}
