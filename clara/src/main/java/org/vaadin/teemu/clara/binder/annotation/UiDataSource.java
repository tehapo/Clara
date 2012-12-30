package org.vaadin.teemu.clara.binder.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;

/**
 * Indicates that the return value of a method decorated with this annotation
 * can be used as a data source for UI components.
 * 
 * <br />
 * <br />
 * This means that the return value must implement one of the Vaadin data model
 * interfaces ({@link Property}, {@link Item} or {@link Container}).
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface UiDataSource {

    String value();

}
