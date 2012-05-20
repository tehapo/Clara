package org.vaadin.teemu.clara;

import java.io.IOException;
import java.io.InputStream;

import org.vaadin.teemu.clara.factory.ComponentFactory;
import org.vaadin.teemu.clara.factory.ComponentInstantiationException;
import org.vaadin.teemu.clara.factory.DefaultComponentFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class ViewInflater {

    private ComponentFactory componentFactory = new DefaultComponentFactory();

    public void setComponentFactory(ComponentFactory componentFactory) {
        this.componentFactory = componentFactory;
    }

    public InflatedCustomComponent inflate(InputStream xml)
            throws ViewInflaterException {
        try {
            // initialize content handler
            ViewInflaterContentHandler handler = new ViewInflaterContentHandler(
                    componentFactory);

            // parse the XML
            XMLReader parser = XMLReaderFactory.createXMLReader();
            parser.setContentHandler(handler);
            parser.parse(new InputSource(xml));

            // construct the result
            return new InflatedCustomComponent(handler.getIdMap(),
                    handler.getRoot());
        } catch (SAXException e) {
            throw new ViewInflaterException(e);
        } catch (IOException e) {
            throw new ViewInflaterException(e);
        } catch (ComponentInstantiationException e) {
            throw new ViewInflaterException(e.getMessage(), e);
        }
    }

}
