package org.vaadin.teemu.clara.inflater;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

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

public class LayoutInflater {

    private final LayoutInflaterContentHandler contentHandler = new LayoutInflaterContentHandler();

    public Component inflate(InputStream xml) throws LayoutInflaterException {
        try {
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
        for (AttributeHandler attributeHandler : contentHandler.attributeHandlers) {
            attributeHandler.addAttributeFilter(attributeFilter);
        }
    }

    public void removeAttributeFilter(AttributeFilter attributeFilter) {
        for (AttributeHandler attributeHandler : contentHandler.attributeHandlers) {
            attributeHandler.removeAttributeFilter(attributeFilter);
        }
    }

    private class LayoutInflaterContentHandler extends DefaultHandler {

        private static final String URN_NAMESPACE_ID = "package";
        private static final String DEFAULT_NAMESPACE = "urn:"
                + URN_NAMESPACE_ID + ":com.vaadin.ui";
        private static final String ID_ATTRIBUTE = "id";

        private Stack<Component> componentStack = new Stack<Component>();
        private ComponentContainer currentContainer;
        private Component root;
        private final ComponentFactory componentFactory;
        private final List<AttributeHandler> attributeHandlers;
        private final Set<String> assignedIds = new HashSet<String>();

        public LayoutInflaterContentHandler() {
            componentFactory = new ComponentFactory();
            attributeHandlers = new ArrayList<AttributeHandler>();
            attributeHandlers.add(new AttributeHandler());
            attributeHandlers.add(new LayoutAttributeHandler());
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

                Component component = instantiateComponent(uri, localName);

                if (root == null) {
                    // This was the first Component created -> root.
                    root = component;
                }
                if (currentContainer != null) {
                    currentContainer.addComponent(component);
                }

                handleAttributes(component, attributes);

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

        private Component instantiateComponent(String uri, String localName) {
            // Extract the package and class names.
            String packageName = uri
                    .substring(("urn:" + URN_NAMESPACE_ID + ":").length());
            String className = localName;

            return componentFactory.createComponent(packageName, className);
        }

        private void handleAttributes(Component component, Attributes attributes) {
            for (AttributeHandler attributeHandler : attributeHandlers) {
                // Get attributes for the namespace this AttributeHandler is
                // interested in.
                Map<String, String> attributeMap = getAttributeMap(attributes,
                        attributeHandler.getNamespace());
                attributeHandler.assignAttributes(component, attributeMap);
            }
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
