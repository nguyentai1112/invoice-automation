package com.example.exception;

public class ExcelHandlingException extends BaseException{

  public ExcelHandlingException(String message) {
    super(message);
  }

  public ExcelHandlingException(String message, Throwable cause) {
    super(message, cause);
  }
}
