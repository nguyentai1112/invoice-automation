package com.example.service;

import static com.example.util.StringUtil.SLASH;

import com.example.dto.Attachment;
import com.example.dto.Invoice;
import com.example.exception.InternalException;
import com.example.exception.PdfParsingException;
import com.example.util.StringUtil;
import java.awt.Rectangle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;

@Service
@Slf4j
public class PdfService {

  private static final String REGION_SELLER = "seller";
  private static final String REGION_BUYER = "buyer";
  private static final String REGION_AMOUNT = "amount";
  private static final String ATTACHMENT_FOLDER = "attachments";


  public Invoice readPdf(PDDocument document)
      throws IOException, PdfParsingException, InternalException {
    Invoice invoice = new Invoice();
    String sellerText = null;
    String buyerText = null;
    String amountText = null;
    try {
      int lastPageIndex = document.getNumberOfPages() - 1;

      PDFTextStripperByArea stripper = getPdfTextStripperByArea();
      //check if the last page is empty
      //by extract the text of the last page
      //if the text is less than 200 characters, then the last page is empty
      //test/template4.pdf has this issue
      PDFTextStripper lastPageTextStripper = new PDFTextStripper();

      lastPageTextStripper.setStartPage(document.getNumberOfPages());
      lastPageTextStripper.setEndPage(document.getNumberOfPages());
      String lastPageText = lastPageTextStripper.getText(document);


      if(!isCorrectPageToParse(lastPageText)) {
        log.warn(
            "The last page does not contain tax code, it seems empty or not complete page or tax code is missing, please recheck it, the extracted text is: {}",
            lastPageText);
        lastPageIndex = lastPageIndex - 1;
      }
      // Extract text from the last page

      stripper.extractRegions(document.getPage(lastPageIndex));

      sellerText = stripper.getTextForRegion(REGION_SELLER);
      // System.out.println(sellerText);
      buyerText = stripper.getTextForRegion(REGION_BUYER);
      // System.out.println(buyerText);
      amountText = stripper.getTextForRegion(REGION_AMOUNT);
      //System.out.println(amountText);

      invoice.extractSellerInfo(sellerText);
      invoice.extractBuyerInfo(buyerText);
      invoice.extractAmount(amountText);

      invoice.checkValidation();
    } catch (Exception e) {
      log.error("sellerText: {}, buyerText: {}, amountText: {}",
          sellerText, buyerText, amountText, e);
      throw e;
    }
    return invoice;
  }

  public Invoice readPdf(String path) throws IOException, PdfParsingException, InternalException {
    File file = new File(path);

    try (PDDocument document = Loader.loadPDF(file)) {
      return readPdf(document);
    }
  }

  public Invoice readPdf(byte[] data) throws IOException, PdfParsingException, InternalException {
    PDDocument document = null;
    try {
      document = Loader.loadPDF(data);

      return readPdf(document);
    } finally {
      if (document != null) {
        document.close();
      }

    }

  }

  private static PDFTextStripperByArea getPdfTextStripperByArea() throws IOException {
    PDFTextStripperByArea stripper = new PDFTextStripperByArea();
    stripper.setSortByPosition(true);

    Rectangle sellerRect = new Rectangle(0, 0, 800, 200); // coordinates (adjust as needed)
    stripper.addRegion(REGION_SELLER, sellerRect);
    Rectangle buyerRect = new Rectangle(0, 200, 800, 110); // coordinates (adjust as needed)
    stripper.addRegion(REGION_BUYER, buyerRect);
    Rectangle amountRect = new Rectangle(0, 350, 800, 500); // coordinates (adjust as needed)
    stripper.addRegion(REGION_AMOUNT, amountRect);
    return stripper;
  }


  public String backupPdfAttachment(String messageId, Attachment attachment, ZonedDateTime time)
      throws IOException {
    String filePath =
        ATTACHMENT_FOLDER + SLASH + StringUtil.generateFolderFromDate(time)
            + SLASH + StringUtil.replaceSpacesWithUnderScores(attachment.getFileName());

    Path path = Paths.get(filePath);
    //create folder if not exist
    Files.createDirectories(path.getParent());
    Files.write(path, attachment.getData());
    log.info("MessageId: {}, Saved attachment to {} ", messageId, filePath);
    return path.toString();
  }

  private boolean isCorrectPageToParse(String pageText){
    final boolean hasSellerInfo = pageText.contains("Mã số thuế (Tax code)") |pageText.contains( "MST (Tax Code)") ;
    final boolean hasBuyerInfo = pageText.contains("Tên đơn vị (Company") | pageText.contains(
        "Đơn vị (Co. name)") | pageText.contains(
        "Tên đơn vị(Co. name)");
    final boolean hasAmountInfo = pageText.contains("Tổng cộng tiền thanh toán (Total payment)") | pageText.contains("Tổng cộng (Total amount)");
    return hasSellerInfo && hasBuyerInfo && hasAmountInfo;
  }
  /*
  public static void main(String[] args) throws IOException, PdfParsingException {
    String path = "attachments/sample.pdf";
    PdfService pdfService = new PdfService();
    Invoice invoice = pdfService.readPdf(path);
    System.out.println(StringUtil.toJson(invoice));

    //  System.out.println(text);
  }

   */
}