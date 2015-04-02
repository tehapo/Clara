package org.vaadin.teemu.clara.binder.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.vaadin.ui.Component;

/**
 * Indicates that a {@code null}-valued field with this annotation will be
 * assigned to a {@link Component} instance.
 * 
 * If the field is already assigned to a non-{@code null} value, the previously
 * assigned {@link Component} instance will be used in the component hierarchy
 * instead of creating a new {@link Component} instance.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UiField {

    /**
     * The {@code id} property value of the {@link Component} to assign. Field
     * name is used if this value is not defined.
     * 
     * @return {@code id} of the {@link Component}.
     */
    String value() default "";

}
