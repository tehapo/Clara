package org.vaadin.teemu.clara.demo;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.vaadin.teemu.clara.Clara;
import org.vaadin.teemu.clara.binder.annotation.DataSource;
import org.vaadin.teemu.clara.binder.annotation.EventHandler;
import org.vaadin.teemu.clara.inflater.LayoutInflaterException;

import com.vaadin.Application;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class DemoApplication extends Application {

    private DemoController controller;
    private TextArea xmlArea;
    private HorizontalSplitPanel split;
    private Label statusLabel;
    private Panel resultPanel;

    @Override
    public void init() {
        setTheme("clara");
        Window mainWindow = new Window();
        setMainWindow(mainWindow);

        // Create the content from xml.
        split = (HorizontalSplitPanel) Clara
                .create("DemoApplication.xml", this);
        mainWindow.setContent(split);

        // TODO should Clara support @Component (or similar) annotation
        xmlArea = (TextArea) Clara.findComponentById(split, "xmlArea");
        statusLabel = (Label) Clara.findComponentById(split, "statusLabel");
        resultPanel = (Panel) Clara.findComponentById(split, "resultPanel");

        // Initial update
        controller = new DemoController(mainWindow);
        updateResultPanel((String) xmlArea.getValue());
    }

    /**
     * Returns the content of {@code demo-layout.xml} as a {@link String}.
     */
    @DataSource("xmlArea")
    public Property readStartingPoint() {
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
            return new ObjectProperty<String>(xml.toString());
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

    private void setErrorStatus(String message) {
        statusLabel.setValue(message);
        statusLabel.addStyleName("error");
    }

    private void setOkStatus(String message) {
        statusLabel.setValue(message);
        statusLabel.removeStyleName("error");
    }

    @EventHandler("xmlArea")
    public void updateLayout(TextChangeEvent event) {
        try {
            long startTime = System.currentTimeMillis();
            updateResultPanel(event.getText());
            long endTime = System.currentTimeMillis();
            setOkStatus("Layout updated in " + (endTime - startTime) + "ms.");
        } catch (LayoutInflaterException e) {
            setErrorStatus(e.getMessage());
        }
    }

    private void updateResultPanel(String xml) {
        Component c = Clara.create(new ByteArrayInputStream(xml.getBytes()),
                controller);
        resultPanel.removeAllComponents();
        resultPanel.addComponent(c);
    }
}
