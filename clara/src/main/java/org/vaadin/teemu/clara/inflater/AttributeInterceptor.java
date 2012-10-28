package org.vaadin.teemu.clara.inflater;

import com.vaadin.ui.Component;

public interface AttributeInterceptor {

    /**
     * This method is called before an attribute value is set to a
     * {@link Component} allowing manipulation of the value by calling
     * {@link AttributeContext#setValue(Object)}.
     * 
     * <br />
     * <br />
     * Call the {@link AttributeContext#proceed()} to proceed to the next
     * {@link AttributeInterceptor} and to finally set the value if no other
     * {@link AttributeInterceptor}s exist. If you do not call the
     * {@link AttributeContext#proceed()} method, the value will never be set
     * for the {@link Component}.
     * 
     * @param invocationContext
     */
    void intercept(AttributeContext invocationContext);

}
