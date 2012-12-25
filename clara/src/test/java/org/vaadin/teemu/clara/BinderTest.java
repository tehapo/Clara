package org.vaadin.teemu.clara;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.vaadin.teemu.clara.binder.Binder;
import org.vaadin.teemu.clara.binder.BinderException;
import org.vaadin.teemu.clara.binder.annotation.DataSource;
import org.vaadin.teemu.clara.binder.annotation.EventHandler;
import org.vaadin.teemu.clara.inflater.LayoutInflater;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.DateField;

public class BinderTest {

    private LayoutInflater inflater;

    @Before
    public void setUp() {
        inflater = new LayoutInflater();
    }

    private InputStream getXml(String fileName) {
        return getClass().getClassLoader().getResourceAsStream(fileName);
    }

    @Test
    public void bind_clickListener_clickListenerInvoked() {
        Button button = (Button) inflater.inflate(getXml("single-button.xml"));

        ControllerWithClickHandler controller = new ControllerWithClickHandler();
        Binder binder = new Binder();
        binder.bind(button, controller);

        simulateButtonClick(button);

        // check that the handler was called
        assertTrue("Annotated handler method was not called.",
                controller.clickCalled);
    }

    @Test
    public void bind_dataSource_dataSourceAttached() {
        DateField view = (DateField) inflater
                .inflate(getXml("single-datefield.xml"));

        Binder binder = new Binder();
        binder.bind(view, new ControllerWithDataSource());

        Date value = (Date) view.getValue();
        assertEquals(1337337477578L, value.getTime());
    }

    @Test(expected = BinderException.class)
    public void bind_nonExistingId_exceptionThrown() {
        Button button = (Button) inflater.inflate(getXml("single-button.xml"));

        Binder binder = new Binder();
        binder.bind(button, new ControllerWithMissingIdBinding());
    }

    @Test(expected = BinderException.class)
    public void bind_nonEventParameter_exceptionThrown() {
        Button button = (Button) inflater.inflate(getXml("single-button.xml"));

        Binder binder = new Binder();
        binder.bind(button, new ControllerWithNonEventHandler());
    }

    private void simulateButtonClick(Button button) {
        Method fireClick;
        try {
            fireClick = Button.class.getDeclaredMethod("fireClick");
            fireClick.setAccessible(true);
            fireClick.invoke(button);
        } catch (Exception e) {
            throw new RuntimeException("Couldn't simulate button click.", e);
        }
    }

    public static class ControllerWithMissingIdBinding {

        @EventHandler("non-existing-id")
        public void handleButtonClick(ClickEvent event) {
            // NOP
        }

    }

    public static class ControllerWithDataSource {

        @DataSource("my-datefield")
        public Property getDataSource() {
            Date date = new Date(1337337477578L);
            return new com.vaadin.data.util.ObjectProperty<Date>(date);
        }

    }

    public static class ControllerWithClickHandler {

        boolean clickCalled;

        @EventHandler("my-button")
        public void handleButtonClick(ClickEvent event) {
            clickCalled = true;
        }

    }

    public static class ControllerWithNonEventHandler {

        @EventHandler("my-button")
        public void handleButtonClick(String nonEvent) {
            // NOP
        }

    }

}
