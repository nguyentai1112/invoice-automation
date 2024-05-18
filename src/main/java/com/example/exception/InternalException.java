package com.example.exception;

public class InternalException extends BaseException{

  public InternalException(String message) {
    super(message);
  }

  public InternalException(String message, Throwable cause) {
    super(message, cause);
  }
}
