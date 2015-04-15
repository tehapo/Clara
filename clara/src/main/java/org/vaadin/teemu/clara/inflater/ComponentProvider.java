package org.vaadin.teemu.clara.inflater;

import com.vaadin.ui.Component;

public interface ComponentProvider {
  /**
   * Provides components given the uri, localName and/or id.
   *
   * @param uri the namespace URI of the node describing the requested component.
   * @param localName the node name of the node describing the requested component.
   * @param id the id of the requested component, or <code>null</code> if no id supplied.
   * @return the component, based on uri, localName and/or id.
   * @throws LayoutInflaterException on failure to provide the component.
   */
  Component getComponent(String uri, String localName, String id) throws LayoutInflaterException;
}
