package org.vaadin.teemu.clara.inflater;

import java.io.IOException;
import java.io.InputStream;

import org.vaadin.teemu.clara.inflater.filter.AttributeFilter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.vaadin.ui.Component;

public class LayoutInflater {

    private final LayoutInflaterContentHandler contentHandler = new LayoutInflaterContentHandler();

    public Component inflate(InputStream xml) throws LayoutInflaterException {
        try {
            // parse the XML
            XMLReader parser = XMLReaderFactory.createXMLReader();
            parser.setContentHandler(contentHandler);
            parser.parse(new InputSource(xml));

            // construct the result
            return contentHandler.getRoot();
        } catch (SAXException e) {
            throw new LayoutInflaterException(e);
        } catch (IOException e) {
            throw new LayoutInflaterException(e);
        } catch (ComponentInstantiationException e) {
            throw new LayoutInflaterException(e.getMessage(), e);
        }
    }

    public void addAttributeFilter(AttributeFilter attributeFilter) {
        contentHandler.getAttributeHandler().addAttributeFilter(attributeFilter);
    }

    public void removeAttributeFilter(AttributeFilter attributeFilter) {
        contentHandler.getAttributeHandler().removeAttributeFilter(attributeFilter);
    }

}
