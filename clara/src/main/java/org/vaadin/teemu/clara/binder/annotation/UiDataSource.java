package org.vaadin.teemu.clara.binder.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Component;

/**
 * Indicates that the return value of a method with this annotation will be used
 * as a data source for a {@link Component}.
 * 
 * The return type must implement one of the Vaadin data model interfaces (
 * {@link Property}, {@link Item} or {@link Container}).
 * 
 * The method is called once during the binding.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UiDataSource {

    /**
     * The {@code id} property value of the {@link Component} to bind with the
     * data source.
     * 
     * @return {@code id} of the {@link Component}.
     */
    String value();

}
