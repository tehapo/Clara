package org.vaadin.teemu.clara;

import java.util.Iterator;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;

public class Clara {

    public static Component findComponentById(Component root, String componentId) {
        // Check for null before doing anything.
        if (componentId == null) {
            throw new IllegalArgumentException("Component id must not be null.");
        }

        // Recursively traverse the whole component tree starting from the given
        // root component.
        if (componentId.equals(root.getDebugId())) {
            return root;
        } else if (root instanceof ComponentContainer) {
            // TODO change to HasComponents for Vaadin 7?
            for (Iterator<Component> i = ((ComponentContainer) root)
                    .getComponentIterator(); i.hasNext();) {
                Component c = findComponentById(i.next(), componentId);
                if (c != null) {
                    return c;
                }
            }
        }
        return null;
    }

}
