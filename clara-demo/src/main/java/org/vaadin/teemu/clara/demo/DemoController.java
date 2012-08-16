package org.vaadin.teemu.clara.demo;

import java.util.Date;

import org.vaadin.teemu.clara.binder.annotation.DataSource;
import org.vaadin.teemu.clara.binder.annotation.EventHandler;

import com.vaadin.data.Container;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Window;

public class DemoController {

    private Window window;

    public DemoController(Window window) {
        this.window = window;
    }

    @DataSource("date")
    public Property getDateProperty() {
        return new ObjectProperty<Date>(new Date());
    }

    @DataSource("person-list")
    public Container getPersonContainer() {
        IndexedContainer container = new IndexedContainer();
        container.addContainerProperty("Name", String.class, "");
        container.addContainerProperty("Age", Integer.class, 0);

        Object itemId = container.addItem();
        container.getItem(itemId).getItemProperty("Name")
                .setValue("Teemu Pöntelin");
        container.getItem(itemId).getItemProperty("Age").setValue(32);

        itemId = container.addItem();
        container.getItem(itemId).getItemProperty("Name")
                .setValue("John Smith");
        container.getItem(itemId).getItemProperty("Age").setValue(35);
        return container;
    }

    @EventHandler("button")
    public void handleButtonClick(ClickEvent event) {
        window.showNotification("Button \"button\" clicked");
    }

    @EventHandler("another-button")
    public void handleAnotherButtonClick(ClickEvent event) {
        window.showNotification("Button \"another-button\" clicked");
    }

    @EventHandler("value-field")
    public void someValueChanged(ValueChangeEvent event) {
        window.showNotification("Value of \"value-field\" is now "
                + event.getProperty().getValue());
    }

}
