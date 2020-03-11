# Problem: Spring WebFlux

[![Javadoc](http://javadoc.io/badge/org.zalando/problem-spring-webflux.svg)](http://www.javadoc.io/doc/org.zalando/problem-spring-webflux)
[![Maven Central](https://img.shields.io/maven-central/v/org.zalando/problem-spring-webflux.svg)](https://maven-badges.herokuapp.com/maven-central/org.zalando/problem-spring-webflux)

## Installation

Add the following dependency to your project:

```xml
<dependency>
    <groupId>org.zalando</groupId>
    <artifactId>problem-spring-webflux</artifactId>
    <version>${problem-spring-webflux.version}</version>
</dependency>
```

## Configuration

Make sure you register the required modules with your ObjectMapper:

```java
@Bean
public ProblemModule problemModule() {
    return new ProblemModule();
}

@Bean
public ConstraintViolationProblemModule constraintViolationProblemModule() {
    return new ConstraintViolationProblemModule();
}
```

The following table shows all built-in advice traits:

| Advice Trait                                                                                                                                                       | Produces                                                  |
|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------|
| [**`ProblemHandling`**](src/main/java/org/zalando/problem/spring/webflux/advice/ProblemHandling.java)                                                              |                                                           |
| `├──`[**`GeneralAdviceTrait`**](src/main/java/org/zalando/problem/spring/webflux/advice/general/GeneralAdviceTrait.java)                                           |                                                           |
| `│   ├──`[`ProblemAdviceTrait`](src/main/java/org/zalando/problem/spring/webflux/advice/general/ProblemAdviceTrait.java)                                           | *depends*                                                 |
| `│   ├──`[`ThrowableAdviceTrait`](src/main/java/org/zalando/problem/spring/webflux/advice/general/ThrowableAdviceTrait.java)                                       | [`500 Internal Server Error`](https://httpstatus.es/500)  |
| `│   └──`[ `UnsupportedOperationAdviceTrait`](src/main/java/org/zalando/problem/spring/webflux/advice/general/UnsupportedOperationAdviceTrait.java)                | [`501 Not Implemented`](https://httpstatus.es/501)        |
| `├──`[**`HttpAdviceTrait`**](src/main/java/org/zalando/problem/spring/webflux/advice/http/HttpAdviceTrait.java)                                                    |                                                           |
| `│   ├──`[`MethodNotAllowedAdviceTrait`](src/main/java/org/zalando/problem/spring/webflux/advice/http/MethodNotAllowedAdviceTrait.java)                            | [`405 Method Not Allowed`](https://httpstatus.es/405)     |
| `│   ├──`[`NotAcceptableAdviceTrait`](src/main/java/org/zalando/problem/spring/webflux/advice/http/NotAcceptableAdviceTrait.java)                                  | [`406 Not Acceptable`](https://httpstatus.es/406)         |
| `│   ├──`[`ResponseStatusAdviceTrait`](src/main/java/org/zalando/problem/spring/webflux/advice/http/ResponseStatusAdviceTrait.java)                                |                                                           |
| `│   └──`[`UnsupportedMediaTypeAdviceTrait`](src/main/java/org/zalando/problem/spring/webflux/advice/http/UnsupportedMediaTypeAdviceTrait.java)                    | [`415 Unsupported Media Type`](https://httpstatus.es/415) |
| `├──`[**`NetworkAdviceTrait`**](src/main/java/org/zalando/problem/spring/webflux/advice/network/NetworkAdviceTrait.java)                                           |                                                           |
| `│   └──`[`SocketTimeoutAdviceTrait`](src/main/java/org/zalando/problem/spring/webflux/advice/network/SocketTimeoutAdviceTrait.java)                               | [`504 Gateway Timeout`](https://httpstatus.es/504)        |
| `└──`[**`ValidationAdviceTrait`**](src/main/java/org/zalando/problem/spring/webflux/advice/validation/ValidationAdviceTrait.java)                                  |                                                           |
| `    └──`[`ConstraintViolationAdviceTrait`](src/main/java/org/zalando/problem/spring/webflux/advice/validation/ConstraintViolationAdviceTrait.java)                | [`400 Bad Request`](https://httpstatus.es/400)            |

You're free to use them either individually or in groups. Future versions of this library may add additional traits to groups. A typical usage would look like this:

```java
@ControllerAdvice
class ExceptionHandling implements ProblemHandling {

}
```

In WebFlux, if a request handler is not called, then the `ControllerAdvice` will not be used. So for
[`ResponseStatusAdviceTrait`](src/main/java/org/zalando/problem/spring/webflux/advice/http/ResponseStatusAdviceTrait.java) for a `404 Not found`, 
[`MethodNotAllowedAdviceTrait`](src/main/java/org/zalando/problem/spring/webflux/advice/http/MethodNotAllowedAdviceTrait.java), 
[`NotAcceptableAdviceTrait`](src/main/java/org/zalando/problem/spring/webflux/advice/http/NotAcceptableAdviceTrait.java), 
and [`UnsupportedMediaTypeAdviceTrait`](src/main/java/org/zalando/problem/spring/webflux/advice/http/UnsupportedMediaTypeAdviceTrait.java)
it is required to add a specific `WebExceptionHandler`:

```java
@Bean
@Order(-2) // The handler must have precedence over WebFluxResponseStatusExceptionHandler and Spring Boot's ErrorWebExceptionHandler
public WebExceptionHandler problemExceptionHandler(ObjectMapper mapper, ProblemHandling problemHandling) {
    return new ProblemExceptionHandler(mapper, problemHandling);
}
```

### Security

The Spring Security integration requires additional steps:

```java
@ControllerAdvice
class ExceptionHandling implements ProblemHandling, SecurityAdviceTrait {

}
```

```java
@Configuration
@Import(SecurityProblemSupport.class)
public class SecurityConfiguration {

    @Autowired
    private SecurityProblemSupport problemSupport;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(final ServerHttpSecurity http) {
        return http.exceptionHandling()
                .authenticationEntryPoint(problemSupport)
                .accessDeniedHandler(problemSupport)
                .and().build();
    }

}
```

`SecurityProblemSupport` will need a [`SecurityAdviceTrait`](src/main/java/org/zalando/problem/spring/webflux/advice/security/SecurityAdviceTrait.java) bean at startup. For instance:

```java
@ControllerAdvice
public class SecurityExceptionHandler implements SecurityAdviceTrait {
}
```

### Failsafe

The optional failsafe integration adds support for `CircuitBreakerOpenException` in the form of an advice trait:

```java
@ControllerAdvice
class ExceptionHandling implements ProblemHandling, CircuitBreakerOpenAdviceTrait {

}
```

An open circuit breaker will be translated into a `503 Service Unavailable`:

```http
HTTP/1.1 503 Service Unavailable
Content-Type: application/problem+json

{
  "title": "Service Unavailable",
  "status": 503
}
```

### Swagger/OpenAPI Request Validator

The optional integration for [Atlassian's Swagger Request Validator](https://bitbucket.org/atlassian/swagger-request-validator)
adds support for invalid request/response exceptions as a dedicated advice trait:

```java
@ControllerAdvice
class ExceptionHandling implements ProblemHandling, OpenApiValidationAdviceTrait {

}
```
