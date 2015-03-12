package org.vaadin.teemu.clara.inflater;

@SuppressWarnings("serial")
public class LayoutInflaterException extends RuntimeException {

    public LayoutInflaterException(String message) {
        super(message);
    }

    public LayoutInflaterException(String message, Throwable cause) {
        super(message, cause);
    }

    public LayoutInflaterException(Throwable cause) {
        super(cause);
    }

}
