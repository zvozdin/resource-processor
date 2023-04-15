package com.example.resourceprocessor.retry;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

@Slf4j
@RequiredArgsConstructor
@Component
public class RestTemplateWithTimeoutAndRetry {

    private final RestTemplate restTemplate;

    @Retryable(
            value = {HttpServerErrorException.class, SocketTimeoutException.class, ConnectException.class},
            maxAttemptsExpression = "${app.server.restTemplate.maxRetryAttempts:2}",
            backoff = @Backoff(delayExpression = "${app.server.restTemplate.retryDelayMilliSeconds:10}")
    )
    public <T1, T2> ResponseEntity<T1> exchange(RequestEntity<T2> request, Class<T1> responseType) {
        return restTemplate.exchange(request, responseType);
    }

}
