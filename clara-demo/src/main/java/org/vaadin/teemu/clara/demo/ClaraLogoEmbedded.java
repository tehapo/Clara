package org.vaadin.teemu.clara.demo;

import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Embedded;

@SuppressWarnings("serial")
public class ClaraLogoEmbedded extends Embedded {

    public ClaraLogoEmbedded() {
        super(null, new ThemeResource("clara-logo-simplified-90x90.png"));
        setHeight("90px");
        setWidth("90px");
    }

}
