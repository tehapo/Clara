package org.vaadin.teemu.clara.inflater;

import com.vaadin.ui.Component;
import com.vaadin.ui.VerticalLayout;

@SuppressWarnings("serial")
public class CustomVerticalLayout extends VerticalLayout {

    @Deprecated
    public void setExpandRatio(Component c, String e) {
        throw new RuntimeException("This method should not be called!");
    }

}