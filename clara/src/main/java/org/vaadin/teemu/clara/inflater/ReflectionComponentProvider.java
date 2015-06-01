package org.vaadin.teemu.clara.inflater;

import com.vaadin.ui.Component;

import static org.vaadin.teemu.clara.inflater.LayoutInflater.IMPORT_URN_PREFIX;

public class ReflectionComponentProvider implements ComponentProvider {

    private final ComponentFactory componentFactory = new ComponentFactory();

    @Override
    public boolean isApplicableFor(String uri, String localName, String id) {
        return uri.startsWith(IMPORT_URN_PREFIX);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component getComponent(String uri, String localName, String id)
            throws LayoutInflaterException {
        if (!uri.startsWith(IMPORT_URN_PREFIX)) {
            return null;
        }

        // Extract the package and class names.
        String packageName = uri.substring(IMPORT_URN_PREFIX.length());
        String className = localName;

        return componentFactory.createComponent(packageName, className);

    }
}
