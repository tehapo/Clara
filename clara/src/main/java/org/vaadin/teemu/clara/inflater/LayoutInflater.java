package org.vaadin.teemu.clara.inflater;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.vaadin.teemu.clara.inflater.parser.AttributeParser;
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

    static final String URN_NAMESPACE_ID = "import";
    static final String IMPORT_URN_PREFIX = "urn:" + URN_NAMESPACE_ID + ":";
    static final String DEFAULT_NAMESPACE = IMPORT_URN_PREFIX + "com.vaadin.ui";

    private List<AttributeFilter> attributeFilters = new ArrayList<AttributeFilter>();
    private List<AttributeParser> extraAttributeParsers = new ArrayList<AttributeParser>();

    protected Logger getLogger() {
        return Logger.getLogger(LayoutInflater.class.getName());
    }

    /**
     * Inflates the given {@code xml} into a {@link Component} (hierarchy).
     *
     * @param xml
     *            {@link InputStream} for the XML.
     * @return the inflated {@link Component} (hierarchy).
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
     *            {@link InputStream} for the XML.
     * @param componentOverrideMap
     *            {@link Map} of already existing {@link Component} instances
     *            from their {@code id} properties.
     * @return the inflated {@link Component} (hierarchy).
     * @throws LayoutInflaterException
     *             in case of an error in the inflation process.
     */
    public Component inflate(InputStream xml,
            Map<String, Component> componentOverrideMap) {
        return inflate(xml, componentOverrideMap, new ComponentProvider[0]);
    }

    /**
     * Inflates the given {@code xml} into a {@link Component} (hierarchy).
     *
     * @param xml
     *            {@link InputStream} for the XML.
     * @param componentOverrideMap
     *            {@link Map} of already existing {@link Component} instances
     *            from their {@code id} properties.
     * @return the inflated {@link Component} (hierarchy).
     * @throws LayoutInflaterException
     *             in case of an error in the inflation process.
     */
    public Component inflate(InputStream xml,
            Map<String, Component> componentOverrideMap,
            ComponentProvider... additionalComponentProviders) {
        List<ComponentProvider> providers = createDefaultComponentProviders(componentOverrideMap);
        providers.addAll(Arrays.asList(additionalComponentProviders));
        return inflate(xml,
                providers.toArray(new ComponentProvider[providers.size()]));
    }

    private List<ComponentProvider> createDefaultComponentProviders(
            Map<String, Component> componentOverrideMap) {
        List<ComponentProvider> providers = new ArrayList<ComponentProvider>();
        providers.add(new OverrideMapComponentProvider(componentOverrideMap));
        providers.add(new ReflectionComponentProvider());
        return providers;
    }

    /**
     * Inflates the given {@code xml} into a {@link Component} (hierarchy).
     *
     * @param xml
     *            the xml to inflate.
     * @param componentProviders
     *            the {@link ComponentProvider}s to apply in given order to
     *            inflate xml to components.
     * @return the inflated {@link Component} (hierarchy).
     * @throws LayoutInflaterException
     *             in case of an error in the inflation process.
     */
    public Component inflate(InputStream xml,
            ComponentProvider... componentProviders) {
        return inflate(xml, Arrays.asList(componentProviders));
    }

    /**
     * Inflates the given {@code xml} into a {@link Component} (hierarchy).
     *
     * @param xml
     *            the xml to inflate.
     * @param componentProviders
     *            the {@link ComponentProvider}s to apply in given order to
     *            inflate xml to components.
     * @return the inflated {@link Component} (hierarchy).
     * @throws LayoutInflaterException
     *             in case of an error in the inflation process.
     */
    public Component inflate(InputStream xml,
            List<ComponentProvider> componentProviders) {
        try {
            LayoutInflaterContentHandler contentHandler = new LayoutInflaterContentHandler(
                    componentProviders);

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

    public void addAttributeParser(AttributeParser attributeParser) {
        extraAttributeParsers.add(attributeParser);
    }

    private class LayoutInflaterContentHandler extends DefaultHandler {

        private static final String ID_ATTRIBUTE = "id";

        private Stack<Component> componentStack = new Stack<Component>();
        private ComponentContainer currentContainer;
        private Component root;
        private final AttributeHandler attributeHandler;
        private final LayoutAttributeHandler layoutAttributeHandler;
        private final Set<String> assignedIds = new HashSet<String>();
        private final List<ComponentProvider> componentProviders;

        public LayoutInflaterContentHandler(
                List<ComponentProvider> componentProviders) {
            this.componentProviders = componentProviders;

            attributeHandler = new AttributeHandler(attributeFilters,
                    extraAttributeParsers);
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

            String id = attributes.getValue(ID_ATTRIBUTE);

            ComponentProvider componentProvider = findApplicableComponentProvider(
                    uri, localName, id);

            verifyUniqueId(attributes);

            Component component = instantiateComponent(componentProvider, uri,
                    localName, id);

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

        private ComponentProvider findApplicableComponentProvider(String uri,
                String localName, String id) {
            for (ComponentProvider provider : componentProviders) {
                if (provider.isApplicableFor(uri, localName, id)) {
                    return provider;
                }
            }

            throw new LayoutInflaterException(
                    String.format(
                            "None of the component providers is "
                                    + "capable to provide a component for uri=%s, localName=%s, id=%s",
                            uri, localName, id));
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
            if (component instanceof InflaterListener) {
                ((InflaterListener) component).componentInflated();
            }
        }

        private Component instantiateComponent(
                ComponentProvider componentProvider, String uri,
                String localName, String id) {
            for (ComponentProvider provider : componentProviders) {
                Component component = provider.getComponent(uri, localName, id);
                if (component != null) {
                    return component;
                }
            }

            throw new LayoutInflaterException(
                    String.format(
                            "None of the component providers was "
                                    + "able to provide component for uri=%s, localName=%s, id=%s",
                            uri, localName, id));
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
