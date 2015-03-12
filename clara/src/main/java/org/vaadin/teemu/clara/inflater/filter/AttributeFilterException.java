package org.vaadin.teemu.clara.inflater.filter;

@SuppressWarnings("serial")
public class AttributeFilterException extends RuntimeException {

    public AttributeFilterException(String message) {
        super(message);
    }

    public AttributeFilterException(String message, Throwable cause) {
        super(message, cause);
    }

    public AttributeFilterException(Throwable cause) {
        super(cause);
    }

}
