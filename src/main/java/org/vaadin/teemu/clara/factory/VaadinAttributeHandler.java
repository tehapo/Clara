package org.vaadin.teemu.clara.factory;

import com.vaadin.ui.Layout.MarginInfo;

/**
 * {@link AttributeHandler} that handles Vaadin specific types (like
 * {@link MarginInfo} for example).
 */
public class VaadinAttributeHandler implements AttributeHandler {

    public boolean isSupported(Class<?> valueType) {
        return valueType != null && valueType == MarginInfo.class;
    }

    public Object getValueAs(String value, Class<?> valueType) {
        if (valueType == MarginInfo.class) {
            return parseMarginInfo(value);
        }
        return null;
    }

    protected MarginInfo parseMarginInfo(String margin) {
        if (margin.length() > 4) {
            String[] margins = margin.split(" ");
            if (margins.length == 4) {
                return new MarginInfo(Boolean.valueOf(margins[0]),
                        Boolean.valueOf(margins[1]),
                        Boolean.valueOf(margins[2]),
                        Boolean.valueOf(margins[3]));
            }
        }
        return new MarginInfo(Boolean.valueOf(margin));
    }

}
