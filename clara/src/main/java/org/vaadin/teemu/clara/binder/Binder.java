package org.vaadin.teemu.clara.binder;

import static org.vaadin.teemu.clara.util.ReflectionUtils.findMethods;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.vaadin.teemu.clara.Clara;
import org.vaadin.teemu.clara.binder.annotation.UiDataSource;
import org.vaadin.teemu.clara.binder.annotation.UiField;
import org.vaadin.teemu.clara.binder.annotation.UiHandler;
import org.vaadin.teemu.clara.util.MethodsByDeprecationComparator;
import org.vaadin.teemu.clara.util.ReflectionUtils;
import org.vaadin.teemu.clara.util.ReflectionUtils.ParamCount;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Component;

public class Binder {

    private final String idPrefix;

    public Binder() {
        this(null);
    }

    public Binder(String idPrefix) {
        this.idPrefix = idPrefix != null ? idPrefix : "";
    }

    protected Logger getLogger() {
        return Logger.getLogger(Binder.class.getName());
    }

    /**
     * Binds fields and methods of the given {@code controller} instance to
     * {@link Component}s found in the given {@code componentRoot} component
     * hierarchy. The binding is defined by the annotations {@link UiField},
     * {@link UiHandler} and {@link UiDataSource}.
     *
     * @param componentRoot
     *            root of a {@link Component} hierarchy.
     * @param controller
     *            controller instance with annotations defining some bindings.
     *
     * @throws BinderException
     *             if an error is encountered during the binding.
     *
     * @see UiField
     * @see UiHandler
     * @see UiDataSource
     */
    public void bind(Component componentRoot, Object controller) {
        if (controller == null) {
            return;
        }

        bindFields(componentRoot, controller, controller.getClass());
        bindMethods(componentRoot, controller);
    }

    /**
     * Returns a {@link Map} from {@link String} id to {@link Component} of all
     * controller fields decorated with the {@link UiField} annotation that
     * already have a {@link Component} reference assigned. If the given
     * controller is {@code null}, an empty {@link Map} is returned.
     *
     * @param controller
     *            controller instance which can have {@link UiField} annotated
     *            fields.
     * @return a {@link Map} containing already assigned fields with
     *         {@link UiField} annotation.
     *
     * @see UiField
     */
    public Map<String, Component> getAlreadyAssignedFields(Object controller) {
        if (controller == null) {
            return Collections.emptyMap();
        }

        Map<String, Component> assignedFields = new HashMap<String, Component>();
        for (Field field : ReflectionUtils.getAllDeclaredFieldsAnnotatedWith(
                controller.getClass(), UiField.class)) {
            try {
                field.setAccessible(true);
                Object value = field.get(controller);
                if (value instanceof Component) {
                    // We are intentionally not using the idPrefix here
                    // The specific use in the inflater doesn't need the
                    // prefix
                    assignedFields.put(extractComponentId(field),
                            (Component) value);
                }
            } catch (IllegalAccessException e) {
                getLogger().log(Level.WARNING,
                        "Exception while accessing controller object fields.",
                        e);
            }
        }
        return assignedFields;
    }

    private void bindFields(Component componentRoot, Object controller,
            Class<?> clazz) {
        for (Field field : ReflectionUtils.getAllDeclaredFieldsAnnotatedWith(
                controller.getClass(), UiField.class)) {
            bindField(componentRoot, controller, field);
        }
    }

    private void bindMethods(Component componentRoot, Object controller) {
        for (Method method : ReflectionUtils
                .getAllDeclaredMethodsAnnotatedWith(controller.getClass(),
                        UiDataSource.class)) {
            bindDataSource(componentRoot, controller, method);
        }

        for (Method method : ReflectionUtils
                .getAllDeclaredMethodsAnnotatedWith(controller.getClass(),
                        UiHandler.class)) {
            bindEventHandler(componentRoot, controller, method);
        }
    }

    private void bindField(Component componentRoot, Object controller,
            Field field) {
        String componentId = extractComponentId(field);
        Component component = tryToFindComponentById(componentRoot, componentId);

        try {
            field.setAccessible(true);
            if (field.get(controller) == null) {
                field.set(controller, component);
            }
        } catch (IllegalArgumentException e) {
            throw new BinderException(e);
        } catch (IllegalAccessException e) {
            throw new BinderException(e);
        }
    }

    /**
     * Returns id of the {@link Component} that the given {@link Field} should
     * be bound to. Assumes that the given {@code field} has {@link UiField}
     * annotation. This should be checked before calling this method.
     *
     * @return id of the component to bind the field.
     */
    private String extractComponentId(Field field) {
        // Try the id from UiField annotation.
        UiField annotation = field.getAnnotation(UiField.class);
        String componentId = annotation.value();

        if (componentId.length() == 0) {
            // Default to the field name instead of annotated id.
            componentId = field.getName();
        }
        return componentId;
    }

    /**
     * Expects that the given {@link Method} is annotated with {@link UiHandler}
     * annotation.
     *
     * @param componentRoot
     * @param controller
     * @param method
     */
    private void bindEventHandler(Component componentRoot, Object controller,
            Method method) {
        String componentId = method.getAnnotation(UiHandler.class).value();
        Component component = tryToFindComponentById(componentRoot, componentId);

        Class<?> eventType = (method.getParameterTypes().length > 0 ? method
                .getParameterTypes()[0] : null);
        if (eventType == null) {
            throw new BinderException(
                    "Couldn't figure out event type for method " + method + ".");
        }

        Method addListenerMethod = getAddListenerMethod(component.getClass(),
                eventType);
        if (addListenerMethod != null) {
            try {
                Object listener = createListenerProxy(
                        addListenerMethod.getParameterTypes()[0], eventType,
                        method, controller);
                addListenerMethod.invoke(component, listener);
            } catch (IllegalAccessException e) {
                throw new BinderException(e);
            } catch (InvocationTargetException e) {
                throw new BinderException(e.getCause());
            }
        }
    }

    private Object createListenerProxy(Class<?> listenerClass,
            final Class<?> eventClass, final Method listenerMethod,
            final Object controller) {
        Object proxy = Proxy.newProxyInstance(listenerClass.getClassLoader(),
                new Class<?>[] { listenerClass },
                new ListenerInvocationHandler(listenerMethod, eventClass,
                        controller));
        getLogger().fine(
                String.format("Created a proxy for %s.", listenerClass));
        return proxy;
    }

    private static class ListenerInvocationHandler implements
            InvocationHandler, Externalizable {

        private Method listenerMethod;
        private Class<?> eventClass;
        private Object controller;

        public ListenerInvocationHandler(Method listenerMethod,
                Class<?> eventClass, Object controller) {
            this.listenerMethod = listenerMethod;
            this.eventClass = eventClass;
            this.controller = controller;
        }

        public ListenerInvocationHandler() {
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            try {
                if (args != null && args.length > 0
                        && eventClass.isAssignableFrom(args[0].getClass())) {
                    getLogger()
                            .fine(String.format(
                                    "Forwarding method call %s -> %s.",
                                    method.getName(), listenerMethod.getName()));
                    return listenerMethod.invoke(controller, args);
                }
                getLogger().fine(
                        String.format("Forwarding method call %s to %s.",
                                method.getName(), controller.getClass()));
                return method.invoke(controller, args);
            } catch (InvocationTargetException ex) {
                throw ex.getCause();
            }
        }

        private Logger getLogger() {
            return Logger.getLogger(ListenerInvocationHandler.class.getName());
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(controller);
            out.writeObject(eventClass);
            out.writeObject(listenerMethod.getParameterTypes());
            out.writeObject(listenerMethod.getName());
            out.writeObject(listenerMethod.getDeclaringClass());
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException,
                ClassNotFoundException {
            controller = in.readObject();
            eventClass = (Class<?>) in.readObject();
            Class<?>[] parameterTypes = (Class<?>[]) in.readObject();
            String methodName = (String) in.readObject();
            Class<?> declaringClass = (Class<?>) in.readObject();
            try {
                listenerMethod = declaringClass.getDeclaredMethod(methodName,
                        parameterTypes);
            } catch (NoSuchMethodException ex) {
                throw new RuntimeException("Can't deserialize listener method "
                        + declaringClass.getCanonicalName() + ":" + methodName,
                        ex);
            }
        }

    }

    private Method getAddListenerMethod(
            Class<? extends Component> componentClass, Class<?> eventClass) {
        List<Method> addListenerCandidates = findMethods(componentClass,
                "add(.*)Listener", ParamCount.constant(1));
        Collections.sort(addListenerCandidates,
                new MethodsByDeprecationComparator());

        for (Method addListenerCandidate : addListenerCandidates) {
            // Check if this method accepts correct type of listeners.
            Class<?> listenerInterface = addListenerCandidate
                    .getParameterTypes()[0];

            if (findMethods(listenerInterface, ".*", eventClass).size() == 1) {
                // There exist a single method in the listener interface that
                // accepts our eventClass as its sole parameter -> our candidate
                // is accepted.
                return addListenerCandidate;
            }
        }
        return null;
    }

    /**
     * Expects that the given {@link Method} is annotated with
     * {@link UiDataSource} annotation.
     *
     * @param componentRoot
     * @param controller
     * @param method
     */
    private void bindDataSource(Component componentRoot, Object controller,
            Method method) {
        String componentId = method.getAnnotation(UiDataSource.class).value();
        Component component = tryToFindComponentById(componentRoot, componentId);
        Class<?> dataSourceClass = method.getReturnType();

        try {
            // Vaadin data model consists of Property/Item/Container
            // objects and each of them have a Viewer interface.
            if (isContainer(dataSourceClass)
                    && component instanceof Container.Viewer) {
                ((Container.Viewer) component)
                        .setContainerDataSource((Container) method
                                .invoke(controller));
            } else if (isProperty(dataSourceClass)
                    && component instanceof Property.Viewer) {
                ((Property.Viewer) component)
                        .setPropertyDataSource((Property<?>) method
                                .invoke(controller));
            } else if (isItem(dataSourceClass)
                    && component instanceof Item.Viewer) {
                ((Item.Viewer) component).setItemDataSource((Item) method
                        .invoke(controller));
            }
        } catch (IllegalAccessException e) {
            throw new BinderException(e);
        } catch (InvocationTargetException e) {
            throw new BinderException(e.getCause());
        }
    }

    private Component tryToFindComponentById(Component root, String id) {
        Component component = Clara.findComponentById(root, idPrefix, id);
        if (component == null) {
            throw new BinderException(
                    String.format(
                            "No component found for id: %1$s (%2$s%1$s).", id,
                            idPrefix));
        }
        return component;
    }

    private boolean isContainer(Class<?> dataSourceClass) {
        return Container.class.isAssignableFrom(dataSourceClass);
    }

    private boolean isItem(Class<?> dataSourceClass) {
        return Item.class.isAssignableFrom(dataSourceClass);
    }

    private boolean isProperty(Class<?> dataSourceClass) {
        return Property.class.isAssignableFrom(dataSourceClass);
    }
}
