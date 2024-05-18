package com.example.exception;

public class EmailHandlingException extends BaseException{

  public EmailHandlingException(String message) {
    super(message);
  }

  public EmailHandlingException(String message, Throwable cause) {
    super(message, cause);
  }
}
