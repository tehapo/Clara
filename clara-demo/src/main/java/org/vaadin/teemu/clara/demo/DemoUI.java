package org.vaadin.teemu.clara.demo;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.vaadin.teemu.clara.Clara;
import org.vaadin.teemu.clara.binder.BinderException;
import org.vaadin.teemu.clara.binder.annotation.UiDataSource;
import org.vaadin.teemu.clara.binder.annotation.UiField;
import org.vaadin.teemu.clara.binder.annotation.UiHandler;
import org.vaadin.teemu.clara.inflater.LayoutInflaterException;

import com.vaadin.annotations.Theme;
import com.vaadin.data.Property;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;

@SuppressWarnings("serial")
@Theme("clara")
public class DemoUI extends UI {

    private DemoController controller;

    @UiField("xmlArea")
    private TextArea xmlArea;
    private HorizontalSplitPanel split;

    @UiField("statusLabel")
    private Label statusLabel;

    @UiField("resultPanel")
    private Panel resultPanel;

    @Override
    public void init(VaadinRequest request) {
        // Create the content from xml.
        split = (HorizontalSplitPanel) Clara
                .create("DemoApplication.xml", this);
        setContent(split);

        // Initial update
        controller = new DemoController();
        updateResultPanel((String) xmlArea.getValue());
    }

    /**
     * Returns the content of {@code demo-layout.xml} as a {@link String}.
     */
    @UiDataSource("xmlArea")
    public Property<String> readStartingPoint() {
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

    @UiHandler("xmlArea")
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
        try {
            Component c = Clara.create(
                    new ByteArrayInputStream(xml.getBytes()), controller);
            resultPanel.setContent(c);
        } catch (BinderException e) {
            Notification.show(e.getMessage(), Notification.Type.ERROR_MESSAGE);
        }
    }
}
