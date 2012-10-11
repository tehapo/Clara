package org.vaadin.teemu.clara;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.vaadin.teemu.clara.binder.Binder;
import org.vaadin.teemu.clara.binder.annotation.DataSource;
import org.vaadin.teemu.clara.binder.annotation.EventHandler;

import com.vaadin.data.Property;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.DateField;

public class BinderTest {

    private LayoutInflater inflater;
    private boolean clickCalled;

    @Before
    public void setUp() {
        inflater = new LayoutInflater();
    }

    private InputStream getXml(String fileName) {
        return getClass().getClassLoader().getResourceAsStream(fileName);
    }

    @EventHandler("my-button")
    public void handleButtonClick(ClickEvent event) {
        clickCalled = true;
    }

    @DataSource("my-datefield")
    public Property getDataSource() {
        Date date = new Date(1337337477578L);
        return new com.vaadin.data.util.ObjectProperty<Date>(date);
    }

    @Test
    public void bind_clickListener_clickListenerInvoked() {
        Button button = (Button) inflater.inflate(getXml("single-button.xml"));

        Binder binder = new Binder();
        binder.bind(button, this);

        clickCalled = false;
        simulateButtonClick(button);

        // check that the handler was called
        assertTrue("Annotated handler method was not called.", clickCalled);
    }

    @Test
    public void bind_dataSource_dataSourceAttached() {
        DateField view = (DateField) inflater
                .inflate(getXml("single-datefield.xml"));

        Binder binder = new Binder();
        binder.bind(view, this);

        Date value = (Date) view.getValue();
        assertEquals(1337337477578L, value.getTime());
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

}
