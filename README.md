![Clara](https://github.com/tehapo/Clara/raw/99386831b5c2f3fc1e916180b1b60c796c2cd0ad/dist/img/clara-logo-150x174.png)

## Clara - Declarative UI and Bindings for Vaadin Framework

Purpose of this project is to define a declarative XML-based syntax for defining [Vaadin](https://vaadin.com) user interfaces. Also another goal is to provide support annotation-based binding of data sources and event handlers. Starting from version 0.5.0 Clara supports only Vaadin 7. A separate [branch](https://github.com/tehapo/Clara/tree/vaadin-6) exist for Vaadin 6, but it is not maintained anymore.

Project also serves as a part of my Master's thesis at the University of Turku and also derives from the work done by Joonas Lehtinen on his [xmlui Vaadin add-on](http://vaadin.
com/addon/xmlui). A lot of the functionality is also inspired by GWT UiBinder.

Maven is used to build the add-on and the demo application modules (thanks to [vdemeester](https://github.com/vdemeester) for help). [Travis CI](http://travis-ci.org/) is used for automated testing.

To package and install the Clara add-on to your local repository, just run the following command:
```bash
mvn install
```

If you want to run and/or package the demo application, you must also compile the widgetset.
```bash
cd clara-demo
mvn vaadin:compile jetty:run
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

A good starting point to try things out is the online [demo application](http://teemu.virtuallypreinstalled.com/clara) demonstrating the current version of the project.

## Tools

 * [claraxsd-maven-plugin](https://github.com/kumm/claraxsd-maven-plugin) – A Maven plugin to generate XML schemas to enable code completion and validation.

## Examples

See the forked [SimpleAddressbook example](https://github.com/tehapo/SimpleAddressbook) that is modified to use Clara.

## Quickstart

**Quickstart is written for Clara 1.0.0.**

1) Create a new Vaadin 7 project.

2) Download the latest version of Clara to WEB-INF/lib from Vaadin Directory (or use the Maven dependency).

3) Create a new Java package that will contain the XML layout definition and the controller class (see steps 4 and 5).

4) Create a new XML file (name it ```MyFirstClaraLayout.xml```) for the layout definition and save it to the Java package you just created. You can copy the example below. Notice the special "urn:vaadin:parent" namespace on the componentAlignment attribute which means that the componentAlignment property belongs to the containing layout instead of the component itself.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<VerticalLayout xmlns="urn:import:com.vaadin.ui" xmlns:l="urn:vaadin:parent">
    <Label id="my-label" value="Hello Clara!" />
    <Button id="my-button" caption="Click me!" width="200px" l:componentAlignment="MIDDLE_CENTER" />
</VerticalLayout>
```

5) Create a plain old Java class with the name ```MyFirstClaraController``` and add it to the same Java package as the XML file. Binding this controller to the components in the XML layout is done with annotations. Add the following two methods to your controller class.

```java
// The value "my-button" of the annotation is a reference to the id attribute in the XML layout.
@UiHandler("my-button")
public void handleMyButtonClick(ClickEvent event) {
    Notification.show("Clicked!");
}

@UiDataSource("my-label")
public Property<String> getLabelProperty() {
    return new ObjectProperty<String>("Hello from Controller!",
            String.class);
}
```

By using the ```@UiField``` annotation on a field, you could bind a field directly to a component instance with a certain id. For example:

```java
@UiField("my-label")
private Label myLabel;
```

6) Now you can instantiate the XML definition to a Vaadin component in your Java code. See the example below.

```java
// Use the static "create" method to instantiate the XML into a Vaadin component.
VerticalLayout layout = (VerticalLayout) Clara.create(
        "MyFirstClaraLayout.xml", new MyFirstClaraController());

// Now the layout is ready to be used.
setContent(layout);
```

7) Congratulations, you just created your first application that uses Clara. As next steps you might want to see the other static methods contained in the ```Clara``` class to see more ways to use Clara.

## Internationalization with Attribute Filters

Attribute filters enable runtime modification of any attributes read from the declarative XML layout file. The most obvious use case for this is to provide internationalization of text displayed in the user interface.

To create an attribute filter, you must implement the single-method ```AttributeFilter``` interface and pass it to the ```Clara.create``` method. The sole method in the interface is called ```filter``` and it takes a single argument of type ```AttributeContext```. You can modify the value before it's assigned  by calling the ```setValue``` method of the ```AttributeContext```. You should always call the ```proceed``` method to pass the value forward to next filter (or to finally assign the value). If you do not call the ```proceed``` method, the attribute value will never be assigned (which might sometimes be the desired effect).

Simple example of an ```AttributeFilter``` implementation:

```java
AttributeFilter filter = new AttributeFilter() {

    @Override
    public void filter(AttributeContext attributeContext) {
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
