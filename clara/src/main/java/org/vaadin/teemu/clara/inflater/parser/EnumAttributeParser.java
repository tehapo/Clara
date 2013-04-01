package org.vaadin.teemu.clara.inflater.parser;

import com.vaadin.ui.Component;

public class EnumAttributeParser implements AttributeParser {

    @Override
    public boolean isSupported(Class<?> valueType) {
        return valueType.isEnum();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public Object getValueAs(String value, Class<?> valueType,
            Component component) {
        return Enum.valueOf(((Class<? extends Enum>) valueType), value);
    }
}
