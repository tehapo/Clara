package org.vaadin.teemu.clara.inflater;

import static org.vaadin.teemu.clara.util.ReflectionUtils.getMethodsByNameAndParamCountRange;
import static org.vaadin.teemu.clara.util.ReflectionUtils.getMethodsByNameAndParamTypes;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.vaadin.teemu.clara.inflater.filter.AttributeContext;
import org.vaadin.teemu.clara.inflater.filter.AttributeFilter;
import org.vaadin.teemu.clara.inflater.parser.AttributeParser;
import org.vaadin.teemu.clara.inflater.parser.EnumAttributeParser;
import org.vaadin.teemu.clara.inflater.parser.PrimitiveAttributeParser;
import org.vaadin.teemu.clara.inflater.parser.VaadinAttributeParser;
import org.vaadin.teemu.clara.util.AnyClassOrPrimitive;
import org.vaadin.teemu.clara.util.MethodComparator;

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
            throw new AttributeHandlerException(e);
        } catch (InvocationTargetException e) {
            throw new AttributeHandlerException(e);
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

    private static String setterNameFor(String propertyName) {
        if (propertyName.length() > 0) {
            // For example: "sizeFull" -> "setSizeFull"
            return "set" + propertyName.substring(0, 1).toUpperCase()
                    + propertyName.substring(1);
        }
        return "";
    }

    private Method resolveSetterMethod(String propertyName,
            Class<? extends Component> componentClass) {
        List<Method> writeMethods = getMethodsByNameAndParamCountRange(
                componentClass, setterNameFor(propertyName), 0, 1);

        if (writeMethods.size() > 0) {
            Collections.sort(writeMethods, new ParserAwareMethodComparator(0));
            return writeMethods.get(0);
        }
        return null;
    }

    private Method resolveLayoutSetterMethod(
            Class<? extends ComponentContainer> layoutClass, String propertyName) {
        // We need the first parameter to be a Component, the other one can be
        // anything.
        Class<?>[] expectedParameterTypes = { Component.class,
                AnyClassOrPrimitive.class };

        List<Method> settersWithTwoParams = getMethodsByNameAndParamTypes(
                layoutClass, setterNameFor(propertyName),
                expectedParameterTypes);
        if (settersWithTwoParams.size() > 0) {
            Collections.sort(settersWithTwoParams,
                    new ParserAwareMethodComparator(1));
            return settersWithTwoParams.get(0);
        }
        return null;
    }

    /**
     * Comparator to sort {@link Method}s into a preferred ordering taking into
     * account available parsers in addition to method deprecation.
     */
    private class ParserAwareMethodComparator extends MethodComparator {

        private final int propertyParameterIndex;

        public ParserAwareMethodComparator(int propertyParameterIndex) {
            this.propertyParameterIndex = propertyParameterIndex;
        }

        private Class<?> getPropertyClass(Method method) {
            return method.getParameterTypes()[propertyParameterIndex];
        }

        private boolean isSpecialAttributeParser(AttributeParser parser) {
            return parser != null
                    && !(parser instanceof PrimitiveAttributeParser);
        }

        @Override
        public int compare(Method method1, Method method2) {
            // Check for parsers.
            AttributeParser parser1 = getParserFor(getPropertyClass(method1));
            AttributeParser parser2 = getParserFor(getPropertyClass(method2));
            if (isSpecialAttributeParser(parser1)
                    && !isSpecialAttributeParser(parser2)) {
                return -1;
            }
            if (isSpecialAttributeParser(parser2)
                    && !isSpecialAttributeParser(parser1)) {
                return 1;
            }
            return super.compare(method1, method2);
        }
    }

}
