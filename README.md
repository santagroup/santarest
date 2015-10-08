# SantaRest

Flexible library for easily HTTP requests executing. It can be used for Android and Java.

### What's this for?

SantaRest makes profit in such ways:

1. Flexibility and easy using (Thanks [Retrofit](http://square.github.io/retrofit/))
2. Decoupling networking code from application and receiving responses in different places, delivered with EventBus (Thanks [Otto](http://square.github.io/otto/) and [EventBus](https://github.com/greenrobot/EventBus))

With the help of SantaRest you can create application with network communication but without callbacks and Android activity's life-cycle checking.

### Usage

To send and receive HTTP requests you should use SantaRest class
```java
santaRest = new SantaRest.Builder()
                 .setServerUrl("https://api.github.com")
                .build();
```

Each HTTP request in SantaRest is an individual class which contains all information about the request and response. Let's call it the Action.

You should annotate action class with `@RestAction`. 
```java
@RestAction(value = "/demo", method = RestAction.Method.GET)
public class ExampleAction {

}
```

To process request, you may annotate Action fields with:
* `@Path` for path value
* `@Query` for request URL's parameters
* `@Body` for POST requests's body
* `@RequestHeader` for request headers
* `@Field` for request's fields if request type is `RestAction.Type.FORM_URL_ENCODED`
* `@Part` for multipart request's parts

To process response, you may use special annotations:
* `@Response` for getting response body.
* `@Status` for getting response status. You can use `Integer`, `Long`, `int` or `long` fields for get status code or use `boolean` if you want to know request was sent successfully
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

To receive actions with responses, you should subscribe for event:
```java
santaRest.subscribe(this);
```
But don’t forget to unsubscribe from it:
```java
santaRest.unsubscribe(this);
```

For android, we recommend you to use `santaRest.subscribe()` at `onResume` and `santaRest.unsubscribe(this)` at `onPause` methods.

### Converters
It's possible to add converters. By default, santarest works with `GsonConverter`. But you can create your own, just implement `Converter` interface;
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
```java
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
    compile project(":core")//TODO: Replace with dependecy from maven
    apt project(':santarest-compiler')//TODO: Replace with dependecy from maven
}