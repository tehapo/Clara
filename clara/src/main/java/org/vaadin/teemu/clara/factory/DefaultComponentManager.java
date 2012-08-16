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
import java.util.Set;
import java.util.logging.Logger;

import org.vaadin.teemu.clara.util.ReflectionUtils;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;

public class DefaultComponentManager implements ComponentManager {

    private List<AttributeHandler> attributeHandlers = new ArrayList<AttributeHandler>();

    private Logger getLogger() {
        return Logger.getLogger(DefaultComponentManager.class.getName());
    }

    public DefaultComponentManager() {
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
        if (ReflectionUtils.isComponent(componentClass)) {
            return (Class<? extends Component>) componentClass;
        } else {
            throw new IllegalArgumentException(String.format(
                    "Resolved class %s is not a %s.", componentClass.getName(),
                    Component.class.getName()));
        }
    }

    protected void handleAttributes(Component component,
            Map<String, String> attributes) {
        getLogger().fine(attributes.toString());

        try {
            for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                Method setter = getSetter(attribute.getKey(),
                        component.getClass());
                if (setter != null) {
                    AttributeHandler handler = getHandlerFor(setter
                            .getParameterTypes()[0]);
                    if (handler != null) {
                        // We have a handler that knows how to handle conversion
                        // for this property.
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
                                    handler.getValueAs(attributeValue,
                                            setter.getParameterTypes()[0]));
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

    public void applyLayoutAttributes(ComponentContainer container,
            Component component, Map<String, String> attributes) {
        if (!component.getParent().equals(container)) {
            throw new IllegalStateException(
                    "The given container must be the parent of given component.");
        }
        try {
            for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                if (attribute.getKey().startsWith("layout_")) {
                    String layoutProperty = attribute.getKey().substring(
                            "layout_".length());
                    Method layoutMethod = getLayoutMethod(container.getClass(),
                            layoutProperty);
                    if (layoutMethod != null) {
                        AttributeHandler handler = getHandlerFor(layoutMethod
                                .getParameterTypes()[1]);
                        if (handler != null)
                            layoutMethod
                                    .invoke(container, component,
                                            handler.getValueAs(attribute
                                                    .getValue(), layoutMethod
                                                    .getParameterTypes()[1]));
                    }
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static Method getSetter(String propertyName,
            Class<? extends Component> componentClass)
            throws IntrospectionException {
        // Get the Component properties.
        BeanInfo beanInfo = Introspector.getBeanInfo(componentClass);
        PropertyDescriptor[] properties = beanInfo.getPropertyDescriptors();

        Method candidate = null;
        for (PropertyDescriptor property : properties) {
            if (property.getName().equals(propertyName)) {
                // Workaround to the overloaded setters in Vaadin components.
                Set<Method> setters = ReflectionUtils
                        .getMethodsByNameAndParamCount(componentClass, property
                                .getWriteMethod().getName(), 1);

                for (Method setter : setters) {
                    if (setter.isAnnotationPresent(Deprecated.class)
                            || !setter.getParameterTypes()[0]
                                    .equals(String.class)) {
                        // Prefer non-deprecated setters and those that directly
                        // accept String as their parameters without any
                        // conversion.
                        candidate = setter;
                    } else {
                        return setter;
                    }
                }
            }
        }
        return candidate;
    }

    private static Method getLayoutMethod(
            Class<? extends ComponentContainer> layoutClass, String propertyName) {
        String methodToLookFor = "set"
                + propertyName.substring(0, 1).toUpperCase()
                + propertyName.substring(1);
        Set<Method> settersWithTwoParams = ReflectionUtils
                .getMethodsByNameAndParamCount(layoutClass, methodToLookFor, 2);

        Method candiateMethod = null;
        for (Method setter : settersWithTwoParams) {
            Class<?>[] params = setter.getParameterTypes();
            if (ReflectionUtils.isComponent(params[0])
                    && setter.getName().equals(methodToLookFor)) {
                if (!setter.isAnnotationPresent(Deprecated.class)) {
                    // Prefer non-deprecated methods.
                    return setter;
                } else {
                    // Use deprecated methods only if no other match found.
                    candiateMethod = setter;
                }
            }
        }
        return candiateMethod;
    }

}
