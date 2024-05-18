package com.example.exception;

public class DuplicateInfoException extends BaseException{

  public DuplicateInfoException(String message) {
    super(message);
  }

  public DuplicateInfoException(String message, Throwable cause) {
    super(message, cause);
  }
}
