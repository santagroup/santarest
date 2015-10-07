# SantaRest

Durable library for easily HTTP requests executing with the help of EventBus for Android and Java.

### What's this for?

SantaRest makes profit in such ways:

1. Flexibility and easy using (Thanks [Retrofit](http://square.github.io/retrofit/))
2. Decoupling networking code from application and receiving responses in different places with EventBus (Thanks [Otto](http://square.github.io/otto/) and [EventBus](https://github.com/greenrobot/EventBus))
With the help of SantaRest you can create application with network communication but without callbacks and Android activity's life-cycle checking.

### Usage

To send and receive HTTP requests you should use SantaRest class
```java
santaRest = new SantaRest.Builder()
                .setServerUrl("https://api.github.com")
                .build();
```

Each HTTP request in SantaRest is an individual class which contains all information about the request and response. We call it as Action.


For creating an Action you should write a class with annotation `@RestAction`. Example:
```java

@RestAction(value = “/demo",
        method = RestAction.Method.GET)
public class ExampleAction {

}
```

In the Action class you can describe information about request using fields with annotations:

* `@Path` for path value
* `@Query` for request URL's parameters
* `@Body` for POST requests's body
* `@RequestHeader` for request headers
* `@Field` for request's fields if request type is RestAction.Type.FORM_URL_ENCODED
* `@Part` for multipart request's parts

To receive information of response you can use special annotations:

* `@Response` for getting response body.
* `@Status` for getting response status. You can use Integer or Long fields for get status code or use boolean if you want to know request is sent successfully
* `@ResponseHeader` for getting response headers

To send an action you should use method sendAction. After this request will be created, parsed and filled by response. To receive filled actions you should use:
```java
santaRest.subscribe(this);
```

But don’t forget to unsubscribe from it:
```java
santaRest.unsubscribe(this);
```

For android we recommend you to use `santaRest.subscribe()` at onResume and `santaRest.unsubscribe(this)` at onPause methods.
