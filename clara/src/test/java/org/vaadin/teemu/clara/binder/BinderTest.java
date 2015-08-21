package org.vaadin.teemu.clara.binder;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Date;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.vaadin.teemu.clara.binder.annotation.UiDataSource;
import org.vaadin.teemu.clara.binder.annotation.UiField;
import org.vaadin.teemu.clara.binder.annotation.UiHandler;
import org.vaadin.teemu.clara.inflater.LayoutInflater;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
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
        ButtonAndControllerWrapper buttonAndController = buildAndBindButtonWithController();
        simulateButtonClickAndAssert(buttonAndController);
    }

    @Test
    public void bind_clickListener_clickListenerInvoked_after_deserialization()
            throws IOException, ClassNotFoundException {
        ButtonAndControllerWrapper original = buildAndBindButtonWithController();

        ByteArrayOutputStream targetStream = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(targetStream);
        out.writeObject(original);
        out.close();
        ByteArrayInputStream sourceStream = new ByteArrayInputStream(
                targetStream.toByteArray());
        ObjectInputStream in = new ObjectInputStream(sourceStream);
        ButtonAndControllerWrapper deserialized = (ButtonAndControllerWrapper) in
                .readObject();

        simulateButtonClickAndAssert(deserialized);
    }

    @Test
    public void bind_field_fieldSetCorrectly() {
        Button button = (Button) inflater.inflate(getXml("single-button.xml"));

        ControllerWithFieldBinding controller = new ControllerWithFieldBinding();
        Binder binder = new Binder();
        binder.bind(button, controller);

        // check that the field is correctly set
        assertSame(button, controller.getMyButton());
    }

    @Test
    public void bind_field_fieldOfSuperclassSetCorrectly() {
        Button button = (Button) inflater.inflate(getXml("single-button.xml"));

        SubcontrollerWithoutFieldBinding controller = new SubcontrollerWithoutFieldBinding();
        Binder binder = new Binder();
        binder.bind(button, controller);

        // check that the field is correctly set
        assertSame(button, controller.getMyButton());
    }

    @Test
    public void getAlreadyAssignedFields_yieldFieldFromClass() {
        ControllerWithFieldBinding controller = new ControllerWithFieldBinding(
                new Button("myButton"));
        Binder binder = new Binder();
        Map<String, Component> assignedFields = binder
                .getAlreadyAssignedFields(controller);
        assertEquals(1, assignedFields.size());
    }

    @Test
    public void getAlreadyAssignedFields_yieldFieldFromBaseClass() {
        SubcontrollerWithoutFieldBinding controller = new SubcontrollerWithoutFieldBinding(
                new Button("myButton"));
        Binder binder = new Binder();
        Map<String, Component> assignedFields = binder
                .getAlreadyAssignedFields(controller);
        assertEquals(1, assignedFields.size());
    }

    @Test(expected = BinderException.class)
    public void bind_fieldWithMissingId_exceptionThrown() {
        Button button = (Button) inflater.inflate(getXml("single-button.xml"));

        ControllerWithMissingIdBinding controller = new ControllerWithMissingIdBinding();
        Binder binder = new Binder();
        binder.bind(button, controller);
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

    @Test
    public void bind_withoutId_fieldSetCorrectly() {
        Button button = (Button) inflater.inflate(getXml("single-button.xml"));

        ControllerWithFieldBindingWithoutId controller = new ControllerWithFieldBindingWithoutId();
        Binder binder = new Binder();
        binder.bind(button, controller);

        // check that the field is correctly set
        assertSame(button, controller.myButton);
    }

    @Test
    public void bound_event_exceptionHandling_noUndeclaredThrowableOrInvocationTargetException() {
        Button button = (Button) inflater.inflate(getXml("single-button.xml"));

        ControllerWithExceptionInClickHandler controller = new ControllerWithExceptionInClickHandler();
        Binder binder = new Binder();
        binder.bind(button, controller);

        try {
            button.click();
        } catch (Exception ex) {
            Throwable current = ex;
            while (current != null) {
                assertThat(
                        current,
                        allOf(not(instanceOf(UndeclaredThrowableException.class)),
                                not(instanceOf(InvocationTargetException.class))));
                current = current.getCause();
            }
        }
    }

    private ButtonAndControllerWrapper buildAndBindButtonWithController() {
        Button button = (Button) inflater.inflate(getXml("single-button.xml"));

        ControllerWithClickHandler controller = new ControllerWithClickHandler();
        Binder binder = new Binder();
        binder.bind(button, controller);

        return new ButtonAndControllerWrapper(controller, button);
    }

    private void simulateButtonClickAndAssert(ButtonAndControllerWrapper wrapper) {
        wrapper.button.click();

        // check that the handler was called
        assertTrue("Annotated handler method was not called.",
                wrapper.controller.clickCalled);
    }

    /*
     * Static controller classes to be used to test different bindings.
     */
    public static class ControllerWithMissingIdBinding {

        @UiHandler("non-existing-id")
        public void handleButtonClick(ClickEvent event) {
            // NOP
        }

    }

    public static class ControllerWithDataSource {

        @UiDataSource("my-datefield")
        public Property<Date> getDataSource() {
            Date date = new Date(1337337477578L);
            return new com.vaadin.data.util.ObjectProperty<Date>(date);
        }

    }

    public static class ControllerWithClickHandler implements Serializable {

        boolean clickCalled;

        @UiHandler("myButton")
        public void handleButtonClick(ClickEvent event) {
            clickCalled = true;
        }

    }

    private static class ButtonAndControllerWrapper implements Serializable {

        private final ControllerWithClickHandler controller;
        private final Button button;

        private ButtonAndControllerWrapper(
                ControllerWithClickHandler controller, Button button) {
            this.controller = controller;
            this.button = button;
        }

    }

    public static class ControllerWithFieldBinding {

        @UiField("myButton")
        private Button myButton;

        public ControllerWithFieldBinding() {
            // do nothing
        }

        public ControllerWithFieldBinding(Button button) {
            myButton = button;
        }

        public Button getMyButton() {
            return myButton;
        }

    }

    public static class SubcontrollerWithoutFieldBinding extends
            ControllerWithFieldBinding {

        public SubcontrollerWithoutFieldBinding() {
            super();
        }

        public SubcontrollerWithoutFieldBinding(Button button) {
            super(button);
        }
    }

    public static class ControllerWithFieldBindingOfMissingId {

        @UiField("non-existing-id")
        private Button myButton;

    }

    public static class ControllerWithFieldBindingWithoutId {

        @UiField
        private Button myButton;

    }

    public static class ControllerWithExceptionInClickHandler implements
            Serializable {

        @UiHandler("myButton")
        public void handleButtonClick(ClickEvent event) {
            throw new RuntimeException("A RuntimeException");
        }

    }

}
