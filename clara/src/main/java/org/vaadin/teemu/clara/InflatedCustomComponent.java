package org.vaadin.teemu.clara;

import java.util.Map;

import org.vaadin.teemu.clara.binder.ComponentMapper;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;

/**
 * {@link CustomComponent} that is inflated from a declarative representation by
 * the {@link LayoutInflater}.
 */
@SuppressWarnings("serial")
public class InflatedCustomComponent extends CustomComponent implements
        ComponentMapper {

    private final Map<String, Component> idMap;

    InflatedCustomComponent(Map<String, Component> idMap, Component root) {
        super(root);
        this.idMap = idMap;
    }

    public Component getComponentById(String id) {
        return idMap.get(id);
    }

}
