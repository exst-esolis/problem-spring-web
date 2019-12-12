package org.zalando.problem.spring.web.advice.network;

import net.jodah.failsafe.CircuitBreakerOpenException;
import org.apiguardian.api.API;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.spring.web.advice.AdviceTrait;

import java.time.Duration;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.INTERNAL;

@API(status = EXPERIMENTAL)
public interface CircuitBreakerOpenAdviceTrait extends AdviceTrait {

    @API(status = INTERNAL)
    @ExceptionHandler
    default ResponseEntity<Problem> handleCircuitBreakerOpen(
            final CircuitBreakerOpenException exception,
            final NativeWebRequest request) {

        final Duration delay = exception.getCircuitBreaker().getDelay();
        final HttpHeaders headers = retryAfter(delay.getSeconds());
        return create(Status.SERVICE_UNAVAILABLE, exception, request, headers);
    }

    default HttpHeaders retryAfter(final long delay) {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.RETRY_AFTER, String.valueOf(delay));
        return headers;
    }

}
