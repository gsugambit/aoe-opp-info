package com.theboys.aoe.opp.info.controller;

import com.theboys.aoe.opp.info.exception.ExceptionResponse;
import com.theboys.aoe.opp.info.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ErrorControllerAdvice {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNotFoundException(NotFoundException e) {

        ExceptionResponse response = ExceptionResponse.builder().message(e.getMessage()).exceptionClass(e.getClass().getName()).build();


        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(Exception e) {

        ExceptionResponse response = ExceptionResponse.builder().message(e.getMessage()).exceptionClass(e.getClass().getName()).build();


        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
