package org.vaadin.teemu.clara.inflater;

public class EnumAttributeParser implements AttributeParser {

    @Override
    public boolean isSupported(Class<?> valueType) {
        return valueType.isEnum();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object getValueAs(String value, Class<?> valueType) {
        return Enum.valueOf(((Class<? extends Enum>) valueType), value);
    }
}
