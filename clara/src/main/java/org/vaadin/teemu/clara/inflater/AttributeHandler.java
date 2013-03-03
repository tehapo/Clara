package org.vaadin.teemu.clara.inflater;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.vaadin.teemu.clara.inflater.filter.AttributeContext;
import org.vaadin.teemu.clara.inflater.filter.AttributeFilter;
import org.vaadin.teemu.clara.inflater.parser.AttributeParser;
import org.vaadin.teemu.clara.inflater.parser.EnumAttributeParser;
import org.vaadin.teemu.clara.inflater.parser.PrimitiveAttributeParser;
import org.vaadin.teemu.clara.inflater.parser.VaadinAttributeParser;
import org.vaadin.teemu.clara.util.ReflectionUtils;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;

public class AttributeHandler {

    private List<AttributeParser> attributeParsers = new ArrayList<AttributeParser>();
    private List<AttributeFilter> attributeFilters = new ArrayList<AttributeFilter>();

    private Logger getLogger() {
        return Logger.getLogger(AttributeHandler.class.getName());
    }

    public AttributeHandler() {
        // Setup the default AttributeHandlers.
        addAttributeParser(new PrimitiveAttributeParser());
        addAttributeParser(new VaadinAttributeParser());
        addAttributeParser(new EnumAttributeParser());
    }

    public void addAttributeParser(AttributeParser handler) {
        attributeParsers.add(handler);
    }

    public void removeAttributeParser(AttributeParser handler) {
        attributeParsers.remove(handler);
    }

    public void addAttributeFilter(AttributeFilter attributeFilter) {
        attributeFilters.add(attributeFilter);
    }

    public void removeAttributeFilter(AttributeFilter attributeFilter) {
        attributeFilters.remove(attributeFilter);
    }

    /**
     * Assigns the given attributes to the given {@link Component}.
     * 
     * @param component
     * @param attributes
     */
    public void assignAttributes(Component component,
            Map<String, String> attributes) {
        getLogger().fine(attributes.toString());

        try {
            for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                Method setter = resolveSetterMethod(attribute.getKey(),
                        component.getClass());
                if (setter != null) {
                    if (setter.getParameterTypes().length == 0) {
                        // Setter method without any parameters.
                        setter.invoke(component);
                    } else {
                        AttributeParser handler = getParserFor(setter
                                .getParameterTypes()[0]);
                        if (handler != null) {
                            // We have a handler that knows how to handle
                            // conversion
                            // for this property.
                            String attributeValue = attribute.getValue();
                            if (attributeValue == null
                                    || attributeValue.length() == 0) {
                                // No need for conversion.
                                invokeWithAttributeFilters(setter, component,
                                        attributeValue);
                            } else {
                                // Ask the AttributeHandler to convert the
                                // value.
                                invokeWithAttributeFilters(setter, component,
                                        handler.getValueAs(attributeValue,
                                                setter.getParameterTypes()[0]));
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
        }
    }

    /**
     * Assigns the layout related attributes (for example alignment and expand
     * ratio) of the given {@link Component} in the given
     * {@link ComponentContainer}.
     * 
     * @param layout
     * @param component
     * @param attributes
     * @throws IllegalStateException
     *             if the given {@code container} isn't the parent of the given
     *             {@code component}.
     */
    public void assignLayoutAttributes(ComponentContainer container,
            Component component, Map<String, String> attributes) {
        if (!component.getParent().equals(container)) {
            throw new IllegalStateException(
                    "The given container must be the parent of given component.");
        }
        try {
            for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                Method layoutMethod = resolveLayoutSetterMethod(
                        container.getClass(), attribute.getKey());
                if (layoutMethod != null) {
                    AttributeParser handler = getParserFor(layoutMethod
                            .getParameterTypes()[1]);
                    if (handler != null) {
                        invokeWithAttributeFilters(layoutMethod, container,
                                component, handler.getValueAs(
                                        attribute.getValue(),
                                        layoutMethod.getParameterTypes()[1]));
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

    private void invokeWithAttributeFilters(final Method methodToInvoke,
            final Object obj, final Object... args)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {

        if (attributeFilters.isEmpty()) {
            methodToInvoke.invoke(obj, args);
        } else {
            final LinkedList<AttributeFilter> filtersCopy = new LinkedList<AttributeFilter>(
                    attributeFilters);
            AttributeFilter filter = filtersCopy.pop();
            filter.filter(new AttributeContext(methodToInvoke,
                    args.length > 1 ? args[1] : args[0]) {

                @Override
                public void proceed() throws Exception {
                    if (filtersCopy.size() > 0) {
                        // More filters -> invoke them.
                        filtersCopy.pop().filter(this);
                    } else {
                        // No more filters -> time to invoke the actual
                        // method.
                        if (args.length > 1) {
                            methodToInvoke.invoke(obj, args[0], this.getValue());
                        } else {
                            methodToInvoke.invoke(obj, this.getValue());
                        }
                    }
                }
            });
        }
    }

    private AttributeParser getParserFor(Class<?> type) {
        for (AttributeParser parser : attributeParsers) {
            if (parser.isSupported(type)) {
                return parser;
            }
        }
        return null;
    }

    private static String capitalize(String propertyName) {
        if (propertyName.length() > 0) {
            return propertyName.substring(0, 1).toUpperCase()
                    + propertyName.substring(1);
        }
        return "";
    }

    private Method resolveSetterMethod(String propertyName,
            Class<? extends Component> componentClass) {
        String methodToLookFor = "set" + capitalize(propertyName);
        Set<Method> writeMethods = ReflectionUtils
                .getMethodsByNameAndParamCount(componentClass, methodToLookFor,
                        1);
        if (!writeMethods.isEmpty()) {
            return selectPreferredMethod(writeMethods, 0);
        } else {
            // Try with setters without any parameters, like
            // setSizeFull, setSizeUndefined, etc.
            writeMethods = ReflectionUtils.getMethodsByNameAndParamCount(
                    componentClass, methodToLookFor, 0);
            if (writeMethods.size() == 1) {
                return writeMethods.iterator().next();
            }
        }
        return null;
    }

    private Method resolveLayoutSetterMethod(
            Class<? extends ComponentContainer> layoutClass, String propertyName) {
        String methodToLookFor = "set" + capitalize(propertyName);
        Set<Method> settersWithTwoParams = ReflectionUtils
                .getMethodsByNameAndParamCount(layoutClass, methodToLookFor, 2);
        return selectPreferredMethod(settersWithTwoParams, 1);
    }

    private Method selectPreferredMethod(Set<Method> methods, int dataParamIndex) {
        Method candidate = null;
        for (Method method : methods) {
            if (dataParamIndex > 0
                    && !ReflectionUtils
                            .isComponent(method.getParameterTypes()[0])) {
                // First parameter must be a Component.
                continue;
            }

            Class<?> parameterType = method.getParameterTypes()[dataParamIndex];
            AttributeParser handler = getParserFor(parameterType);

            if (handler != null
                    && !(handler instanceof PrimitiveAttributeParser)) {
                // We found a setter method that we have a special
                // AttributeParser for.
                return method;
            }

            if (method.isAnnotationPresent(Deprecated.class)
                    || !parameterType.equals(String.class)) {
                // Prefer non-deprecated setters and those that directly
                // accept String as their parameters without any
                // conversion.
                candidate = method;
            } else {
                return method;
            }

        }
        // Did not found a perfect method -> fallback to the candidate.
        return candidate;
    }

}
