package org.vaadin.teemu.clara;

@SuppressWarnings("serial")
public class ViewInflaterException extends RuntimeException {

    public ViewInflaterException(String message) {
        super(message);
    }

    public ViewInflaterException(String message, Throwable e) {
        super(message, e);
    }

    public ViewInflaterException(Throwable e) {
        super(e);
    }

}
