package com.example.exception;

public class ConcurrentExecutionException extends InternalException{

  public ConcurrentExecutionException(String message) {
    super(message);
  }

  public ConcurrentExecutionException(String message, Throwable cause) {
    super(message, cause);
  }
}
