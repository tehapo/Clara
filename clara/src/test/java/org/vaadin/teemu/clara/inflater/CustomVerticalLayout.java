package org.vaadin.teemu.clara.inflater;

import org.vaadin.teemu.clara.binder.annotation.UiField;

import com.vaadin.ui.Component;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class CustomVerticalLayout extends VerticalLayout {
    public static final TextField TEXT_FIELD = new TextField();

    @UiField
    private TextField textField = TEXT_FIELD;

    @Deprecated
    public void setExpandRatio(Component c, String e) {
        throw new RuntimeException("This method should not be called!");
    }

}
