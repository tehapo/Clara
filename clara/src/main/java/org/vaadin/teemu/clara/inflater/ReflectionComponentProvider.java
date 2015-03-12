package org.vaadin.teemu.clara.inflater;

import com.vaadin.ui.Component;

public class ReflectionComponentProvider implements ComponentProvider {
  private static final String URN_NAMESPACE_ID = "import";
  public static final String IMPORT_URN_PREFIX = "urn:" + URN_NAMESPACE_ID + ":";
  private final ComponentFactory componentFactory;

  /**
   * Constructor.
   */
  public ReflectionComponentProvider() {
    componentFactory = new ComponentFactory();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Component getComponent(String uri, String localName, String id)
      throws LayoutInflaterException {
    if (!uri.startsWith(IMPORT_URN_PREFIX)) {
        return null;
    }

    // Extract the package and class names.
    String packageName = uri.substring(IMPORT_URN_PREFIX.length());
    String className = localName;

    return componentFactory.createComponent(packageName, className);

  }
}
