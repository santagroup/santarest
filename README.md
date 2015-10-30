# SantaRest

Flexible library to ease HTTP/HTTPS requests execution. It can be used for Android and Java.

### What does SantaRest give?

1. Flexibility and easy usage (thanks to [Retrofit](http://square.github.io/retrofit/))
2. Networking code and responses handling decoupling (thanks to [Otto](http://square.github.io/otto/) and [EventBus](https://github.com/greenrobot/EventBus))

With the help of SantaRest you can create application with network communication but without callbacks and Android activity's life-cycle checking.
By relying on compile-time annotation processor that generates code for you, you can write clear maintainable code.

### Usage

To send and receive HTTP requests you should use SantaRest class
```java
santaRest = new SantaRest.Builder()
                 .setServerUrl("https://api.github.com")
                .build();
```

Each HTTP request in SantaRest is an individual class that contains all information about the request and response. Let's call it Action.

You should annotate action class with `@RestAction`. 
```java
@RestAction(value = "/demo", method = RestAction.Method.GET)
public class ExampleAction {

}
```

To process request, you can annotate Action fields with:
* `@Path` for path value
* `@Query` for request URL parameters
* `@Body` for POST request body
* `@RequestHeader` for request headers
* `@Field` for request fields if request type is `RestAction.Type.FORM_URL_ENCODED`
* `@Part` for multipart request parts

To process response, you may use special annotations:
* `@Response` for getting response body.
* `@Status` for getting response status. You can use `Integer`, `Long`, `int` or `long` fields for get status code or use `boolean` if you want to know if request was sent successfully
* `@ResponseHeader` for getting response headers

```java
@RestAction(value = "/demo/{examplePath}/info",
        type = RestAction.Type.SIMPLE,
        method = RestAction.Method.GET)
public class ExampleAction {
    @Path("examplePath")
    String ownerr;
    @Query("repo")
    int query;
    @RequestHeader("Example-Header-Name")
    String requestHeaderValue;
    @Status
    int statusCode;
    @Body
    ExampleModel exampleModel;
    @Response
    ExampleDataModel exampleDataModel;
    @ResponseHeader("Example-Responseheader-Name")
    String responseHeaderValue;
}
```

To send actions asynchronously, you should use method `sendAction` or `runAction` for synchronous calls.
```java
santaRest.sendAction(new ExampleAction());
santaRest.runAction(new ExampleAction());
```

To receive actions with responses, you should subscribe to events:
```java
santaRest.subscribe(this);
```
Don’t forget to unsubscribe by using:
```java
santaRest.unsubscribe(this);
```

For android, we recommend you to use `santaRest.subscribe()` and `santaRest.unsubscribe(this)` in `onResume` and `onPause` lifecycle callbacks.

### Converters
It is possible to add converters. By default, SantaRest works with `GsonConverter`. But you can create your own, just implement `Converter` interface.
```java
SantaRest santaRest = new SantaRest.Builder()
    .setConverter(new GsonConverter(gson))
    .build();
```

### Proguard
Like all libraries that generate dynamic code, Proguard might think some classes are unused and remove them. To prevent this, the following lines can be added to your proguard config file.

```java
-keep class **$$ActionHelperFactoryImpl { *; }
```

### Download
[ ![Download](https://api.bintray.com/packages/santagroup/maven/santarest/images/download.svg) ](https://bintray.com/santagroup/maven/santarest/_latestVersion)

```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.neenbedankt.gradle.plugins:android-apt:1.4'
    }
}
apply plugin: 'com.neenbedankt.android-apt'

dependencies {
    compile 'com.github.santagroup:santarest:0.0.1'
    apt 'com.github.santagroup:santarest-compiler:0.0.1'
}
```
