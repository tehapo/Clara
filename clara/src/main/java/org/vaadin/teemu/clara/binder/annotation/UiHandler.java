package org.vaadin.teemu.clara.binder.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

/**
 * Indicates that a method decorated with this annotation can act as an event
 * handler for UI components.
 * 
 * <br />
 * <br />
 * The type of events that the method will be invoked on is derived from the
 * parameter type. For example a method with {@link ClickEvent} parameter can
 * act as a click listener for a {@link Button}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UiHandler {

    String value();

}
