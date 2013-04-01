package org.vaadin.teemu.clara.inflater.parser;

import java.util.HashMap;
import java.util.Map;

import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;

/**
 * {@link AttributeParser} that parses Vaadin specific types (like
 * {@link MarginInfo} for example).
 */
public class VaadinAttributeParser implements AttributeParser {

    protected static final Map<String, Alignment> alignmentMap;
    static {
        alignmentMap = new HashMap<String, Alignment>();
        alignmentMap.put("BOTTOM_CENTER", Alignment.BOTTOM_CENTER);
        alignmentMap.put("BOTTOM_LEFT", Alignment.BOTTOM_LEFT);
        alignmentMap.put("BOTTOM_RIGHT", Alignment.BOTTOM_RIGHT);
        alignmentMap.put("MIDDLE_CENTER", Alignment.MIDDLE_CENTER);
        alignmentMap.put("MIDDLE_LEFT", Alignment.MIDDLE_LEFT);
        alignmentMap.put("MIDDLE_RIGHT", Alignment.MIDDLE_RIGHT);
        alignmentMap.put("TOP_CENTER", Alignment.TOP_CENTER);
        alignmentMap.put("TOP_LEFT", Alignment.TOP_LEFT);
        alignmentMap.put("TOP_RIGHT", Alignment.TOP_RIGHT);
    }

    @Override
    public boolean isSupported(Class<?> valueType) {
        return valueType != null
                && (valueType == MarginInfo.class || valueType == Alignment.class);
    }

    @Override
    public Object getValueAs(String value, Class<?> valueType,
            Component component) {
        if (valueType == MarginInfo.class) {
            return parseMarginInfo(value);
        } else if (valueType == Alignment.class) {
            return parseAlignment(value);
        }
        return null;
    }

    private Object parseAlignment(String value) {
        return alignmentMap.get(value);
    }

    protected MarginInfo parseMarginInfo(String margin) {
        String[] margins = margin.split(" ");
        if (margins.length == 4) {
            return new MarginInfo(Boolean.valueOf(margins[0]),
                    Boolean.valueOf(margins[1]), Boolean.valueOf(margins[2]),
                    Boolean.valueOf(margins[3]));
        } else {
            return new MarginInfo(Boolean.valueOf(margin));
        }
    }

}
