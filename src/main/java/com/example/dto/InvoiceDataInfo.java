package com.example.dto;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.persistence.invoice.InvoiceEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.example.controller.AdminController.DATE_FORMATTER;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceDataInfo {
  private Long id;
  private String messageId;
  private String filePath;
  private String fileName;
  private ZonedDateTime sentTime;
  private ZonedDateTime createdTime;
  private String invoiceDate;
  private String invoiceNumber;
  private String emailTitle;
  private String receiver;
  private String status;
  private String errorMessage;

  public static List<InvoiceDataInfo> fromInvoiceEntityData(List<InvoiceEntity> invoiceEntities){
    List<InvoiceDataInfo> invoices = new ArrayList<>();
    for (InvoiceEntity invoiceEntity : invoiceEntities) {
      InvoiceDataInfo invoice = InvoiceDataInfo.builder()
              .id(invoiceEntity.getId())
              .filePath(invoiceEntity.getFilePath())
              .fileName(invoiceEntity.getFileName())
              .emailTitle(invoiceEntity.getOutBoxEntity().getTitle())
              .createdTime(invoiceEntity.getCreatedTime())
              .sentTime(invoiceEntity.getOutBoxEntity().getSentTime())
              .receiver(invoiceEntity.getOutBoxEntity().getReceiver().getEmail())
              .status(invoiceEntity.getInvoiceStatus().name())
              .errorMessage(invoiceEntity.getErrorMessage())
              .messageId(invoiceEntity.getOutBoxEntity().getMessageId())
              .invoiceNumber(invoiceEntity.getInvoiceNo())
              .invoiceDate(DATE_FORMATTER.format(invoiceEntity.getInvoiceDate()))
              .build();

      invoices.add(invoice);
    }
    return invoices;
  }
}
