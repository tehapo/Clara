package org.vaadin.teemu.clara;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.vaadin.teemu.clara.inflater.PrimitiveAttributeParser;

public class PrimitiveAttributeParserTest {

    private PrimitiveAttributeParser handler;

    @Before
    public void setUp() {
        handler = new PrimitiveAttributeParser();
    }

    @Test
    public void testBoolean() throws Exception {
        assertTrue(handler.isSupported(Boolean.TYPE));
        assertTrue(handler.isSupported(Boolean.class));

        assertEquals(true, handler.getValueAs("true", Boolean.TYPE));
        assertEquals(false, handler.getValueAs("false", Boolean.TYPE));
        assertEquals(true, handler.getValueAs("true", Boolean.class));
        assertEquals(false, handler.getValueAs("false", Boolean.class));
    }

    @Test
    public void testInteger() throws Exception {
        assertTrue(handler.isSupported(Integer.TYPE));
        assertTrue(handler.isSupported(Integer.class));

        assertEquals(10, handler.getValueAs("10", Integer.TYPE));
        assertEquals(-10, handler.getValueAs("-10", Integer.TYPE));
        assertEquals(20, handler.getValueAs("20", Integer.class));
        assertEquals(-20, handler.getValueAs("-20", Integer.class));
    }

    @Test
    public void testByte() throws Exception {
        assertTrue(handler.isSupported(Byte.TYPE));
        assertTrue(handler.isSupported(Byte.class));

        assertEquals((byte) 10, handler.getValueAs("10", Byte.TYPE));
        assertEquals((byte) -10, handler.getValueAs("-10", Byte.TYPE));
        assertEquals((byte) 20, handler.getValueAs("20", Byte.class));
        assertEquals((byte) -20, handler.getValueAs("-20", Byte.class));
    }

    @Test
    public void testShort() throws Exception {
        assertTrue(handler.isSupported(Short.TYPE));
        assertTrue(handler.isSupported(Short.class));

        assertEquals((short) 10, handler.getValueAs("10", Short.TYPE));
        assertEquals((short) -10, handler.getValueAs("-10", Short.TYPE));
        assertEquals((short) 20, handler.getValueAs("20", Short.class));
        assertEquals((short) -20, handler.getValueAs("-20", Short.class));
    }

    @Test
    public void testLong() throws Exception {
        assertTrue(handler.isSupported(Long.TYPE));
        assertTrue(handler.isSupported(Long.class));

        assertEquals((long) 10, handler.getValueAs("10", Long.TYPE));
        assertEquals((long) -10, handler.getValueAs("-10", Long.TYPE));
        assertEquals((long) 20, handler.getValueAs("20", Long.class));
        assertEquals((long) -20, handler.getValueAs("-20", Long.class));
    }

    @Test
    public void testCharacter() throws Exception {
        assertTrue(handler.isSupported(Character.TYPE));
        assertTrue(handler.isSupported(Character.class));

        assertEquals('a', handler.getValueAs("a", Character.TYPE));
        assertEquals('b', handler.getValueAs("b", Character.class));
    }

    @Test
    public void testFloat() throws Exception {
        assertTrue(handler.isSupported(Float.TYPE));
        assertTrue(handler.isSupported(Float.class));

        assertEquals(1.0f, handler.getValueAs("1.0", Float.TYPE));
        assertEquals(1.0f, handler.getValueAs("1.0", Float.class));
    }

    @Test
    public void testDouble() throws Exception {
        assertTrue(handler.isSupported(Double.TYPE));
        assertTrue(handler.isSupported(Double.class));

        assertEquals(2.0d, handler.getValueAs("2.0", Double.TYPE));
        assertEquals(2.0d, handler.getValueAs("2.0", Double.class));
    }

    @Test
    public void testString() throws Exception {
        assertTrue(handler.isSupported(String.class));

        assertEquals("Hello world!",
                handler.getValueAs("Hello world!", String.class));
    }
}
