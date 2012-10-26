package org.vaadin.teemu.clara;

import java.io.InputStream;
import java.util.Iterator;

import org.vaadin.teemu.clara.binder.Binder;
import org.vaadin.teemu.clara.inflater.LayoutInflater;

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
     * Searches the given component hierarchy {@code root} for the
     * {@link Component} with a given {@code componentId} as its debugId (see
     * {@link Component#setDebugId(String)}).
     * 
     * <br />
     * <br />
     * If the given {@code root} is a {@link ComponentContainer}, this method
     * will iterate the component hierarchy in search for the correct
     * {@link Component}. Otherwise if the given {@code root} is a single
     * {@link Component}, only it is checked for its debugId value.
     * 
     * @param root
     *            root of a component tree (non-{@code null}).
     * @param componentId
     *            debugId of a component to search for (non-{@code null}).
     * @return {@link Component} with a given {@code componentId} as its debugId
     *         or {@code null} if no such component is found.
     * @throws IllegalArgumentException
     *             if either of the given parameters is {@code null}.
     * @see Component#setDebugId(String)
     */
    public static Component findComponentById(Component root, String componentId) {
        // Check for null before doing anything.
        if (componentId == null) {
            throw new IllegalArgumentException("Component id must not be null.");
        }
        if (root == null) {
            throw new IllegalArgumentException(
                    "Root component must not be null.");
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
