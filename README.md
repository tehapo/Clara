## Clara - Declarative UI and Bindings for Vaadin Framework

Purpose of this project is to define a declarative XML-based syntax for defining [Vaadin](https://vaadin.com) user interfaces. Also another goal is to provide support annotation-based binding of data sources and event handlers.

Project also serves as a part of my Master's thesis at the University of Turku and also derives from the work done by Joonas Lehtinen on his [xmlui Vaadin add-on](http://vaadin.com/addon/xmlui).

## License

The project is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

## Online Demo

The project is still very experimental and documentation is minimal at this point. If you still are interested to try things out you should see the [demo application](http://teemu.virtuallypreinstalled.com/clara) demonstrating the current version of the project.

## Quickstart

```java
// Get XML file from classpath.
InputStream xmlLayout = getClass().getClassLoader().getResourceAsStream("xml-layout.xml");

// Instantiate a new ViewInflater instance and inflate the XML to a CustomComponent.
ViewInflater inflater = new ViewInflater();
InflatedCustomComponent layout = inflater.inflate(xmlLayout);

// Now the inflated layout is ready to be used.
getMainWindow().addComponent(layout);

// Optional:
// Bind to a POJO (myController) that has methods annotated with @DataSource or
// @EventHandler annotations.
Binder binder = new Binder();
binder.bind(layout, myController);
```
