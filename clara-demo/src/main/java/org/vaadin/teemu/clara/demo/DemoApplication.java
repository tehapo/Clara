package org.vaadin.teemu.clara.demo;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.vaadin.teemu.clara.InflatedCustomComponent;
import org.vaadin.teemu.clara.LayoutInflater;
import org.vaadin.teemu.clara.LayoutInflaterException;
import org.vaadin.teemu.clara.binder.Binder;

import com.vaadin.Application;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

@SuppressWarnings("serial")
public class DemoApplication extends Application {

    private DemoController controller;
    private TextArea xmlArea;
    private HorizontalSplitPanel split = new HorizontalSplitPanel();
    private Window mainWindow;

    private LayoutInflater inflater = new LayoutInflater();
    private Binder binder = new Binder();

    @Override
    public void init() {
        setTheme("clara");
        setMainWindow(mainWindow = new Window());

        controller = new DemoController(mainWindow);
        mainWindow.setContent(split);

        VerticalLayout editor = new VerticalLayout();
        editor.setSpacing(true);
        editor.setMargin(false, false, false, true);
        editor.setHeight("100%");
        editor.addComponent(xmlArea = createXmlArea());
        editor.setExpandRatio(xmlArea, 1.0f);
        editor.addComponent(createUpdateButton());

        HorizontalLayout wrapper = new HorizontalLayout();
        wrapper.setMargin(true);
        wrapper.setSizeFull();
        wrapper.addComponent(createLogo());
        wrapper.addComponent(editor);
        wrapper.setExpandRatio(editor, 1.0f);
        split.setFirstComponent(wrapper);
        updateLayout();
        setTheme("clara");
    }

    private Component createLogo() {
        Embedded logo = new Embedded(null, new ThemeResource(
                "clara-logo-90x90.png"));
        logo.setHeight("90px");
        logo.setWidth("90px");
        return logo;
    }

    private TextArea createXmlArea() {
        TextArea area = new TextArea();
        area.setStyleName("xml-area");
        area.setSizeFull();
        area.setValue(readStartingPoint()); // initial value
        return area;
    }

    private Button createUpdateButton() {
        return new Button("Update", new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                updateLayout();
            }
        });
    }

    /**
     * Returns the content of {@code demo-layout.xml} as a {@link String}.
     */
    private String readStartingPoint() {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(getClass()
                    .getClassLoader().getResourceAsStream("demo-layout.xml")));
            StringBuilder xml = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                xml.append(line);
                xml.append("\n");
            }
            return xml.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private void updateLayout() {
        try {
            InflatedCustomComponent c = inflater
                    .inflate(new ByteArrayInputStream(xmlArea.getValue()
                            .toString().getBytes()));
            binder.bind(c, controller);

            split.setSecondComponent(c);
        } catch (LayoutInflaterException e) {
            mainWindow.showNotification(e.getMessage(),
                    Notification.TYPE_ERROR_MESSAGE);
        }
    }

}
