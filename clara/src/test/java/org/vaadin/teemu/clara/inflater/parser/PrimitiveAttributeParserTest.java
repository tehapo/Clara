package org.vaadin.teemu.clara.inflater.parser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

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

        assertEquals(true, handler.getValueAs("true", Boolean.TYPE, null));
        assertEquals(false, handler.getValueAs("false", Boolean.TYPE, null));
        assertEquals(true, handler.getValueAs("true", Boolean.class, null));
        assertEquals(false, handler.getValueAs("false", Boolean.class, null));
    }

    @Test
    public void testInteger() throws Exception {
        assertTrue(handler.isSupported(Integer.TYPE));
        assertTrue(handler.isSupported(Integer.class));

        assertEquals(10, handler.getValueAs("10", Integer.TYPE, null));
        assertEquals(-10, handler.getValueAs("-10", Integer.TYPE, null));
        assertEquals(20, handler.getValueAs("20", Integer.class, null));
        assertEquals(-20, handler.getValueAs("-20", Integer.class, null));
    }

    @Test
    public void testByte() throws Exception {
        assertTrue(handler.isSupported(Byte.TYPE));
        assertTrue(handler.isSupported(Byte.class));

        assertEquals((byte) 10, handler.getValueAs("10", Byte.TYPE, null));
        assertEquals((byte) -10, handler.getValueAs("-10", Byte.TYPE, null));
        assertEquals((byte) 20, handler.getValueAs("20", Byte.class, null));
        assertEquals((byte) -20, handler.getValueAs("-20", Byte.class, null));
    }

    @Test
    public void testShort() throws Exception {
        assertTrue(handler.isSupported(Short.TYPE));
        assertTrue(handler.isSupported(Short.class));

        assertEquals((short) 10, handler.getValueAs("10", Short.TYPE, null));
        assertEquals((short) -10, handler.getValueAs("-10", Short.TYPE, null));
        assertEquals((short) 20, handler.getValueAs("20", Short.class, null));
        assertEquals((short) -20, handler.getValueAs("-20", Short.class, null));
    }

    @Test
    public void testLong() throws Exception {
        assertTrue(handler.isSupported(Long.TYPE));
        assertTrue(handler.isSupported(Long.class));

        assertEquals((long) 10, handler.getValueAs("10", Long.TYPE, null));
        assertEquals((long) -10, handler.getValueAs("-10", Long.TYPE, null));
        assertEquals((long) 20, handler.getValueAs("20", Long.class, null));
        assertEquals((long) -20, handler.getValueAs("-20", Long.class, null));
    }

    @Test
    public void testCharacter() throws Exception {
        assertTrue(handler.isSupported(Character.TYPE));
        assertTrue(handler.isSupported(Character.class));

        assertEquals('a', handler.getValueAs("a", Character.TYPE, null));
        assertEquals('b', handler.getValueAs("b", Character.class, null));
    }

    @Test
    public void testFloat() throws Exception {
        assertTrue(handler.isSupported(Float.TYPE));
        assertTrue(handler.isSupported(Float.class));

        assertEquals(1.0f, handler.getValueAs("1.0", Float.TYPE, null));
        assertEquals(1.0f, handler.getValueAs("1.0", Float.class, null));
    }

    @Test
    public void testDouble() throws Exception {
        assertTrue(handler.isSupported(Double.TYPE));
        assertTrue(handler.isSupported(Double.class));

        assertEquals(2.0d, handler.getValueAs("2.0", Double.TYPE, null));
        assertEquals(2.0d, handler.getValueAs("2.0", Double.class, null));
    }

    @Test
    public void testString() throws Exception {
        assertTrue(handler.isSupported(String.class));

        assertEquals("Hello world!",
                handler.getValueAs("Hello world!", String.class, null));
    }
}
