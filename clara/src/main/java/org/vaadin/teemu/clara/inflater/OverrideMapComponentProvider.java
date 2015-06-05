package org.vaadin.teemu.clara.inflater;

import com.vaadin.ui.Component;

import java.util.Map;

public class OverrideMapComponentProvider implements ComponentProvider {
    private final Map<String, Component> componentOverrideMap;

    /**
     * Constructor.
     *
     * @param componentOverrideMap
     *            the map with component instances to lookup by id.
     */
    public OverrideMapComponentProvider(
            Map<String, Component> componentOverrideMap) {
        this.componentOverrideMap = componentOverrideMap;
    }

    @Override
    public boolean isApplicableFor(String uri, String localName, String id) {
        return componentOverrideMap.containsKey(id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Component getComponent(String uri, String localName, String id)
            throws LayoutInflaterException {
        return componentOverrideMap.get(id);
    }
}
