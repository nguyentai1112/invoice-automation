package com.example.util;

import com.example.dto.Invoice;
import com.example.persistence.invoice.InvoiceEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class JsonUtil {

  public static ObjectMapper objectMapper = new ObjectMapper();
  static {
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  }

  public static String toJson(Object object) {
    try {
      return JsonUtil.objectMapper.writeValueAsString(object);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }
  public static List<Invoice> readInvoiceListFromFile(String filePath) throws IOException {
    File file = new File(filePath);
    List<Invoice> invoiceList = objectMapper.readValue(file, new TypeReference<List<Invoice>>() {
    });
    return invoiceList;
  }

  public static List<InvoiceEntity> readInvoiceEntityListFromFile(String filePath) throws IOException {
    File file = new File(filePath);
    List<InvoiceEntity> invoiceList = objectMapper.readValue(file, new TypeReference<List<InvoiceEntity>>() {
    });
    return invoiceList;
  }

  public static void writeInvoiceListToFile(List<InvoiceEntity> invoiceList, String filePath)
      throws IOException {
    File file = new File(filePath);
    objectMapper.writeValue(file, invoiceList);
  }
}
