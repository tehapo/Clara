![Clara](https://github.com/tehapo/Clara/raw/99386831b5c2f3fc1e916180b1b60c796c2cd0ad/dist/img/clara-logo-150x174.png)

## Clara - Declarative UI and Bindings for Vaadin Framework

Purpose of this project is to define a declarative XML-based syntax for defining [Vaadin](https://vaadin.com) user interfaces. Also another goal is to provide support annotation-based binding of data sources and event handlers.

Project also serves as a part of my Master's thesis at the University of Turku and also derives from the work done by Joonas Lehtinen on his [xmlui Vaadin add-on](http://vaadin.
com/addon/xmlui).

Maven is used to build the add-on and the demo application modules (thanks to [vdemeester](https://github.com/vdemeester)). [Travis CI](http://travis-ci.org/) is used for automated testing.

To package and install the Clara add-on to your local repository, just run the following command:
```bash
mvn install
```

If you want to run and/or package the demo application, you must also compile the widgetset.
```bash
cd clara-demo
mvn gwt:compile jetty:run
```

Packaging the distributable add-on zip (that can be uploaded to Vaadin Directory) can be done as follows.
```bash
cd clara
mvn clean package assembly:single
```


[![Build Status](https://secure.travis-ci.org/tehapo/Clara.png)](http://travis-ci.org/tehapo/Clara)

## License

The project is licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).

## Online Demo

The project is still very experimental and documentation is minimal at this point. If you still are interested to try things out you should see the [demo application](http://teemu.virtuallypreinstalled.com/clara) demonstrating the current version of the project.

## Quickstart

**Quickstart is written for Clara 0.3.0. Please notice that at this point anything and everything can change in future releases.**

1) Create a new Vaadin project.

2) Download the latest version of Clara to WEB-INF/lib from Vaadin Directory (or use the Maven dependency).

3) Create a layout definition in XML. See the example below. Notice the special "urn:vaadin:layout" namespace on the componentAlignment attribute which means that the componentAlignment property belongs to the containing layout instead of the component itself.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<VerticalLayout xmlns="urn:package:com.vaadin.ui" xmlns:l="urn:vaadin:layout">
    <Label id="my-label" value="Hello Clara!" />
    <Button id="my-button" caption="Click me!" width="200px" l:componentAlignment="MIDDLE_CENTER" />
</VerticalLayout>
```

4) Save this XML file as a resource in your classpath as ```MyFirstClaraLayout.xml```. Next you should create a plain old Java class with the name ```MyFirstClaraController``` and add it to the same Java package as the XML file. Binding this controller to the components in the XML layout is done with annotations. Add the following two methods to your controller class.

```java
// The value "my-button" of the annotation is a reference to the id attribute in the XML layout.
@EventHandler("my-button")
public void handleMyButtonClick(ClickEvent event) {
    event.getButton().getApplication().getMainWindow()
            .showNotification("Clicked!");
}

@DataSource("my-label")
public Property getLabelProperty() {
    return new ObjectProperty<String>("Hello from Controller!",
            String.class);
}
```

5) Now you can instantiate the XML definition to a Vaadin component in your Java code. See the example below.

```java
// Use the static "create" method to instantiate the XML into a Vaadin component.
Clara.create("MyFirstClaraLayout.xml", new MyFirstClaraController());

// Now the layout is ready to be used.
getMainWindow().setContent(layout);
```

## Internationalization with Attribute Interceptors

Clara 0.2.0 introduced concept of attribute interceptors. Attribute interceptors enable runtime modification of any attributes read from the declarative XML layout file. The most obvious use case for this is to provide internationalization of text displayed in the user interface.

To create an attribute interceptor, you must implement the single-method ```AttributeInterceptor``` interface and pass it to the ```Clara.create``` method. The sole method in the interface is called ```intercept``` and it takes a single argument of type ```AttributeContext```. You can modify the value before it's assigned  by calling the ```setValue``` method of the ```AttributeContext```. You should always call the ```proceed``` method to pass the value forward to next interceptor (or to finally assign the value). If you do not call the ```proceed``` method, the attribute value will never be assigned (which might sometimes be the desired effect).

Simple example of an ```AttributeInterceptor``` implementation:

```java
AttributeInterceptor interceptor = new AttributeInterceptor() {

    @Override
    public void intercept(AttributeContext attributeContext) {
        if (attributeContext.getValue().getClass() == String.class) {
            String value = (String) attributeContext.getValue();
            if (value.startsWith("{i18n:")) {
                String translatedValue = getTranslation(value);
                attributeContext.setValue(translatedValue);
            }
        }
        try {
            attributeContext.proceed();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
};
```