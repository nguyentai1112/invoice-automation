package com.example.exception;

public class RecordNotFoundException extends BaseException{

  public RecordNotFoundException(String message) {
    super(message);
  }

  public RecordNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
