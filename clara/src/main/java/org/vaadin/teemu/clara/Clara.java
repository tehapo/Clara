package org.vaadin.teemu.clara;

import java.io.InputStream;
import java.util.Iterator;

import org.vaadin.teemu.clara.binder.Binder;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;

public class Clara {

    /**
     * TODO documentation
     * 
     * @param xml
     * @return
     */
    public static Component create(InputStream xml) {
        return create(xml, null);
    }

    /**
     * TODO documentation
     * 
     * @param xml
     * @param controller
     * @param interceptors
     * @return
     */
    public static Component create(InputStream xml, Object controller,
            AttributeInterceptor... interceptors) {
        // Inflate the XML to a component (tree).
        LayoutInflater inflater = new LayoutInflater();
        if (interceptors != null) {
            for (AttributeInterceptor interceptor : interceptors) {
                inflater.addInterceptor(interceptor);
            }
        }
        Component result = inflater.inflate(xml);

        // Bind to controller if one is given.
        if (controller != null) {
            Binder binder = new Binder();
            binder.bind(result, controller);
        }
        return result;
    }

    /**
     * TODO documentation
     * 
     * @param root
     * @param componentId
     * @return
     */
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
