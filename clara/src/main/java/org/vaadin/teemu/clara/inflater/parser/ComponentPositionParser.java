package org.vaadin.teemu.clara.inflater.parser;

import java.util.logging.Logger;

import org.vaadin.teemu.clara.inflater.LayoutInflaterException;

import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.AbsoluteLayout.ComponentPosition;
import com.vaadin.ui.Component;

public class ComponentPositionParser implements AttributeParser {

    protected Logger getLogger() {
        return Logger.getLogger(ComponentPositionParser.class.getName());
    }

    @Override
    public boolean isSupported(Class<?> valueType) {
        return valueType == ComponentPosition.class;
    }

    @Override
    public Object getValueAs(String value, Class<?> valueType,
            Component component) {
        if (component.getParent() instanceof AbsoluteLayout) {
            AbsoluteLayout outerInstance = (AbsoluteLayout) component
                    .getParent();
            try {
                // ComponentPosition is an inner class that requires
                // an instance of the outer class as a parameter.
                ComponentPosition position = ((ComponentPosition) valueType
                        .getConstructors()[0].newInstance(outerInstance));
                position.setCSSString(value);
                return position;
            } catch (Exception e) {
                throw new LayoutInflaterException(
                        "Exception while processing ComponentPosition value: "
                                + value, e);
            }
        }
        return null;
    }

}
