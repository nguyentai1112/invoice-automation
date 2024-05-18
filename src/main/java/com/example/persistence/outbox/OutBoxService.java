package com.example.persistence.outbox;

import com.example.dto.EmailOutBox;
import com.example.dto.Invoice;
import com.example.persistence.invoice.InvoiceEntity;
import com.example.persistence.invoice.InvoiceService;
import com.example.persistence.invoice.InvoiceStatus;
import com.example.persistence.receiver.ReceiverEntity;
import com.example.persistence.receiver.ReceiverService;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class OutBoxService {

  private final OutBoxRepository outBoxRepository;
  private final ReceiverService receiverService;
  private final InvoiceService invoiceService;

  @Transactional
  public void insertOutBox(OutBoxEntity outBoxEntity) {
    outBoxRepository.save(outBoxEntity);
  }

  public ZonedDateTime getLatestSentTime() {
    ZonedDateTime defaultZoneDateTime = ZonedDateTime.now().minusYears(1).truncatedTo(
        ChronoUnit.DAYS).withDayOfYear(1);
    return outBoxRepository.findFirstByOrderBySentTimeDesc()
        .map(OutBoxEntity::getSentTime)
        .orElse(defaultZoneDateTime);
  }

  @Transactional
  public void insertOutBoxInfo(EmailOutBox message, List<Invoice> invoices) {
    OutBoxEntity outBoxEntity = insertOutBoxInfo(message);

    for (Invoice invoice : invoices) {
      invoiceService.insertInvoice(InvoiceEntity.builder()
          .attachmentId(invoice.getAttachmentId())
          .invoiceDate(invoice.getInvoiceZoneDateTime())
          .invoiceNo(invoice.getInvoiceNo())
          .seller(invoice.getSeller())
          .sellerTaxCode(invoice.getSellerTaxCode())
          .sellerAddress(invoice.getSellerAddress())
          .buyer(invoice.getBuyer())
          .buyerTaxCode(invoice.getBuyerTaxCode())
          .buyerAddress(invoice.getBuyerAddress())
          .subTotal(invoice.getSubTotal())
          .vatRate(invoice.getVatRate())
          .vatAmount(invoice.getVatAmount())
          .totalPayment(invoice.getTotalPayment())
          .filePath(invoice.getFilePath())
          .outBoxEntity(outBoxEntity)
          .receiver(outBoxEntity.getReceiver().getEmail())
          .invoiceStatus(InvoiceStatus.SUCCESS)
          .createdTime(ZonedDateTime.now())
          .modifiedTime(ZonedDateTime.now())
          .build());
    }

  }

  @Transactional
  public OutBoxEntity insertOutBoxInfo(EmailOutBox message) {
    ReceiverEntity receiver = receiverService.createIfNotExisting(ReceiverEntity.builder()
        .name(message.getReceiver().getName())
        .email(message.getReceiver().getEmail().toLowerCase())
        .modifiedTime(ZonedDateTime.now())
        .createdTime(ZonedDateTime.now())
        .build());

    return outBoxRepository.save(OutBoxEntity.builder()
        .messageId(message.getMessageId())
        .title(message.getTitle())
        .sentTime(message.getSentTime())
        .body(message.getBody())
        .receiver(receiver)
        .originalRawReceiver(message.getOriginalRawReceiver())
        .cc(message.getCc())
        .internalDate(message.getInternalDate())
        .threadId(message.getThreadId())
        .modifiedTime(ZonedDateTime.now())
        .createdTime(ZonedDateTime.now())
        .build());
  }

  public boolean checkIfExisting(String messageId) {
    return outBoxRepository.findByMessageId(messageId).isPresent();
  }

  @Transactional
  public void update(OutBoxEntity outBoxEntity) {
    outBoxRepository.save(outBoxEntity);
  }
}
