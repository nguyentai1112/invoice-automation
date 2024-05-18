package com.example.dto;

import com.example.exception.InternalException;
import com.example.exception.PdfParsingException;
import com.example.util.JsonUtil;
import com.example.util.StringUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

@Data
@Slf4j
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(Include.NON_NULL)
public class Invoice {

  private String invoiceDate;
  private String invoiceNo;
  private String attachmentId;
  private String seller;
  private String sellerTaxCode;
  private String sellerAddress;
  private String buyer;
  private String buyerTaxCode;
  private String buyerAddress;
  private Long subTotal;
  private Integer vatRate;
  private Long vatAmount;
  private Long totalPayment;
  private String toEmail;
  private List<String> ccEmails;
  private String filePath;
  private String fileName;


  public static DateTimeFormatter INVOICE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");
  private static String INVOICE_NO_PATTERN = "Số \\(No\\.\\):\\s*(\\d+)";
  private static String SELLER_PATTERN = "Đơn vị bán hàng \\(Seller\\):\\s*(.+)";
  private static String SELLER_TAX_CODE_PATTERN = "Mã số thuế \\(Tax code\\):\\s*(.+)";
  private static String SELLER_ADDRESS_PATTERN = "Địa chỉ \\(Address\\):\\s*(.+)";
  private static String BUYER_PATTERN = "Tên đơn vị \\(Company name\\):\\s*(.+)";
  private static String BUYER_TAX_CODE_PATTERN = "Mã số thuế \\(Tax code\\):\\s*(.+)";
  private static String BUYER_ADDRESS_PATTERN = "Địa chỉ \\(Address\\):\\s*(.+)";
  private static String SUB_TOTAL_PATTERN = "Cộng tiền hàng \\((Sub total|Total amount)\\):\\s*(.+)";
  private static String VAT_AMOUNT_PATTERN = "Tiền thuế GTGT \\(VAT amount\\):\\s*(.+)";
  private static String TOTAL_PAYMENT_PATTERN = "Tổng cộng tiền thanh toán \\(Total payment\\):\\s*(.+)";
  private static String AMOUNT_INFO_PATTERN = "Tổng cộng \\(Total amount\\):\\s?([\\d\\.]+)\\s([\\d\\.]+)\\s([\\d\\.]+)";

  private static String VAT_RATE_PATTERN = "Thuế suất GTGT \\(VAT rate\\):\\s*(\\d+)%.*";
  private static String INVOICE_DATE_PATTERN = "(Ngày \\(Date\\)|Ngày \\(day\\))\\s*(\\d+)\\s+tháng \\(month\\)\\s*(\\d+)\\s*(năm)?\\s*\\(year\\)\\s*(\\d+).*";


  /*
  Example of buyerText:
    Họ tên người mua hàng (Buyer):
    Tên đơn vị (Company name): CÔNG TY CỔ PHẦN PHÚC LONG HERITAGE
    Mã số thuế (Tax code): 0316871719
    Địa chỉ (Address): Phòng 702, Tầng 7, Tòa nhà Central Plaza, số 17 Lê Duẩn, Phường Bến Nghé, Quận 1, Thành phố Hồ Chí
    Minh, Việt Nam
    Hình thức thanh toán (Payment method): Tiền mặt/Chuyển khoản Số tài khoản (A/C No.):
   */
  public void extractBuyerInfo(String buyerText) throws PdfParsingException {

    String[] rows = buyerText.split("\\r?\\n");
    for (int i = 0; i < rows.length; i++) {
      final String row = rows[i];
      if (row.contains("Tên đơn vị (Company") | row.contains(
          "Đơn vị (Co. name)") | row.contains(
          "Tên đơn vị(Co. name)")) { //it can be Company name or Company's name
        String[] buyerParts = row.split(":");
        String buyer = buyerParts[1].trim();
        if(i< rows.length -1){
          final String nextRow = rows[i + 1];
          if(!nextRow.contains(":")){
            buyer = buyer + StringUtil.SPACE + nextRow;
          }
        }
        final String nextRow = rows[i + 1];

        this.setBuyer(buyer);
        //  } else if (row.contains("Mã số thuế (Tax code)") | row.contains("MST (Tax Code)")) {
        //   String[] taxCodeParts = row.split(":");
        //  String taxCode = taxCodeParts[1].trim();
        // this.setBuyerTaxCode(taxCode);
      } else if (row.contains("Địa chỉ (Address)")) {
        String[] addressParts = row.split(":");
        String address = addressParts[1].trim();
        this.setBuyerAddress(address);
      }
    }
    validateBuyer();
  }

  private void validateBuyer() throws PdfParsingException {
    if (ObjectUtils.isEmpty(this.buyer)) {
      throw new PdfParsingException("Buyer info is invalid");
    }
  }

  /*
  Example of sellerText:
    HÓA ĐƠN GIÁ TRỊ GIA TĂNG Ký hiệu (Serial): 1C23TYY
    (VAT INVOICE) Số (No.): 319
    Ngày (Date) 14 tháng (month) 12 năm (year) 2023
    Đơn vị bán hàng (Seller): CÔNG TY TNHH THƯƠNG MẠI DỊCH VỤ VÂN MỸ BÌNH
    Mã số thuế (Tax code): 0 3 1 8 1 5 7 5 6 3
    Địa chỉ (Address): 252/62/6 Phạm Văn Chiêu, Phường 9, Quận Gò Vấp, TP Hồ Chí Minh, Việt Nam

   */
  //template 1: "Hoa don GTGT" is at the top of the page
  // Seller info is below the "Hoa don GTGT"
  public void extractSellerInfoFirstAttempt(String sellerText)
      throws PdfParsingException, InternalException {
    //  this.seller = StringUtil.extractFromPattern(sellerText, SELLER_PATTERN);
    //this.sellerAddress = StringUtil.extractFromPattern(sellerText, SELLER_ADDRESS_PATTERN);
    //this.sellerTaxCode = StringUtil.extractFromPattern(sellerText, SELLER_TAX_CODE_PATTERN);
    String[] rows = sellerText.split("\\r?\\n");
    for (String row : rows) {
      if (row.contains("Đơn vị bán hàng (Seller)") | row.contains("Đơn vị bán (Seller)")) {
        String[] sellerParts = row.split(":");
        String seller = sellerParts[1].trim();
        this.setSeller(seller);
      } else {
        extractSellerInfoFromRow(row);
      }
    }
    validateSellerInfo();
  }

  public void extractSellerInfo(String sellerText) throws PdfParsingException, InternalException {
    try {
      extractSellerInfoFirstAttempt(sellerText);
    } catch (PdfParsingException e) {
      try {
        log.warn("Seller info is extracted by template1 is invalid, from :{} object: {}",
            sellerText, JsonUtil.toJson(this));
        log.info("Trying to extract seller info by template 2...", sellerText);
        extractSellerInfoSecondAttempt(sellerText);
      } catch (PdfParsingException ex) {
        log.error("Seller info is extracted by template 2 from :{} is invalid, object: {}",
            sellerText, JsonUtil.toJson(this));
        throw ex;
      }

    }
  }


  //template 2: Seller info is on top of the page
  // "Hoa don GTGT" is below the seller info
  public void extractSellerInfoSecondAttempt(String sellerText)
      throws PdfParsingException, InternalException {
    String[] rows = sellerText.split("\\r?\\n");
    int rowNum = 1;
    for (String row : rows) {
      if (rowNum == 1) {
        this.seller = row;
      } else {
        extractSellerInfoFromRow(row);
      }
      rowNum++;
    }
    validateSellerInfo();
  }

  public void extractSellerInfoFromRow(String row) throws InternalException {
    if (row.contains("Ngày (Date)") || row.contains("Ngày (day)")) {
      final Matcher invoiceDateMatcher = StringUtil.extractFromPattern(row, INVOICE_DATE_PATTERN);
      String invoiceDate =
          StringUtil.paddingZero(invoiceDateMatcher.group(2)) + "-" + StringUtil.paddingZero(
              invoiceDateMatcher.group(3)) + "-"
              + invoiceDateMatcher.group(5);
      this.setInvoiceDate(invoiceDate);
    } else if (row.contains("Số (No.)") || row.contains(" Số (Invoice No.)")) {
      String[] numberParts = row.split(":");
      String number = numberParts[1].trim();
      this.setInvoiceNo(number);
    } else if (row.contains("Mã số thuế (Tax code)") | row.contains("MST (Tax Code)")) {
      String[] taxCodeParts = row.split(":");
      String taxCode = taxCodeParts[1].trim();
      this.setSellerTaxCode(taxCode);
    } else if (row.contains("Địa chỉ (Address)")) {
      String[] addressParts = row.split(":");
      String address = addressParts[1].trim();
      this.setSellerAddress(address);
    }
  }

  /*
  Example of amountText:
    Cộng tiền hàng (Sub total): 10.584.000
    Thuế suất GTGT (VAT rate):  8%   Tiền thuế GTGT (VAT amount): 846.720
    Tổng cộng tiền thanh toán (Total payment): 11.430.720
    Số tiền viết bằng chữ (Amount in words): Mười một triệu bốn trăm ba mươi nghìn bảy trăm hai mươi đồng.
   */

  public void extractAmount(String amountText) throws InternalException, PdfParsingException {
    try {
      extractAmountFirstAttempt(amountText);
    } catch (Exception e) {
      log.warn(
          "extractAmountFirstAttempt failed, try extractAmountSecondAttempt, the amountText: {}",
          amountText);
      extractAmountSecondAttempt(amountText);
    }

  }

  //the info is only 1 line
  //Tổng cộng (Total amount): 1.050.000.000 84.000.000 1.134.000.000
  private void extractAmountSecondAttempt(String amountText) throws PdfParsingException {
    String[] rows = amountText.split("\\r?\\n");
    try {
      for (String row : rows) {
        if (row.contains("Tổng cộng (Total amount)")) {
          Matcher matcher = StringUtil.extractFromPattern(row, AMOUNT_INFO_PATTERN);
          this.setSubTotal(StringUtil.stringNumToLong(matcher.group(1)));
          this.setVatAmount(StringUtil.stringNumToLong(matcher.group(2)));
          this.setTotalPayment(StringUtil.stringNumToLong(matcher.group(3)));
        }
      }
    } catch (Exception e) {
      log.error("Can not parse the Amount info from {}", amountText, e);
      throw new PdfParsingException("Can not parse Amount info", e);
    }
    validateAmountInfo();
  }

  public void extractAmountFirstAttempt(String amountText)
      throws PdfParsingException {
    String[] rows = amountText.split("\\r?\\n");
    try {
      for (String row : rows) {
        if (row.contains("Cộng tiền hàng (Sub total)") || row.contains(
            "Cộng tiền hàng (Total amount)")) {
          String[] subTotalParts = row.split(":");
          // String subTotal = subTotalParts[1].trim();
          String subTotalString = StringUtil.extractFromPattern(row, SUB_TOTAL_PATTERN, 2);
          this.setSubTotal(StringUtil.stringNumToLong(subTotalString));
        } else if (row.contains("Tiền thuế GTGT (VAT amount)") || row.contains(
            "Cộng tiền thuế GTGT (VAT amount)")) {
          // this.setVatRate(
          //   StringUtil.stringPercentToInt(StringUtil.extractFromPattern(row, VAT_RATE_PATTERN)));
          //String vatAmountString = StringUtil.extractFromPattern(row, VAT_AMOUNT_PATTERN);
          // this.setVatAmount(StringUtil.stringNumToLong(vatAmountString));
          String[] vatAmountParts = row.split(":");
          //the text can be like Thuế suất GTGT (VAT rate):  8%   Tiền thuế GTGT (VAT amount): 846.720
          //so we need to get the last part
          int lastPartIndex = vatAmountParts.length - 1;
          String vatAmountString = vatAmountParts[lastPartIndex].trim();
          this.setVatAmount(StringUtil.stringNumToLong(vatAmountString));

        } else if (row.contains("Tổng cộng tiền thanh toán (Total payment)")) {
          String[] totalPaymentParts = row.split(":");
          String totalPayment = totalPaymentParts[1].trim();
          this.setTotalPayment(StringUtil.stringNumToLong(totalPayment));
        }
      }
    } catch (Exception e) {
      log.error("amountText: {}", amountText, e);
      throw new PdfParsingException("Can not parse Amount info", e);
    }
    validateAmountInfo();
  }

  public void setSellerTaxCode(String sellerTaxCode) {
    this.sellerTaxCode = StringUtil.removeSpaces(sellerTaxCode);
  }

  public void checkValidation() throws PdfParsingException {
    validateSellerInfo();
    validateBuyer();
    validateAmountInfo();
  }


  public void validateSellerInfo() throws PdfParsingException {

    if (ObjectUtils.isEmpty(this.seller) || ObjectUtils.isEmpty(this.sellerTaxCode)
        || ObjectUtils.isEmpty(this.invoiceNo)
        || ObjectUtils.isEmpty(this.invoiceDate)) {
      throw new PdfParsingException("Seller info is invalid");
    }
    if(this.invoiceNo.contains("Chưa cấp số")){
      throw new PdfParsingException("Invoice number is not defined");
    }
  }

  public void validateAmountInfo() throws PdfParsingException {
    if (ObjectUtils.isEmpty(this.subTotal) || ObjectUtils.isEmpty(this.vatAmount)
        || ObjectUtils.isEmpty(this.totalPayment)) {
      throw new PdfParsingException("Amount info is invalid, one of the fields is empty");
    }

    if ((this.subTotal > this.totalPayment) || (this.vatAmount > 0
        && this.vatAmount >= this.subTotal)) {
      throw new PdfParsingException("Amount info is invalid, the amount info is not correct");
    }

  }

  @JsonIgnore
  public LocalDate getInvoiceLocalDate() {
    return LocalDate.parse(this.invoiceDate, INVOICE_DATE_FORMAT);
  }

  @JsonIgnore
  public ZonedDateTime getInvoiceZoneDateTime() {
    LocalDate localDate = getInvoiceLocalDate();

    return localDate.atStartOfDay().atZone(ZoneId.systemDefault());
  }

  /*
  public static class InvoiceComparator implements java.util.Comparator<Invoice> {

    @Override
    public int compare(Invoice o1, Invoice o2) {
      int sellerComparison = o1.getSeller().compareTo(o2.getSeller());
      if (sellerComparison != 0) {
        return sellerComparison;
      }

      LocalDate date1 = o1.getInvoiceLocalDate();
      LocalDate date2 = o2.getInvoiceLocalDate();
      return date1.compareTo(date2);
    }
  }

   */
}