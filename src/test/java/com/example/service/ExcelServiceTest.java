package com.example.service;

import com.example.dto.Invoice;
import com.example.dto.Quarter;
import com.example.persistence.invoice.InvoiceEntity;
import com.example.util.JsonUtil;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.jupiter.api.Test;


class ExcelServiceTest {

  ExcelService excelService =  new ExcelService();;

  @Test
  void writeToExcel() throws IOException {
    List<InvoiceEntity> invoiceList = JsonUtil.readInvoiceEntityListFromFile("tests/invoices.json");


    Workbook workbook = excelService.processInvoices(invoiceList);
    //write this workbook in excel file.
    workbook.write(new java.io.FileOutputStream("tests/invoices.xlsx"));
  }
}