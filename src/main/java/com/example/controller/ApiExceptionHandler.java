package com.example.controller;

import com.example.exception.ConcurrentExecutionException;
import com.example.exception.PdfParsingException;
import com.example.exception.RecordNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;


  @ControllerAdvice
  @Slf4j
  public class ApiExceptionHandler {

    static final String INTERNAL_SERVER_ERROR_MESSAGE = "Internal Server Error";

    @ExceptionHandler({RecordNotFoundException.class, ConcurrentExecutionException.class, PdfParsingException.class})
    public ResponseEntity<String> handleKnownException(Throwable exception) {
      log.error(INTERNAL_SERVER_ERROR_MESSAGE, exception);
      return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({Throwable.class})
    public ResponseEntity<String> handleUnhandledError(Throwable exception) {
      log.error(INTERNAL_SERVER_ERROR_MESSAGE, exception);
      return new ResponseEntity<>(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);

    }


  }