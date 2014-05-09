package org.vaadin.teemu.clara.inflater;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Logger;

import org.vaadin.teemu.clara.inflater.filter.AttributeFilter;
import org.vaadin.teemu.clara.inflater.handler.AttributeHandler;
import org.vaadin.teemu.clara.inflater.handler.LayoutAttributeHandler;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.SingleComponentContainer;

public class LayoutInflater {

    private List<AttributeFilter> attributeFilters = new ArrayList<AttributeFilter>();

    protected Logger getLogger() {
        return Logger.getLogger(LayoutInflater.class.getName());
    }

    /**
     * Inflates the given {@code xml} into a {@link Component} (hierarchy).
     * 
     * @param xml
     * @return the inflated {@link Component} (hierarchy).
     * 
     * @throws LayoutInflaterException
     *             in case of an error in the inflation process.
     */
    public Component inflate(InputStream xml) {
        Map<String, Component> empty = Collections.emptyMap();
        return inflate(xml, empty);
    }

    /**
     * Inflates the given {@code xml} into a {@link Component} (hierarchy).
     * 
     * @param xml
     * @param componentOverrideMap
     *            {@link Map} of already existing {@link Component} instances
     *            from their {@code id} properties.
     * @return the inflated {@link Component} (hierarchy).
     * 
     * @throws LayoutInflaterException
     *             in case of an error in the inflation process.
     */
    public Component inflate(InputStream xml,
            Map<String, Component> componentOverrideMap) {
        try {
            LayoutInflaterContentHandler contentHandler = new LayoutInflaterContentHandler(
                    componentOverrideMap);

            // Parse the XML and return root Component.
            XMLReader parser = XMLReaderFactory.createXMLReader();
            parser.setContentHandler(contentHandler);
            parser.parse(new InputSource(xml));
            return contentHandler.root;
        } catch (SAXException e) {
            throw new LayoutInflaterException(e);
        } catch (IOException e) {
            throw new LayoutInflaterException(e);
        } catch (ComponentInstantiationException e) {
            throw new LayoutInflaterException(e.getMessage(), e);
        }
    }

    public void addAttributeFilter(AttributeFilter attributeFilter) {
        attributeFilters.add(attributeFilter);
    }

    public void removeAttributeFilter(AttributeFilter attributeFilter) {
        attributeFilters.remove(attributeFilter);
    }

    private class LayoutInflaterContentHandler extends DefaultHandler {

        private static final String URN_NAMESPACE_ID = "import";
        private static final String DEFAULT_NAMESPACE = "urn:"
                + URN_NAMESPACE_ID + ":com.vaadin.ui";
        private static final String ID_ATTRIBUTE = "id";

        private Stack<Component> componentStack = new Stack<Component>();
        private ComponentContainer currentContainer;
        private Component root;
        private final ComponentFactory componentFactory;
        private final AttributeHandler attributeHandler;
        private final LayoutAttributeHandler layoutAttributeHandler;
        private final Set<String> assignedIds = new HashSet<String>();
        private final Map<String, Component> componentOverrideMap;

        public LayoutInflaterContentHandler(
                Map<String, Component> componentOverrideMap) {
            this.componentOverrideMap = componentOverrideMap;

            componentFactory = new ComponentFactory();
            attributeHandler = new AttributeHandler(attributeFilters);
            layoutAttributeHandler = new LayoutAttributeHandler(
                    attributeFilters);
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            assignedIds.clear();
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            if (uri.length() == 0) {
                uri = DEFAULT_NAMESPACE;
            }

            if (uri.startsWith("urn:" + URN_NAMESPACE_ID + ":")) {
                // Throw an exception if the id is already used.
                verifyUniqueId(attributes);

                Component component = instantiateComponent(uri, localName,
                        attributes.getValue(ID_ATTRIBUTE));

                if (root == null) {
                    // This was the first Component created -> root.
                    root = component;
                }

                // Basic attributes -> attach -> layout attributes.
                handleAttributes(component, attributes, attributeHandler);
                attachComponent(component);
                handleAttributes(component, attributes, layoutAttributeHandler);

                if (component instanceof ComponentContainer) {
                    currentContainer = (ComponentContainer) component;
                }
                componentStack.push(component);
            }
        }

        private void attachComponent(Component component) {
            Component topComponent = componentStack.isEmpty() ? null
                    : componentStack.peek();
            if (topComponent instanceof SingleComponentContainer) {
                ((SingleComponentContainer) topComponent).setContent(component);
            } else if (currentContainer != null) {
                currentContainer.addComponent(component);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            super.endElement(uri, localName, qName);
            Component component = componentStack.pop();
            if (component instanceof ComponentContainer) {
                Component parent = component.getParent();
                while (parent != null
                        && !(parent instanceof ComponentContainer)) {
                    parent = parent.getParent();
                }
                currentContainer = (ComponentContainer) parent;
            }
        }

        private Component instantiateComponent(String uri, String localName,
                String id) {
            // Check if we should use an override.
            if (componentOverrideMap.containsKey(id)) {
                return componentOverrideMap.get(id);
            }

            // Extract the package and class names.
            String packageName = uri
                    .substring(("urn:" + URN_NAMESPACE_ID + ":").length());
            String className = localName;

            return componentFactory.createComponent(packageName, className);
        }

        private void handleAttributes(Component component,
                Attributes attributes, AttributeHandler attributeHandler) {
            // Get attributes for the namespace this AttributeHandler is
            // interested in.
            Map<String, String> attributeMap = getAttributeMap(attributes,
                    attributeHandler.getNamespace());
            attributeHandler.assignAttributes(component, attributeMap);
        }

        private void verifyUniqueId(Attributes attributes)
                throws LayoutInflaterException {
            String id = attributes.getValue(ID_ATTRIBUTE);
            if (id != null && id.length() > 0) {
                boolean unique = assignedIds.add(id);
                if (!unique) {
                    throw new LayoutInflaterException(String.format(
                            "Given id %s has already been assigned.", id));
                }
            }
        }

        private Map<String, String> getAttributeMap(Attributes attributes,
                String namespace) {
            Map<String, String> attributeMap = new HashMap<String, String>(
                    attributes.getLength());
            for (int i = 0; i < attributes.getLength(); i++) {
                if (attributes.getURI(i).equals(namespace)) {
                    // Namespace matches -> add to map.
                    String value = attributes.getValue(i);
                    String name = attributes.getLocalName(i);
                    attributeMap.put(name, value);
                }
            }
            return attributeMap;
        }
    }
}
