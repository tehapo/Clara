package org.vaadin.teemu.clara;

import java.io.IOException;
import java.io.InputStream;

import org.vaadin.teemu.clara.factory.ComponentInstantiationException;
import org.vaadin.teemu.clara.factory.ComponentManager;
import org.vaadin.teemu.clara.factory.DefaultComponentManager;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class LayoutInflater {

    private ComponentManager componentManager = new DefaultComponentManager();

    public void setComponentManager(ComponentManager componentManager) {
        this.componentManager = componentManager;
    }

    public InflatedCustomComponent inflate(InputStream xml)
            throws LayoutInflaterException {
        try {
            // initialize content handler
            LayoutInflaterContentHandler handler = new LayoutInflaterContentHandler(
                    componentManager);

            // parse the XML
            XMLReader parser = XMLReaderFactory.createXMLReader();
            parser.setContentHandler(handler);
            parser.parse(new InputSource(xml));

            // construct the result
            return new InflatedCustomComponent(handler.getIdMap(),
                    handler.getRoot());
        } catch (SAXException e) {
            throw new LayoutInflaterException(e);
        } catch (IOException e) {
            throw new LayoutInflaterException(e);
        } catch (ComponentInstantiationException e) {
            throw new LayoutInflaterException(e.getMessage(), e);
        }
    }

    public void addInterceptor(AttributeInterceptor attributeInterceptor) {
        componentManager.addInterceptor(attributeInterceptor);
    }

    public void removeInterceptor(AttributeInterceptor attributeInterceptor) {
        componentManager.removeInterceptor(attributeInterceptor);
    }

}
