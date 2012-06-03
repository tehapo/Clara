package org.vaadin.teemu.clara.factory;

import java.util.Map;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;

public interface ComponentFactory {

    /**
     * Returns a new {@link Component} instance of given {@code namespace} and
     * {@code name} with fields populated from the {@code attributes} map. If
     * the component cannot be instantiated properly a
     * {@link ComponentInstantiationException} is thrown.
     * 
     * @param namespace
     * @param name
     * @param attributes
     * @return a new {@link Component} instance.
     * @throws ComponentInstantiationException
     */
    Component createComponent(String namespace, String name,
            Map<String, String> attributes)
            throws ComponentInstantiationException;

    void handleLayoutAttributes(ComponentContainer layout, Component component,
            Map<String, String> attributes);

}
