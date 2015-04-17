package org.vaadin.teemu.clara.inflater;

import com.vaadin.ui.VerticalLayout;

/**
 * Layout for testing InflaterListener calls.
 */
public class VerticalLayoutWithInflaterListener extends VerticalLayout
        implements InflaterListener {

    private boolean componentInflatedCalled;
    private String idAfterInflate;
    private int componentCountAfterInflate;

    @Override
    public void componentInflated() {
        componentInflatedCalled = true;
        idAfterInflate = getId();
        componentCountAfterInflate = getComponentCount();
    }

    public boolean isComponentInflatedCalled() {
        return componentInflatedCalled;
    }

    public String getIdAfterInflate() {
        return idAfterInflate;
    }

    public int getComponentCountAfterInflate() {
        return componentCountAfterInflate;
    }
}
