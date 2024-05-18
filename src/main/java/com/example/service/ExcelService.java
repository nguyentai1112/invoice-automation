package com.example.service;

import com.example.dto.Invoice;
import com.example.dto.Quarter;
import com.example.persistence.invoice.InvoiceEntity;
import com.example.persistence.invoice.InvoiceEntity.InvoiceComparator;
import io.micrometer.common.util.StringUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import org.apache.commons.collections.map.HashedMap;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ExcelService {

  // private static final String[] COLUMN_NAMES = {"Invoice Date", "Invoice No", "Seller", "Buyer",
  // "Sub Total", "Total Payment", "VAT", "File Name"};
  private static final String[] COLUMN_NAMES = {"Ngày Xuất", "Số Hoá Đơn", "Công Ty Xuất",
      "Công Ty Lấy",
      "Chưa VAT", "Có VAT", "VAT", "File Path", "CC"};
  private final String SUM_NAME = "TC";

  public Workbook processInvoices(List<InvoiceEntity> invoices) {
    Workbook workbook = new XSSFWorkbook();

    Map<String, List<InvoiceEntity>> invoicesByQuarter = groupByQuarter(invoices);
    for (Map.Entry<String, List<InvoiceEntity>> entry : invoicesByQuarter.entrySet()) {
      createAndAddDataToSheet(workbook, entry.getKey(), entry.getValue());
    }
    return workbook;
    // Write the workbook to a file

  }


  private void createAndAddDataToSheet(Workbook workbook, String sheetName,
      List<InvoiceEntity> invoices) {
    Sheet sheet = workbook.createSheet(sheetName);

    // Create a CellStyle with a predefined number format
    CellStyle dateCellStyle = workbook.createCellStyle();
    dateCellStyle.setDataFormat(
        workbook.getCreationHelper().createDataFormat().getFormat("dd/MM/yyyy"));

    final CellStyle numberCellStyle = workbook.createCellStyle();
    numberCellStyle.setDataFormat(
        workbook.getCreationHelper().createDataFormat().getFormat("#,##"));

    // Create header row
    Row headerRow = sheet.createRow(0);
    for (int i = 0; i < COLUMN_NAMES.length; i++) {
      Cell cell = headerRow.createCell(i);
      cell.setCellValue(COLUMN_NAMES[i]);
    }

    // Populate data rows
    int rowNum = 1;
    for (InvoiceEntity invoice : invoices) {
      Row row = sheet.createRow(rowNum++);

      Cell invoiceDateCell = row.createCell(0);
      invoiceDateCell.setCellValue(LocalDateTime.from(invoice.getInvoiceDate()));
      invoiceDateCell.setCellStyle(dateCellStyle);

      row.createCell(1).setCellValue(invoice.getInvoiceNo());
      row.createCell(2).setCellValue(invoice.getSeller());
      row.createCell(3).setCellValue(invoice.getBuyer());

      Cell subTotalCell = row.createCell(4);
      subTotalCell.setCellValue(invoice.getSubTotal());
      subTotalCell.setCellStyle(numberCellStyle);
      Cell totalPaymentCell = row.createCell(5);
      totalPaymentCell.setCellValue(invoice.getTotalPayment());
      totalPaymentCell.setCellStyle(numberCellStyle);
      Cell vatAmountCell = row.createCell(6);
      vatAmountCell.setCellValue(invoice.getVatAmount());
      vatAmountCell.setCellStyle(numberCellStyle);

      row.createCell(7).setCellValue(invoice.getFilePath());

      if (StringUtils.isNotEmpty(invoice.getCc())) {
        final String[] mails = invoice.getCc().split(",");
        for (int i = 0; i < mails.length && i < 5; i++) {
          row.createCell(8 + i).setCellValue(mails[i]);
        }

      }
      //   row.createCell(7).setCellValue(invoice.getFilePath());
    }
    //sum up total amount
    Row row = sheet.createRow(rowNum);
    row.createCell(3).setCellValue(SUM_NAME);
    //add formula to calculate all all subTotal and totalPayment
    Cell subTotalCell = row.createCell(4);
    subTotalCell.setCellFormula("SUM(E2:E" + rowNum + ")");
    subTotalCell.setCellStyle(numberCellStyle);

    Cell totalPaymentCell = row.createCell(5);
    totalPaymentCell.setCellFormula("SUM(F2:F" + rowNum + ")");
    totalPaymentCell.setCellStyle(numberCellStyle);

    Cell vatAmountCell = row.createCell(6);
    vatAmountCell.setCellFormula("SUM(G2:G" + rowNum + ")");
    vatAmountCell.setCellStyle(numberCellStyle);


  }

  private Map<String, List<InvoiceEntity>> groupByQuarter(List<InvoiceEntity> invoices) {
   // Collections.sort(invoices, new InvoiceComparator());

    Map<String, List<InvoiceEntity>> result = new HashedMap();
    for (InvoiceEntity invoice : invoices) {
      String quarterString = Quarter.getQuarterName(invoice.getInvoiceDate());
      if (result.containsKey(quarterString)) {
        result.get(quarterString).add(invoice);
      } else {
        List<InvoiceEntity> invoiceList = new ArrayList<>();
        invoiceList.add(invoice);
        result.put(quarterString, invoiceList);
      }
    }
    return result;
  }

}