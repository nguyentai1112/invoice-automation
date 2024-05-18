package com.example.service;

import com.example.dto.Invoice;
import com.example.exception.InternalException;
import com.example.exception.PdfParsingException;
import com.example.persistence.invoice.InvoiceEntity;
import com.example.util.JsonUtil;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PdfServiceTest {


  private final PdfService pdfService = new PdfService();

  @Test
  void testReadPdf() throws PdfParsingException, IOException, InternalException {
    //  assertDoesNotThrow(() -> pdfService.readPdf(path));
    List<String> paths = List.of("tests/sample.pdf",
        "tests/HÙNG_XUÂN_PHÁT_-_VMB_175.pdf",
        "tests/HÙNG_XUÂN_PHÁT_08.12_-_VMB_225.pdf",
        "tests/MINH_QUYỀN_28.12_-_VMB_715.pdf", "tests/HÙNG_XUÂN_PHÁT_-_VMB_401.pdf",
        "tests/HÙNG_XUÂN_PHÁT_19.12__-_VMB_441.pdf",
        "tests/MINH_QUYỀN_29.12_-_VMB_733.pdf", "tests/HÙNG_XUÂN_PHÁT_-_VMB_567.pdf",
        "tests/HÙNG_XUÂN_PHÁT_27.12_-_VMB_661.pdf",
        "tests/MINH_QUYỀN_30.12_-_VMB_799.pdf", "tests/HÙNG_XUÂN_PHÁT_-_VMB_801.pdf",
        "tests/HÙNG_XUÂN_PHÁT_31.12_-_VMB_811.pdf",
        "tests/XD_HUNG_QUY_30.09_-_KM_231.pdf", "tests/HÙNG_XUÂN_PHÁT_-_VMB__282.pdf",
        "tests/MINH_QUYỀN_26.12_-_VMB_581.pdf", "tests/HOADON_0317769838_1C23TYY_280.pdf",
        "tests/template3.pdf", "tests/template2.pdf", "tests/template4.pdf",
        "tests/template5_amount_info_is_in_1_line.pdf",
        "tests/template8_unknown.pdf",
        "tests/template9_one_more_page.pdf",
        "tests/template10_unknown.pdf",
        "tests/template7_bkav_invoice.pdf",
        "tests/template11_date_pattern_does_not_include_năm.pdf",
        "tests/template11_unknown.pdf",
        "tests/template12_last_page_not_the_correct_page_to_parse.pdf",
        "tests/template13_payment_is_0.pdf",
        "tests/template15_no_amount.pdf"
    );
    List<Invoice> invoiceList = new ArrayList<>();
    for (String path : paths) {
      try{
      Invoice invoice = pdfService.readPdf(path);
        invoiceList.add(invoice);
        System.out.println(invoice);}
      catch (Exception e){
        System.out.println("error: " + path);
        throw e;
      }

    }

  }

  @Test
  void testReadPdf_test() throws PdfParsingException, IOException, InternalException {
    //  assertDoesNotThrow(() -> pdfService.readPdf(path));
    // List<String> paths = List.of("tests/template5_amount_info_is_in_1_line.pdf");
    List<String> paths = List.of(
        "tests/template21_2_lines_buyer_name.pdf"
    );

    for (String path : paths) {
      Invoice invoice = pdfService.readPdf(path);
      System.out.println(JsonUtil.toJson(invoice));
    }

  }
}