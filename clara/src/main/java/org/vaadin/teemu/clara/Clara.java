package org.vaadin.teemu.clara;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.vaadin.teemu.clara.binder.BinderException;
import org.vaadin.teemu.clara.inflater.ComponentProvider;
import org.vaadin.teemu.clara.inflater.LayoutInflaterException;
import org.vaadin.teemu.clara.inflater.filter.AttributeFilter;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.HasComponents;

public class Clara {

    /**
     * Creates an instance of {@link ClaraBuilder} that allows for more control
     * over instantion of components by Clara.
     *
     * @return Builder object
     */
    public static ClaraBuilder build() {
        return new ClaraBuilder();
    }

    /**
     * Returns a {@link Component} that is read from the XML representation
     * given as {@link InputStream}. If you would like to bind the resulting
     * {@link Component} to a controller object, you should use
     * {@link #create(InputStream, Object, AttributeFilter...)} method instead.
     *
     * @param xml
     *            XML representation.
     * @return a {@link Component} that is read from the XML representation.
     */
    public static Component create(InputStream xml) {
        return create(xml, null, new AttributeFilter[0]);
    }

    /**
     * Returns a {@link Component} that is read from the XML representation
     * given as {@link InputStream}. If you would like to bind the resulting
     * {@link Component} to a controller object, you should use
     * {@link #create(InputStream, Object, AttributeFilter...)} method instead.
     *
     * @param xml
     *            XML representation.
     * @param componentProviders
     *            additional component providers.
     * @return a {@link Component} that is read from the XML representation.
     */
    public static Component create(InputStream xml,
            ComponentProvider... componentProviders) {
        return create(xml, null, Arrays.asList(componentProviders));
    }

    /**
     * Returns a {@link Component} that is read from the XML representation
     * given as {@link InputStream} and binds the resulting {@link Component} to
     * the given {@code controller} object.
     * <p>
     * Optionally you may also provide {@link AttributeFilter}s to do some
     * modifications (or example localized translations) to any attributes
     * present in the XML representation.
     * </p>
     *
     * @param xml
     *            XML representation.
     * @param controller
     *            controller object to bind the resulting {@code Component} (
     *            {@code null} allowed).
     * @param attributeFilters
     *            optional {@link AttributeFilter}s to do attribute
     *            modifications.
     * @return a {@link Component} that is read from the XML representation and
     *         bound to the given {@code controller}.
     * @throws LayoutInflaterException
     *             if an error is encountered during the layout inflation.
     * @throws BinderException
     *             if an error is encountered during the binding.
     */
    public static Component create(InputStream xml, Object controller,
            AttributeFilter... attributeFilters) {
        return create(xml, controller,
                Collections.<ComponentProvider> emptyList(), attributeFilters);
    }

    /**
     * Returns a {@link Component} that is read from the XML representation
     * given as {@link InputStream} and binds the resulting {@link Component} to
     * the given {@code controller} object.
     * <p>
     * Optionally you may also provide {@link AttributeFilter}s to do some
     * modifications (or example localized translations) to any attributes
     * present in the XML representation.
     * </p>
     *
     * @param xml
     *            XML representation.
     * @param controller
     *            controller object to bind the resulting {@code Component} (
     *            {@code null} allowed).
     * @param componentProviders
     *            additional component providers.
     * @param attributeFilters
     *            optional {@link AttributeFilter}s to do attribute
     *            modifications.
     * @return a {@link Component} that is read from the XML representation and
     *         bound to the given {@code controller}.
     * @throws LayoutInflaterException
     *             if an error is encountered during the layout inflation.
     * @throws BinderException
     *             if an error is encountered during the binding.
     */
    public static Component create(InputStream xml, Object controller,
            List<ComponentProvider> componentProviders,
            AttributeFilter... attributeFilters) {
        return new ClaraBuilder().withController(controller)
                .withComponentProviders(componentProviders)
                .withAttributeFilters(attributeFilters).createFrom(xml);
    }

    /**
     * Returns a {@link Component} that is read from an XML file in the
     * classpath and binds the resulting {@link Component} to the given
     * {@code controller} object.
     * <p>
     * The filename is given either as a path relative to the class of the
     * {@code controller} object or as an absolute path. For example if you have
     * a {@code MyController.java} and {@code MyController.xml} files in the
     * same package, you can call this method like
     * {@code Clara.create("MyController.xml", new MyController())}.
     * </p>
     * <p>
     * Optionally you may also provide {@link AttributeFilter}s to do some
     * modifications (or example localized translations) to any attributes
     * present in the XML representation.
     * </p>
     *
     * @param xmlClassResourceFileName
     *            filename of the XML representation (within classpath, relative
     *            to {@code controller}'s class or absolute path).
     * @param controller
     *            controller object to bind the resulting {@code Component}
     *            (non-{@code null}).
     * @param attributeFilters
     *            optional {@link AttributeFilter}s to do attribute
     *            modifications.
     * @return a {@link Component} that is read from the XML representation and
     *         bound to the given {@code controller}.
     * @throws LayoutInflaterException
     *             if an error is encountered during the layout inflation.
     * @throws BinderException
     *             if an error is encountered during the binding.
     */
    public static Component create(String xmlClassResourceFileName,
            Object controller, AttributeFilter... attributeFilters) {
        return create(xmlClassResourceFileName, controller,
                Collections.<ComponentProvider> emptyList(), attributeFilters);
    }

    /**
     * Returns a {@link Component} that is read from an XML file in the
     * classpath and binds the resulting {@link Component} to the given
     * {@code controller} object.
     * <p>
     * The filename is given either as a path relative to the class of the
     * {@code controller} object or as an absolute path. For example if you have
     * a {@code MyController.java} and {@code MyController.xml} files in the
     * same package, you can call this method like
     * {@code Clara.create("MyController.xml", new MyController())}.
     * </p>
     * <p>
     * Optionally you may also provide {@link AttributeFilter}s to do some
     * modifications (or example localized translations) to any attributes
     * present in the XML representation.
     * </p>
     *
     * @param xmlClassResourceFileName
     *            filename of the XML representation (within classpath, relative
     *            to {@code controller}'s class or absolute path).
     * @param controller
     *            controller object to bind the resulting {@code Component}
     *            (non-{@code null}).
     * @param componentProviders
     *            additional component providers.
     * @param attributeFilters
     *            optional {@link AttributeFilter}s to do attribute
     *            modifications.
     * @return a {@link Component} that is read from the XML representation and
     *         bound to the given {@code controller}.
     * @throws LayoutInflaterException
     *             if an error is encountered during the layout inflation.
     * @throws BinderException
     *             if an error is encountered during the binding.
     */
    public static Component create(String xmlClassResourceFileName,
            Object controller, List<ComponentProvider> componentProviders,
            AttributeFilter... attributeFilters) {
        InputStream xml = controller.getClass().getResourceAsStream(
                xmlClassResourceFileName);
        return create(xml, controller, componentProviders, attributeFilters);
    }

    /**
     * Searches the given component hierarchy {@code root} for a
     * {@link Component} with the given {@code componentId} as its {@code id}
     * property (see {@link Component#setId(String)}).
     * <p>
     * If the given {@code root} is a {@link ComponentContainer}, this method
     * will recursively iterate the component hierarchy in search for the
     * correct {@link Component}. Otherwise if the given {@code root} is a
     * single {@link Component}, only it is checked for its {@code id} value.
     * </p>
     * <p>
     * <b>Warning</b>: if you use this method to search for a component created
     * with an id prefix (see {@link ClaraBuilder#withIdPrefix(String)}, then
     * this method will only find the component if the prefix is included in
     * {@code componentId}. You can also use
     * {@link #findComponentById(Component, String, String)}.
     * </p>
     *
     * @param root
     *            root of a component tree (non-{@code null}).
     * @param componentId
     *            {@code id} of a component to search for (non-{@code null}).
     * @return {@link Component} with a given {@code componentId} as its
     *         {@code id} or {@code null} if no such component is found.
     * @throws IllegalArgumentException
     *             if either of the given parameters is {@code null}.
     * @see Component#setId(String)
     * @see #findComponentById(Component, String, String)
     */
    public static Component findComponentById(Component root, String componentId) {
        // Check for null before doing anything.
        if (componentId == null) {
            throw new IllegalArgumentException("Component id must not be null.");
        }
        if (root == null) {
            throw new IllegalArgumentException(
                    "Root component must not be null.");
        }

        // Recursively traverse the whole component tree starting from the given
        // root component.
        if (componentId.equals(root.getId())) {
            return root;
        } else if (root instanceof HasComponents) {
            for (Component c : (HasComponents) root) {
                Component result = findComponentById(c, componentId);
                if (result != null) {
                    return result;
                }
            }
        }
        return null;
    }

    /**
     * Searches the given component hierarchy {@code root} for a
     * {@link Component} with the given {@code idPrefix} and {@code componentId}
     * as its {@code id} property (see {@link Component#setId(String)}).
     * <p>
     * If the given {@code root} is a {@link ComponentContainer}, this method
     * will recursively iterate the component hierarchy in search for the
     * correct {@link Component}. Otherwise if the given {@code root} is a
     * single {@link Component}, only it is checked for its {@code id} value.
     * </p>
     *
     * @param root
     *            root of a component tree (non-{@code null}).
     * @param idPrefix
     *            Prefix of the id (empty string is used when {@code null})
     * @param componentId
     *            {@code id} of a component to search for (non-{@code null}).
     * @return {@link Component} with a given {@code idPrefix} and
     *         {@code componentId} as its {@code id} or {@code null} if no such
     *         component is found.
     * @throws IllegalArgumentException
     *             if either of the given parameters is {@code null}.
     * @see Component#setId(String)
     * @see #findComponentById(Component, String, String)
     */
    public static Component findComponentById(Component root, String idPrefix,
            String componentId) {
        if (idPrefix != null && !"".equals(idPrefix)) {
            componentId = idPrefix + componentId;
        }

        return findComponentById(root, componentId);
    }

}
