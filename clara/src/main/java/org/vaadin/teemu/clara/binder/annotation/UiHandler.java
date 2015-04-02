package org.vaadin.teemu.clara.binder.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;

/**
 * Indicates that a method with this annotation will be assigned to handle
 * events from a {@link Component}.
 * 
 * The type of events that this method will be invoked on is derived from the
 * parameter type. For example a method with {@link ClickEvent} parameter can
 * act as a click listener for a {@link Button} .
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UiHandler {

    /**
     * The {@code id} property value of the {@link Component} to bind with the
     * event handler method.
     * 
     * @return {@code id} of the {@link Component}.
     */
    String value();

}
