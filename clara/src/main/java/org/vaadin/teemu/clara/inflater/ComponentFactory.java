package org.vaadin.teemu.clara.inflater;

import static org.vaadin.teemu.clara.util.ReflectionUtils.isComponent;

import com.vaadin.ui.Component;

public class ComponentFactory {

    /**
     * Returns a new {@link Component} instance of given {@code namespace} and
     * {@code name}. If the component cannot be instantiated properly a
     * {@link ComponentInstantiationException} is thrown.
     *
     * @param namespace
     *            the namespace of the component to create.
     * @param name
     *            the name of the component to create.
     * @return a new {@link Component} instance.
     * @throws ComponentInstantiationException
     *             if the component cannot be instantiated.
     */
    public Component createComponent(String namespace, String name)
            throws ComponentInstantiationException {
        try {
            Class<? extends Component> componentClass = resolveComponentClass(
                    namespace, name);
            Component newComponent = componentClass.newInstance();
            return newComponent;
        } catch (Exception e) {
            throw createException(e, namespace, name);
        }
    }

    @SuppressWarnings("unchecked")
    private Class<? extends Component> resolveComponentClass(String namespace,
            String name) throws ClassNotFoundException {
        String qualifiedClassName = namespace + "." + name;
        Class<?> componentClass = null;
        componentClass = Class.forName(qualifiedClassName);

        // Check that we're dealing with a Component.
        if (isComponent(componentClass)) {
            return (Class<? extends Component>) componentClass;
        } else {
            throw new IllegalArgumentException(String.format(
                    "Resolved class %s is not a %s.", componentClass.getName(),
                    Component.class.getName()));
        }
    }

    private ComponentInstantiationException createException(Exception e,
            String namespace, String name) {
        String message = String
                .format("Couldn't instantiate a component for namespace %s and name %s.",
                        namespace, name);
        if (e != null) {
            return new ComponentInstantiationException(message, e);
        } else {
            return new ComponentInstantiationException(message);
        }
    }

}
