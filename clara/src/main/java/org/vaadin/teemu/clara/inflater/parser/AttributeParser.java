package org.vaadin.teemu.clara.inflater.parser;

import com.vaadin.ui.Component;

public interface AttributeParser {

    /**
     * Returns {@code true} if this {@link AttributeParser} can parse a
     * {@link String} to an instance of the given {@code valueType}.
     * 
     * @param valueType
     * @return
     */
    boolean isSupported(Class<?> valueType);

    /**
     * Returns the given {@code value} as an instance of the given
     * {@code valueType}. Before this method is ever invoked the support for
     * value conversion is checked by calling the {@link #isSupported(Class)}
     * method. The related {@link Component} is given as a parameter, but a
     * typical implementation of this method might choose ignore the
     * {@code component}.
     * 
     * @param value
     * @param valueType
     * @param component
     * @return
     */
    Object getValueAs(String value, Class<?> valueType, Component component);

}
