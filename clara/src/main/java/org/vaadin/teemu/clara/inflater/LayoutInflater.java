package org.vaadin.teemu.clara.inflater;

import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.vaadin.ui.Component;

public class LayoutInflater {

    private ComponentManager componentManager = new DefaultComponentManager();

    public void setComponentManager(ComponentManager componentManager) {
        this.componentManager = componentManager;
    }

    public Component inflate(InputStream xml) throws LayoutInflaterException {
        try {
            // initialize content handler
            LayoutInflaterContentHandler handler = new LayoutInflaterContentHandler(
                    componentManager);

            // parse the XML
            XMLReader parser = XMLReaderFactory.createXMLReader();
            parser.setContentHandler(handler);
            parser.parse(new InputSource(xml));

            // construct the result
            return handler.getRoot();
        } catch (SAXException e) {
            throw new LayoutInflaterException(e);
        } catch (IOException e) {
            throw new LayoutInflaterException(e);
        } catch (ComponentInstantiationException e) {
            throw new LayoutInflaterException(e.getMessage(), e);
        }
    }

    public void addAttributeFilter(AttributeFilter attributeFilter) {
        componentManager.addAttributeFilter(attributeFilter);
    }

    public void removeAttributeFilter(AttributeFilter attributeFilter) {
        componentManager.removeAttributeFilter(attributeFilter);
    }

}
