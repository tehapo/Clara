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
    private Component currentComponent;
    private Component root;
    private final ComponentManager componentFactory;
    private final Set<String> assignedIds = new HashSet<String>();

    public LayoutInflaterContentHandler(ComponentManager componentFactory) {
        this.componentFactory = componentFactory;
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

        currentComponent = null;
        if (uri.startsWith("urn:" + URN_NAMESPACE_ID + ":")) {
            String packageName = uri
                    .substring(("urn:" + URN_NAMESPACE_ID + ":").length());
            String className = localName;

            // Get a Map of the attributes and throw an exception if the id it
            // not unique.
            Map<String, String> attributeMap = getAttributeMap(attributes,
                    false);
            verifyUniqueId(attributeMap);

            Map<String, String> layoutAttributeMap = getAttributeMap(
                    attributes, true);

            currentComponent = componentFactory.createComponent(packageName,
                    className, attributeMap);
            if (currentComponent.getId() != null) {
                assignedIds.add(currentComponent.getId());
            }
            if (root == null) {
                // This was the first Component created -> root.
                root = currentComponent;
            }
            if (currentContainer != null) {
                currentContainer.addComponent(currentComponent);
                componentFactory.applyLayoutAttributes(currentContainer,
                        currentComponent, layoutAttributeMap);
            }
            if (currentComponent instanceof ComponentContainer) {
                currentContainer = (ComponentContainer) currentComponent;
            }
            componentStack.push(currentComponent);
        }
    }

    private void verifyUniqueId(Map<String, String> attributes)
            throws LayoutInflaterException {
        String id = attributes.get(ID_ATTRIBUTE);
        if (id != null && id.length() > 0) {
            if (assignedIds.contains(id)) {
                throw new LayoutInflaterException(String.format(
                        "Given id %s has already been assigned.", id));
            }
        }
    }

    private Map<String, String> getAttributeMap(Attributes attributes,
            boolean layoutAttributes) {
        Map<String, String> attributeMap = new HashMap<String, String>(
                attributes.getLength());
        for (int i = 0; i < attributes.getLength(); i++) {
            // Is this attribute from the layout namespace.
            boolean isLayoutAttribute = attributes.getURI(i).equals(
                    LAYOUT_ATTRIBUTE_NAMESPACE);

            // Inclusion depends on the layoutAttributes parameter value.
            if ((layoutAttributes && isLayoutAttribute)
                    || (!layoutAttributes && !isLayoutAttribute)) {
                String value = attributes.getValue(i);
                String name = attributes.getLocalName(i);
                attributeMap.put(name, value);
            }
        }
        return attributeMap;
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

}
