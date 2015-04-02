package org.vaadin.teemu.clara.inflater.parser;

import com.vaadin.ui.Component;

public interface AttributeParser {

    /**
     * Returns {@code true} if this {@link AttributeParser} can parse a
     * {@link String} to an instance of the given {@code valueType}.
     * 
     * @param valueType
     *            {@link Class} of the value type to check.
     * @return {@code true} if this {@link AttributeParser} can parse the given
     *         {@code valueType}.
     */
    boolean isSupported(Class<?> valueType);

    /**
     * Returns the given {@code value} as an instance of the given
     * {@code valueType}. Before this method is invoked, the support for value
     * conversion must be checked by calling the {@link #isSupported(Class)}
     * method. The related {@link Component} is given as a parameter, but a
     * typical implementation of this method might choose ignore the
     * {@code component} parameter.
     * 
     * @param value
     *            value as {@link String}.
     * @param valueType
     *            result type of the parsing as {@link Class}.
     * @param component
     *            related {@link Component}.
     * @return given {@code value} parsed to an instance of the given
     *         {@code valueType}.
     */
    Object getValueAs(String value, Class<?> valueType, Component component);

}
