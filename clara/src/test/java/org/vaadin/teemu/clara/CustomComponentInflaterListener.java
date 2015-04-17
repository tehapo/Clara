package org.vaadin.teemu.clara;


import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import org.vaadin.teemu.clara.inflater.InflaterListener;

/**
 * Custom component that implements {@link InflaterListener}.
 * <p>
 * For testing purposes.
 * </p>
 */
public class CustomComponentInflaterListener extends CustomComponent
        implements InflaterListener{
    @Override
    public void componentInflated() {
        Component root = Clara.build()
                .withIdPrefix(getId() != null ? getId() + "_" : null)
                .withController(this)
                .createFrom("/hierarchy-with-ids.xml");
        setCompositionRoot(root);
    }

    @Override
    public Component getCompositionRoot() {
        return super.getCompositionRoot();
    }
}
