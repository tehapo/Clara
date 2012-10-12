package org.vaadin.teemu.clara.inflater;

import java.util.Arrays;
import java.util.List;

/**
 * {@link AttributeParser} that handles all primitive types (including boxed
 * representations), {@link String} and {@link Object}.
 */
public class PrimitiveAttributeParser implements AttributeParser {

    @SuppressWarnings("unchecked")
    private static final List<Class<?>> supportedClasses = Arrays.asList(
            String.class, Object.class, Boolean.class, Integer.class,
            Byte.class, Short.class, Long.class, Character.class, Float.class,
            Double.class);

    public boolean isSupported(Class<?> valueType) {
        return valueType != null
                && (valueType.isPrimitive() || supportedClasses
                        .contains(valueType));
    }

    public Object getValueAs(String value, Class<?> type) {
        if (type == String.class || type == Object.class) {
            return value;
        }
        if (type == Boolean.TYPE || type == Boolean.class) {
            return Boolean.valueOf(value);
        }
        if (type == Integer.TYPE || type == Integer.class) {
            return Integer.valueOf(value);
        }
        if (type == Byte.TYPE || type == Byte.class) {
            return Byte.valueOf(value);
        }
        if (type == Short.TYPE || type == Short.class) {
            return Short.valueOf(value);
        }
        if (type == Long.TYPE || type == Long.class) {
            return Long.valueOf(value);
        }
        if (type == Character.TYPE || type == Character.class) {
            return value.charAt(0);
        }
        if (type == Float.TYPE || type == Float.class) {
            return Float.valueOf(value);
        }
        if (type == Double.TYPE || type == Double.class) {
            return Double.valueOf(value);
        }
        return null;
    }
}
