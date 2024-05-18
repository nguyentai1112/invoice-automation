package com.example.exception;

public class PdfParsingException extends BaseException{

  public PdfParsingException(String message) {
    super(message);
  }

  public PdfParsingException(String message, Throwable cause) {
    super(message, cause);
  }
}
