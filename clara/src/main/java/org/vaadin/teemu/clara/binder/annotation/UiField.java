package org.vaadin.teemu.clara.binder.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.vaadin.ui.Component;

/**
 * Indicates that a field decorated with this annotation should be assigned a
 * {@link Component} instance from the created component hierarchy.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UiField {

    String value();

}
