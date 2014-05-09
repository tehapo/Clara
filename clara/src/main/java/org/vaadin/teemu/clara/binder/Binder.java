package org.vaadin.teemu.clara.binder;

import static org.vaadin.teemu.clara.util.ReflectionUtils.findMethods;

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
import org.vaadin.teemu.clara.util.MethodComparator;
import org.vaadin.teemu.clara.util.ReflectionUtils.ParamCount;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Component;

public class Binder {

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
     * @param controller
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
     * 
     * @see UiField
     */
    public Map<String, Component> getAlreadyAssignedFields(Object controller) {
        if (controller == null) {
            return Collections.emptyMap();
        }

        Map<String, Component> assignedFields = new HashMap<String, Component>();
        Field[] fields = controller.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(UiField.class)) {
                try {
                    field.setAccessible(true);
                    Object value = field.get(controller);
                    if (value instanceof Component) {
                        assignedFields.put(extractComponentId(field),
                                (Component) value);
                    }
                } catch (IllegalAccessException e) {
                    getLogger()
                            .log(Level.WARNING,
                                    "Exception while accessing controller object fields.",
                                    e);
                }
            }
        }
        return assignedFields;
    }

    private void bindFields(Component componentRoot, Object controller,
            Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(UiField.class)) {
                bindField(componentRoot, controller, field);
            }
        }
        Class<?> superclass = clazz.getSuperclass();
        if (superclass != null) {
            bindFields(componentRoot, controller, superclass);
        }
    }

    private void bindMethods(Component componentRoot, Object controller) {
        Method[] methods = controller.getClass().getMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(UiDataSource.class)) {
                bindDataSource(componentRoot, controller, method);
            }

            if (method.isAnnotationPresent(UiHandler.class)) {
                bindEventHandler(componentRoot, controller, method);
            }
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
                throw new BinderException(e);
            }
        }
    }

    private Object createListenerProxy(Class<?> listenerClass,
            final Class<?> eventClass, final Method listenerMethod,
            final Object controller) {
        Object proxy = Proxy.newProxyInstance(listenerClass.getClassLoader(),
                new Class<?>[] { listenerClass }, new InvocationHandler() {

                    @Override
                    public Object invoke(Object proxy, Method method,
                            Object[] args) throws Throwable {

                        if (args != null
                                && args.length > 0
                                && eventClass.isAssignableFrom(args[0]
                                        .getClass())) {
                            getLogger().fine(
                                    String.format(
                                            "Forwarding method call %s -> %s.",
                                            method.getName(),
                                            listenerMethod.getName()));
                            return listenerMethod.invoke(controller, args);
                        }
                        getLogger()
                                .fine(String.format(
                                        "Forwarding method call %s to %s.",
                                        method.getName(), controller.getClass()));
                        return method.invoke(controller, args);
                    }

                });
        getLogger().fine(
                String.format("Created a proxy for %s.", listenerClass));
        return proxy;
    }

    private Method getAddListenerMethod(
            Class<? extends Component> componentClass, Class<?> eventClass) {
        List<Method> addListenerCandidates = findMethods(componentClass,
                "add(.*)Listener", ParamCount.constant(1));
        Collections.sort(addListenerCandidates, new MethodComparator());

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
            throw new BinderException(e);
        }
    }

    private Component tryToFindComponentById(Component root, String id) {
        Component component = Clara.findComponentById(root, id);
        if (component == null) {
            throw new BinderException("No component found for id: " + id + ".");
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
