package org.vaadin.teemu.clara.inflater;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;

class LayoutInflaterContentHandler extends DefaultHandler {

    private static final String URN_NAMESPACE_ID = "package";
    private static final String DEFAULT_NAMESPACE = "urn:" + URN_NAMESPACE_ID
            + ":com.vaadin.ui";
    private static final String LAYOUT_ATTRIBUTE_NAMESPACE = "urn:vaadin:layout";
    private static final String ID_ATTRIBUTE = "id";

    private Stack<Component> componentStack = new Stack<Component>();
    private ComponentContainer currentContainer;
    private Component root;
    private final ComponentFactory componentFactory;
    private final AttributeHandler attributeHandler;
    private final Set<String> assignedIds = new HashSet<String>();

    public LayoutInflaterContentHandler() {
        componentFactory = new ComponentFactory();
        attributeHandler = new AttributeHandler();
    }

    public Component getRoot() {
        return root;
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

        if (uri == null || uri.length() == 0) {
            uri = DEFAULT_NAMESPACE;
        }

        if (uri.startsWith("urn:" + URN_NAMESPACE_ID + ":")) {
            // Get a Map of the attributes and throw an exception if the id it
            // not unique.
            Map<String, String> attributeMap = getAttributeMap(attributes);
            verifyUniqueId(attributeMap);

            // Extract the package and class names.
            String packageName = uri
                    .substring(("urn:" + URN_NAMESPACE_ID + ":").length());
            String className = localName;

            Component component = componentFactory.createComponent(packageName,
                    className);
            attributeHandler.assignAttributes(component, attributeMap);

            if (root == null) {
                // This was the first Component created -> root.
                root = component;
            }
            if (currentContainer != null) {
                currentContainer.addComponent(component);

                // Handle layout attributes.
                Map<String, String> layoutAttributeMap = getAttributeMap(
                        attributes, LAYOUT_ATTRIBUTE_NAMESPACE);
                attributeHandler.assignLayoutAttributes(currentContainer,
                        component, layoutAttributeMap);
            }
            if (component instanceof ComponentContainer) {
                currentContainer = (ComponentContainer) component;
            }
            componentStack.push(component);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        super.endElement(uri, localName, qName);
        Component component = componentStack.pop();
        if (component instanceof ComponentContainer) {
            currentContainer = (ComponentContainer) component.getParent();
        }
    }

    AttributeHandler getAttributeHandler() {
        return attributeHandler;
    }

    private void verifyUniqueId(Map<String, String> attributes)
            throws LayoutInflaterException {
        String id = attributes.get(ID_ATTRIBUTE);
        if (id != null && id.length() > 0) {
            boolean unique = assignedIds.add(id);
            if (!unique) {
                throw new LayoutInflaterException(String.format(
                        "Given id %s has already been assigned.", id));
            }
        }
    }

    private Map<String, String> getAttributeMap(Attributes attributes) {
        return getAttributeMap(attributes, null);
    }

    private Map<String, String> getAttributeMap(Attributes attributes,
            String namespace) {
        Map<String, String> attributeMap = new HashMap<String, String>(
                attributes.getLength());
        for (int i = 0; i < attributes.getLength(); i++) {
            boolean matchesNamespace = (namespace == null || attributes.getURI(
                    i).equals(namespace));

            // Inclusion depends on the layoutAttributes parameter value.
            if (matchesNamespace) {
                String value = attributes.getValue(i);
                String name = attributes.getLocalName(i);
                attributeMap.put(name, value);
            }
        }
        return attributeMap;
    }
}
