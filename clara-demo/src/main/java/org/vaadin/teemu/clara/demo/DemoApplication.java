package org.vaadin.teemu.clara.demo;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.vaadin.teemu.clara.Clara;
import org.vaadin.teemu.clara.inflater.LayoutInflaterException;

import com.vaadin.Application;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.AbstractTextField.TextChangeEventMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class DemoApplication extends Application {

    private DemoController controller;
    private TextArea xmlArea;
    private HorizontalSplitPanel split = new HorizontalSplitPanel();
    private Window mainWindow;
    private Label statusLabel;
    private ResultPanel resultPanel = new ResultPanel();

    @Override
    public void init() {
        setTheme("clara");
        setMainWindow(mainWindow = new Window());

        controller = new DemoController(mainWindow);
        mainWindow.setContent(split);

        CssLayout editor = new CssLayout();
        editor.setSizeFull();
        editor.setMargin(false, false, true, true);
        editor.addComponent(statusLabel = new Label());
        statusLabel.setStyleName("status");
        statusLabel.addStyleName("error");
        editor.addComponent(xmlArea = createXmlArea());

        HorizontalLayout wrapper = new HorizontalLayout();
        wrapper.setMargin(true);
        wrapper.setSizeFull();
        wrapper.addComponent(createLogo());
        wrapper.addComponent(editor);
        wrapper.setExpandRatio(editor, 1.0f);
        split.setFirstComponent(wrapper);
        split.setSecondComponent(resultPanel);
        resultPanel.setSizeFull();
        updateLayout();
    }

    private Component createLogo() {
        Embedded logo = new Embedded(null, new ThemeResource(
                "clara-logo-simplified-90x90.png"));
        logo.setHeight("90px");
        logo.setWidth("90px");
        return logo;
    }

    private TextArea createXmlArea() {
        TextArea area = new TextArea();
        area.setStyleName("xml-area");
        area.setCaption("XML");
        area.setSizeFull();
        area.setValue(readStartingPoint()); // initial value
        area.setTextChangeEventMode(TextChangeEventMode.LAZY);
        area.setTextChangeTimeout(500);
        area.addListener(new TextChangeListener() {

            @Override
            public void textChange(TextChangeEvent event) {
                updateLayout(event.getText());
            }
        });
        return area;
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

    private void setErrorStatus(String message) {
        statusLabel.setValue(message);
        statusLabel.addStyleName("error");
    }

    private void setOkStatus(String message) {
        statusLabel.setValue(message);
        statusLabel.removeStyleName("error");
    }

    private void updateLayout() {
        updateLayout(xmlArea.getValue().toString());
    }

    private void updateLayout(String xml) {
        try {
            long startTime = System.currentTimeMillis();
            Component c = Clara.create(
                    new ByteArrayInputStream(xml.getBytes()), controller);
            long endTime = System.currentTimeMillis();

            resultPanel.setContent(c);
            setOkStatus("Layout updated in " + (endTime - startTime) + "ms.");
        } catch (LayoutInflaterException e) {
            setErrorStatus(e.getMessage());
        }
    }

    private static class ResultPanel extends CustomComponent {

        private Panel contentPanel;

        public ResultPanel() {
            VerticalLayout marginWrapper = new VerticalLayout();
            marginWrapper.setMargin(true);
            marginWrapper.setSizeFull();
            setCompositionRoot(marginWrapper);

            contentPanel = new Panel("Result");
            contentPanel.setStyleName("result");
            contentPanel.setSizeFull();
            marginWrapper.addComponent(contentPanel);
        }

        public void setContent(Component content) {
            contentPanel.removeAllComponents();
            contentPanel.addComponent(content);
        }
    }
}
