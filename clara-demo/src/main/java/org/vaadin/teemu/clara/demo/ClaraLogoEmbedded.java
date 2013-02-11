package org.vaadin.teemu.clara.demo;

import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Embedded;

@SuppressWarnings("serial")
public class ClaraLogoEmbedded extends Embedded {

    public ClaraLogoEmbedded() {
        super(null, new ThemeResource("clara-logo-180x180.png"));
        // Reduce the size to support retina resolution.
        setHeight("90px");
        setWidth("90px");
    }

}
