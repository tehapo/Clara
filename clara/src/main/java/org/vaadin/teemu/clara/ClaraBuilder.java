package org.vaadin.teemu.clara;

import com.vaadin.ui.Component;
import org.vaadin.teemu.clara.binder.Binder;
import org.vaadin.teemu.clara.binder.BinderException;
import org.vaadin.teemu.clara.inflater.LayoutInflater;
import org.vaadin.teemu.clara.inflater.LayoutInflaterException;
import org.vaadin.teemu.clara.inflater.filter.AttributeFilter;
import org.vaadin.teemu.clara.inflater.parser.AttributeParser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Builder for configuring Clara.
 *
 * @author <a href="mailto:mrotteveel@bol.com">Mark Rotteveel</a>
 */
public class ClaraBuilder {

    private Object controller;
    private final List<AttributeFilter> attributeFilters = new ArrayList<AttributeFilter>();
    private final List<AttributeParser> attributeParsers = new ArrayList<AttributeParser>();

    /**
     * Assigns the controller to the builder.
     *
     * @param controller
     *         Controller object for binding component fields ({@code null} allowed).
     * @return this builder
     */
    public ClaraBuilder withController(Object controller) {
        this.controller = controller;
        return this;
    }

    public Object getController() {
        return controller;
    }

    /**
     * Adds an attribute filter.
     *
     * @param filter
     *         Attribute filter
     * @return this builder
     */
    public ClaraBuilder withAttributeFilter(AttributeFilter filter) {
        attributeFilters.add(filter);
        return this;
    }

    /**
     * Adds attribute filters.
     *
     * @param filters
     *         Attribute filters
     * @return this builder
     */
    public ClaraBuilder withAttributeFilters(AttributeFilter... filters) {
        attributeFilters.addAll(Arrays.asList(filters));
        return this;
    }

    /**
     * @return Unmodifiable list of the current attribute filters
     */
    public List<AttributeFilter> getAttributeFilters() {
        return Collections.unmodifiableList(attributeFilters);
    }

    /**
     * Adds an attribute parser.
     * <p>
     * The attribute parsers added are in addition to the defaults parsers:
     * <ul>
     * <li>{@link org.vaadin.teemu.clara.inflater.parser.PrimitiveAttributeParser}</li>
     * <li>{@link org.vaadin.teemu.clara.inflater.parser.VaadinAttributeParser}</li>
     * <li>{@link org.vaadin.teemu.clara.inflater.parser.EnumAttributeParser}</li>
     * <li>{@link org.vaadin.teemu.clara.inflater.parser.ComponentPositionParser}</li>
     * </ul>
     * </p>
     *
     * @param parser
     *         Attribute parser
     * @return this builder
     */
    public ClaraBuilder withAttributeParser(AttributeParser parser) {
        attributeParsers.add(parser);
        return this;
    }

    /**
     * Adds attribute filters.
     *
     * @param parsers
     *         Attribute parsers
     * @return this builder
     * @see #withAttributeParser(AttributeParser)
     */
    public ClaraBuilder withAttributeParsers(AttributeParser... parsers) {
        attributeParsers.addAll(Arrays.asList(parsers));
        return this;
    }

    /**
     * @return Unmodifiable list of the current attribute parsers
     */
    public List<AttributeParser> getAttributeParsers() {
        return Collections.unmodifiableList(attributeParsers);
    }

    /**
     * Returns a {@link Component} that is read from the XML representation given
     * as {@link InputStream} and binds the resulting {@link Component} to the
     * {@code controller} object set in this builder.
     *
     * @param xml
     *         XML representation.
     * @return a {@link Component} that is read from the XML representation and
     * bound to the given {@code controller}.
     * @throws LayoutInflaterException
     *         if an error is encountered during the layout inflation.
     * @throws BinderException
     *         if an error is encountered during the binding.
     */
    public Component createFrom(InputStream xml) {
        Binder binder = new Binder();

        // Inflate the XML to a component (tree).
        LayoutInflater inflater = createInflater();
        Component result = inflater.inflate(xml,
                binder.getAlreadyAssignedFields(controller));

        // Bind to controller.
        binder.bind(result, controller);
        return result;
    }

    LayoutInflater createInflater() {
        LayoutInflater inflater = new LayoutInflater();
        for (AttributeFilter filter : attributeFilters) {
            inflater.addAttributeFilter(filter);
        }

        for (AttributeParser parser : attributeParsers) {
            inflater.addAttributeParser(parser);
        }

        return inflater;
    }
}
