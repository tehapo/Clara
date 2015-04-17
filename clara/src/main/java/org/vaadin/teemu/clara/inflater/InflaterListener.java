package org.vaadin.teemu.clara.inflater;

/**
 * Listener interface for inflater actions.
 * <p>
 * An example use case is when a custom component needs to perform additional
 * actions on (after) creation that require the attributes to have been set.
 * </p>
 *
 * @author <a href="mailto:mrotteveel@bol.com">Mark Rotteveel</a>
 */
public interface InflaterListener {

    /**
     * Called by the inflater when all attributes and sub-components have been
     * created.
     */
    void componentInflated();
}
