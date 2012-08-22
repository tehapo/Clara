package org.vaadin.teemu.clara.binder;

import com.vaadin.ui.Component;

public interface ComponentMapper {

    /**
     * Returns the {@link Component} with given {@code id} or {@code null} if no
     * such {@link Component} is found.
     * 
     * @param id
     *            identifier of the {@link Component} to search for.
     * @return the {@link Component} with given {@code id} or {@code null}.
     */
    Component findComponentById(String id);

}
