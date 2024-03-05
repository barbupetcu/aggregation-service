package com.test.aggregationservice.api;

import com.test.aggregationservice.api.exception.ParametersNotValidException;
import com.test.aggregationservice.api.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Collections;
import java.util.Map;

@RestControllerAdvice
public class AggregationControllerAdvice {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleResourceNotFoundException(ResourceNotFoundException exception) {
        return Collections.singletonMap("message", exception.getMessage());
    }

    @ExceptionHandler(ParametersNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleParametersNotValidException(ParametersNotValidException exception) {
        return Collections.singletonMap("message", exception.getMessage());
    }
}
