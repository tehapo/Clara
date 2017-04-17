package org.vaadin.teemu.clara.inflater.handler;

import static org.vaadin.teemu.clara.util.ReflectionUtils.listMethods;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.vaadin.teemu.clara.inflater.filter.AttributeContext;
import org.vaadin.teemu.clara.inflater.filter.AttributeFilter;
import org.vaadin.teemu.clara.inflater.filter.AttributeFilterException;
import org.vaadin.teemu.clara.inflater.parser.AttributeParser;
import org.vaadin.teemu.clara.inflater.parser.ComponentPositionParser;
import org.vaadin.teemu.clara.inflater.parser.EnumAttributeParser;
import org.vaadin.teemu.clara.inflater.parser.PrimitiveAttributeParser;
import org.vaadin.teemu.clara.inflater.parser.VaadinAttributeParser;
import org.vaadin.teemu.clara.util.MethodsByDeprecationComparator;
import org.vaadin.teemu.clara.util.ReflectionUtils.ParamCount;

import com.vaadin.ui.Component;
import com.vaadin.ui.Field;

public class AttributeHandler {

    private final List<AttributeParser> attributeParsers = new ArrayList<AttributeParser>();
    private final List<AttributeFilter> attributeFilters;

    public AttributeHandler(List<AttributeFilter> attributeFilters) {
        this(attributeFilters, Collections.<AttributeParser> emptyList());
    }

    public AttributeHandler(List<AttributeFilter> attributeFilters,
            List<AttributeParser> extraAttributeParsers) {
        this.attributeFilters = attributeFilters;

        // Setup the default AttributeParsers.
        attributeParsers.add(new PrimitiveAttributeParser());
        attributeParsers.add(new VaadinAttributeParser());
        attributeParsers.add(new EnumAttributeParser());
        attributeParsers.add(new ComponentPositionParser());
        // Add extra AttributeParsers
        attributeParsers.addAll(extraAttributeParsers);
    }

    /**
     * Returns the namespace of attributes this {@link AttributeHandler} is
     * interested in.
     *
     * @return XML namespace this handler is responsible for.
     */
    public String getNamespace() {
        return ""; // default namespace
    }

    /**
     * Assigns the given attributes to the given {@link Component}.
     *
     * @param component
     *            {@link Component} instance to assign the attributes.
     * @param attributes
     *            {@link Map} of attributes to assign.
     */
    public void assignAttributes(Component component,
            Map<String, String> attributes) {

        if (attributes.isEmpty()) {
            return;
        }

        try {
            for (Map.Entry<String, String> attribute : attributes.entrySet()) {
                Method setter = getWriteMethod(attribute.getKey(),
                        component.getClass());
                if (setter != null) {
                    if (setter.getParameterTypes().length == 0) {
                        // Setter method without any parameters.
                        setter.invoke(component);
                    } else {
                        AttributeParser parser = getParserFor(setter
                                .getParameterTypes()[0]);
                        if (parser != null) {
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
                                Class<?> valueType = setter.getParameterTypes()[0];
                                if (component instanceof Field
                                        && valueType == Object.class
                                        && setter.getName().equals("setValue")) {
                                    // Special handling for Field.setValue with
                                    // unknown type -> get the actual generic
                                    // type of the field.
                                    valueType = (Class<?>) (((ParameterizedType) component
                                            .getClass().getGenericSuperclass())
                                            .getActualTypeArguments()[0]);
                                }
                                invokeWithAttributeFilters(setter, component,
                                        parser.getValueAs(attributeValue,
                                                valueType, component));
                            }
                        }
                    }
                }
            }
        } catch (SecurityException e) {
            throw new AttributeHandlerException(e);
        } catch (IllegalArgumentException e) {
            throw new AttributeHandlerException(e);
        } catch (IllegalAccessException e) {
            throw new AttributeHandlerException(e);
        } catch (InvocationTargetException e) {
            throw new AttributeHandlerException(e.getCause());
        }
    }

    protected void invokeWithAttributeFilters(final Method methodToInvoke,
            final Object obj, final Object... args)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {

        if (attributeFilters.isEmpty()) {
            methodToInvoke.invoke(obj, args);
        } else {
            final LinkedList<AttributeFilter> filtersCopy = new LinkedList<AttributeFilter>(
                    attributeFilters);
            AttributeFilter firstFilter = filtersCopy.pop();
            firstFilter.filter(new AttributeContext(methodToInvoke,
                    args.length > 1 ? args[1] : args[0]) {

                @Override
                public void proceed() throws AttributeFilterException {
                    if (filtersCopy.size() > 0) {
                        // More filters -> invoke them.
                        filtersCopy.pop().filter(this);
                    } else {
                        // No more filters -> time to invoke the actual
                        // method.
                        try {
                            if (args.length > 1) {
                                methodToInvoke.invoke(obj, args[0],
                                        this.getValue());
                            } else {
                                methodToInvoke.invoke(obj, this.getValue());
                            }
                        } catch (IllegalAccessException e) {
                            throw new AttributeFilterException(e);
                        } catch (InvocationTargetException e) {
                            throw new AttributeFilterException(e);
                        }
                    }
                }
            });
        }
    }

    protected AttributeParser getParserFor(Class<?> type) {
        for (AttributeParser parser : attributeParsers) {
            if (parser.isSupported(type)) {
                return parser;
            }
        }
        return null;
    }

    protected static String getWriteMethodName(String propertyName) {
        if (propertyName.length() > 0) {
            // For example: "sizeFull" -> "setSizeFull"
            return "set" + propertyName.substring(0, 1).toUpperCase()
                    + propertyName.substring(1);
        }
        return "";
    }

    protected Method getWriteMethod(String propertyName,
            Class<? extends Component> componentClass) {
        List<Method> writeMethods = listMethods(componentClass,
                getWriteMethodName(propertyName), ParamCount.fromTo(0, 1));
        return getPreferredMethod(writeMethods);
    }

    protected Method getPreferredMethod(List<Method> methods) {
        if (methods == null || methods.isEmpty()) {
            return null;
        }

        // Sort to find the preferred method.
        Collections.sort(methods, new ParserAwareMethodComparator());
        return methods.get(0);
    }

    /**
     * Comparator to sort {@link Method}s into a preferred ordering taking into
     * account available parsers in addition to method deprecation.
     */
    private class ParserAwareMethodComparator extends
            MethodsByDeprecationComparator {

        private Class<?> getPropertyClass(Method method) {
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length > 1
                    && parameterTypes[0] == Component.class) {
                // First parameter is the Component -> use the second one for
                // the property.
                return parameterTypes[1];
            }
            return parameterTypes[0];
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
