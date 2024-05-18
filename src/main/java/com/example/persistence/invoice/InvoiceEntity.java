package com.example.persistence.invoice;

import com.example.dto.Invoice;
import com.example.persistence.outbox.OutBoxEntity;
import com.example.persistence.receiver.ReceiverEntity;
import com.example.util.StringUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.io.File;
import java.sql.Date;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(
    indexes = {
        @Index(name = "receiver_index", columnList = "receiver")
    })

public class InvoiceEntity {

  private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,###");

  @Id
  @GeneratedValue(
      strategy = GenerationType.IDENTITY
  )
  private Long id;
  @Column(columnDefinition = "TEXT")
  private String attachmentId;
  private ZonedDateTime invoiceDate;
  private String invoiceNo;
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
  private String filePath;
  private String cc;

  @Column(columnDefinition = "TEXT")
  private String errorMessage;
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

  @ManyToOne(
      fetch = FetchType.LAZY
  )
  @JoinColumn(
      name = "outbox_id"
  )
  private OutBoxEntity outBoxEntity;

  private String receiver;
  private String originalReceiver;

  @Enumerated(EnumType.STRING)
  private InvoiceStatus invoiceStatus;

  @CreatedDate
  private ZonedDateTime createdTime;
  @LastModifiedDate
  private ZonedDateTime modifiedTime;

  public Integer getVatRate() {
    return subTotal == null ? null : (int) Math.round(vatAmount * 100.0 / subTotal);
  }

  public ZonedDateTime getInvoiceDate() {
    return invoiceDate == null ? null : invoiceDate.withZoneSameInstant(ZoneId.systemDefault());
  }

  public String getInvoiceDateStr() {
    if(invoiceDate == null){
      return null;
    }
    return Invoice.INVOICE_DATE_FORMAT.format(getInvoiceDate());
  }

  public ZonedDateTime getCreatedTime() {
    return createdTime.withZoneSameInstant(ZoneId.systemDefault());
  }

  public ZonedDateTime getModifiedTime() {
    return modifiedTime.withZoneSameInstant(ZoneId.systemDefault());
  }

  @JsonIgnore
  public String getTotalPaymentStr() {
    return DECIMAL_FORMAT.format(totalPayment);
  }

  @JsonIgnore
  public String getSubTotalStr() {
    return DECIMAL_FORMAT.format(subTotal);
  }

  @JsonIgnore
  public String getVatAmountStr() {
    return DECIMAL_FORMAT.format(vatAmount);
  }


  @JsonIgnore
  public String getFileName() {
    return filePath.substring(filePath.lastIndexOf(File.separator) + 1);
  }

  public void updateFromInvoice(Invoice invoice) {
    this.invoiceDate = invoice.getInvoiceZoneDateTime();
    this.invoiceNo = invoice.getInvoiceNo();
    this.seller = invoice.getSeller();
    this.sellerTaxCode = invoice.getSellerTaxCode();
    this.sellerAddress = invoice.getSellerAddress();
    this.buyer = invoice.getBuyer();
    this.buyerTaxCode = invoice.getBuyerTaxCode();
    this.buyerAddress = invoice.getBuyerAddress();
    this.subTotal = invoice.getSubTotal();
    this.vatRate = invoice.getVatRate();
    this.vatAmount = invoice.getVatAmount();
    this.totalPayment = invoice.getTotalPayment();
  }

  public static class InvoiceComparator implements java.util.Comparator<InvoiceEntity> {

    @Override
    public int compare(InvoiceEntity o1, InvoiceEntity o2) {
      int sellerComparison = o1.getSeller().compareTo(o2.getSeller());
      if (sellerComparison != 0) {
        return sellerComparison;
      }

      return o1.getInvoiceDate().compareTo(o2.getInvoiceDate());
    }
  }
}
