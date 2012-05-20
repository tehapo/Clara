package org.vaadin.teemu.clara.factory;

/**
 * {@link AttributeHandler} that handles all primitive types, {@link String} and
 * {@link Object}.
 */
public class PrimitiveAttributeHandler implements AttributeHandler {

    public boolean isSupported(Class<?> valueType) {
        return valueType != null
                && (valueType.isPrimitive() || valueType == String.class || valueType == Object.class);
    }

    public Object getValueAs(String value, Class<?> type) {
        if (type == String.class || type == Object.class) {
            return value;
        }
        if (type == Boolean.TYPE) {
            return Boolean.valueOf(value);
        }
        if (type == Integer.TYPE) {
            return Integer.valueOf(value);
        }
        if (type == Byte.TYPE) {
            return Byte.valueOf(value);
        }
        if (type == Short.TYPE) {
            return Short.valueOf(value);
        }
        if (type == Long.TYPE) {
            return Long.valueOf(value);
        }
        if (type == Character.TYPE) {
            return value.charAt(0);
        }
        if (type == Float.TYPE) {
            return Float.valueOf(value);
        }
        if (type == Double.TYPE) {
            return Double.valueOf(value);
        }
        return null;
    }

}
