package org.vaadin.teemu.clara.binder;

@SuppressWarnings("serial")
public class BinderException extends RuntimeException {

    public BinderException(String message) {
        super(message);
    }

    public BinderException(Throwable cause) {
        super(cause);
    }

}
