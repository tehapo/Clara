package org.vaadin.teemu.clara.factory;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.vaadin.ui.Component;

public class DefaultComponentFactory implements ComponentFactory {

    private List<AttributeHandler> attributeHandlers = new ArrayList<AttributeHandler>();

    private Logger getLogger() {
        return Logger.getLogger(DefaultComponentFactory.class.getName());
    }

    public DefaultComponentFactory() {
        // Setup the default AttributeHandlers.
        addAttributeHandler(new PrimitiveAttributeHandler());
        addAttributeHandler(new VaadinAttributeHandler());
    }

    public void addAttributeHandler(AttributeHandler handler) {
        attributeHandlers.add(handler);
    }

    public void removeAttributeHandler(AttributeHandler handler) {
        attributeHandlers.remove(handler);
    }

    public Component createComponent(String namespace, String name,
            Map<String, String> attributes)
            throws ComponentInstantiationException {
        try {
            Class<? extends Component> componentClass = resolveComponentClass(
                    namespace, name);
            Component newComponent = componentClass.newInstance();
            handleAttributes(newComponent, attributes);
            return newComponent;
        } catch (Exception e) {
            throw createException(e, namespace, name);
        }
    }

    protected ComponentInstantiationException createException(Exception e,
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

    @SuppressWarnings("unchecked")
    protected Class<? extends Component> resolveComponentClass(
            String namespace, String name) throws ClassNotFoundException {
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

    /**
     * Returns {@code true} if the given {@link Class} implements the
     * {@link Component} interface of Vaadin Framework otherwise {@code false}.
     * 
     * @param componentClass
     *            {@link Class} to check against {@link Component} interface.
     * @return {@code true} if the given {@link Class} is a {@link Component},
     *         {@code false} otherwise.
     */
    protected boolean isComponent(Class<?> componentClass) {
        if (componentClass != null) {
            return Component.class.isAssignableFrom(componentClass);
        } else {
            return false;
        }
    }

    protected void handleAttributes(Component component,
            Map<String, String> attributes) {
        getLogger().fine(attributes.toString());

        try {
            // Get the Component properties.
            BeanInfo beanInfo = Introspector.getBeanInfo(component.getClass());
            PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();

            for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                for (PropertyDescriptor property : properties) {
                    if (property.getName().equals(attribute.getKey())) {
                        AttributeHandler handler = getHandlerFor(property
                                .getPropertyType());
                        if (handler != null) {
                            Method setter = property.getWriteMethod();
                            if (setter.getParameterTypes().length == 1) {
                                String attributeValue = attribute.getValue();
                                if (attributeValue == null
                                        || attributeValue.length() == 0) {
                                    // No need for conversion.
                                    setter.invoke(component, attributeValue);
                                } else {
                                    // Ask the AttributeHandler to convert the
                                    // value.
                                    setter.invoke(
                                            component,
                                            handler.getValueAs(
                                                    attributeValue,
                                                    setter.getParameterTypes()[0]));
                                }
                            }
                        }
                    }
                }
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
    }

    protected AttributeHandler getHandlerFor(Class<?> type) {
        for (AttributeHandler handler : attributeHandlers) {
            if (handler.isSupported(type)) {
                return handler;
            }
        }
        return null;
    }

}
