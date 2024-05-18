package com.example.persistence.invoice;

import com.example.dto.Invoice;
import com.example.exception.RecordNotFoundException;
import com.example.persistence.outbox.OutBoxEntity;
import com.example.persistence.receiver.ReceiverEntity;
import com.example.persistence.receiver.ReceiverService;
import com.example.util.JsonUtil;
import jakarta.persistence.EntityManager;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class InvoiceService {

  private final InvoiceRepository invoiceRepository;
  private final InvoiceUpdateRepository invoiceUpdateRepository;
  private final ReceiverService receiverService;
  // @PersistenceContext
  private final EntityManager entityManager;

  @Transactional
  public void insertInvoice(InvoiceEntity invoiceEntity) {
    invoiceRepository.save(invoiceEntity);
  }

  @Transactional
  public void insertInvoice(OutBoxEntity outBox, Invoice invoice) {
    invoiceRepository.save(InvoiceEntity.builder()
        .outBoxEntity(outBox)
        .receiver(outBox.getReceiver().getEmail().toLowerCase())
        .originalReceiver(outBox.getOriginalRawReceiver())
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
        .cc(outBox.getCc())
        .filePath(invoice.getFilePath())
        .invoiceStatus(InvoiceStatus.SUCCESS)
        .createdTime(ZonedDateTime.now())
        .modifiedTime(ZonedDateTime.now())
        .build());
      log.info("MessageId: {}, invoice with invoice no {}, invoice date {}, seller tax code {} has been inserted",
              outBox.getMessageId(), invoice.getInvoiceNo(), invoice.getInvoiceZoneDateTime(), invoice.getSellerTaxCode());


  }

  public List<InvoiceEntity> getAllInvoices() {
    return invoiceRepository.findAll();
  }

  public Optional<InvoiceEntity> findById(Long id) {
    return invoiceRepository.findById(id);
  }

  @Transactional
  public void updateForRetry(InvoiceEntity invoice) {
    invoice.setModifiedTime(ZonedDateTime.now());
    invoiceRepository.save(invoice);
    invoiceUpdateRepository.save(InvoiceUpdateEntity.builder()
        .invoiceEntity(invoice)
        .changeContent("Retry to parse invoice")
        .updateType(InvoiceUpdateType.MANUAL)
        .createdTime(ZonedDateTime.now())
        .modifiedTime(ZonedDateTime.now())
        .build());

    log.info("InvoiceId: " + invoice.getId() + " has been retried parsing");
  }

  @Transactional
  public void updateReceiver(InvoiceEntity invoice, String email, InvoiceUpdateType type)
      throws RecordNotFoundException {
    Optional<ReceiverEntity> find = receiverService.getReceiver(email.toLowerCase());
    // For demonstration purposes, we'll log the information
    String oldEmail = invoice.getReceiver();

    invoice.setReceiver(find.get().getEmail().toLowerCase());
    invoice.setModifiedTime(ZonedDateTime.now());
    invoiceRepository.save(invoice);

    invoiceUpdateRepository.save(InvoiceUpdateEntity.builder()
        .invoiceEntity(invoice)
        .changeContent(
            "Change receiver from " + oldEmail + " to " + invoice.getReceiver())
        .updateType(type)
        .createdTime(ZonedDateTime.now())
        .modifiedTime(ZonedDateTime.now())
        .build());

    log.info("InvoiceId: " + invoice.getId() + " has been updated email from {} to new email: {} ",
        oldEmail,
        email);

  }

  public List<InvoiceEntity> searchInvoices(ZonedDateTime from, ZonedDateTime to,
      String receiver, InvoiceStatus status) {
    return invoiceRepository.findByInvoiceDateBetweenAndReceiverAndInvoiceStatusOrderByInvoiceDateDescInvoiceNoDesc(
        from, to, receiver, status);
  }

  public List<InvoiceEntity> searchInvoicesForExcelExport(ZonedDateTime from, ZonedDateTime to,
      String receiver, InvoiceStatus status) {
    return invoiceRepository.findByInvoiceDateBetweenAndReceiverAndInvoiceStatusOrderBySellerAscInvoiceDateDescInvoiceNoDesc(
        from, to, receiver, status);
  }

  public List<InvoiceEntity> searchInvoices(ZonedDateTime from, ZonedDateTime to,
      String receiver) {
    return invoiceRepository.findByInvoiceDateBetweenAndReceiverOrderByInvoiceDateDescInvoiceNoDesc(
        from, to, receiver);
  }

  public List<InvoiceEntity> searchUnsuccessfulInvoices() {
    return invoiceRepository.findByInvoiceStatusNotOrderByIdDesc(InvoiceStatus.SUCCESS);
  }

  public List<InvoiceEntity> searchInvoicesBySentDate(ZonedDateTime date) {
    final ZonedDateTime endOfDay = date.plusDays(1).minusNanos(1);
    return invoiceRepository.findByOutBoxEntitySentTimeBetween(date, endOfDay) ;
  }

  public List<InvoiceEntity> searchInvoicesByInvoiceDate(ZonedDateTime date) {
    final ZonedDateTime endOfDay = date.plusDays(1).minusNanos(1);
    return invoiceRepository.findByInvoiceDateBetweenOrderByIdDesc(date, endOfDay) ;
  }


  @Transactional
  public void deleteById(Long id) throws RecordNotFoundException {
    Optional<InvoiceEntity> invoiceFind = invoiceRepository.findById(id);
    if (invoiceFind.isEmpty()) {
      log.error("Invoice with id:{} not found", id);
      throw new RecordNotFoundException("Invoice with id " + id + " not found");
    }
    InvoiceEntity invoiceEntity = invoiceFind.get();
    invoiceEntity.setInvoiceStatus(InvoiceStatus.DELETED);
    invoiceEntity.setModifiedTime(ZonedDateTime.now());
    invoiceRepository.save(invoiceEntity);

    invoiceUpdateRepository.save(InvoiceUpdateEntity.builder()
        .invoiceEntity(invoiceEntity)
        .changeContent("Invoice  " + id + " has been deleted")
        .updateType(InvoiceUpdateType.MANUAL)
        .createdTime(ZonedDateTime.now())
        .modifiedTime(ZonedDateTime.now())
        .build());

    log.info("InvoiceId: {} has been deleted", id);

  }

  public List<InvoiceUpdateEntity> getInvoiceChanges(Long invoiceId) {
    return invoiceUpdateRepository.findByInvoiceEntityId(invoiceId);
  }

  public Optional<InvoiceEntity> findExistingInvoice(Invoice invoice) {
    return invoiceRepository
        .findByInvoiceDateAndInvoiceNoAndSellerTaxCode(invoice.getInvoiceZoneDateTime(),
            invoice.getInvoiceNo(), invoice.getSellerTaxCode());

  }

  @Transactional
  public void insertFailedInvoice(OutBoxEntity outBoxEntity, String attachementId, String savedPath,
      String errorMessage) {
    invoiceRepository.save(InvoiceEntity.builder()
        .outBoxEntity(outBoxEntity)
        .receiver(outBoxEntity.getReceiver().getEmail().toLowerCase())
        .cc(outBoxEntity.getCc())
        .attachmentId(attachementId)
        .filePath(savedPath)
        .invoiceStatus(InvoiceStatus.PARSE_FAILED)
        .errorMessage(errorMessage)
        .createdTime(ZonedDateTime.now())
        .modifiedTime(ZonedDateTime.now())
        .build());
  }

  public void updateInvoiceInfo(Long id, Invoice updatedInvoice) throws RecordNotFoundException {
    Optional<InvoiceEntity> found = invoiceRepository.findById(id);
    if (found.isEmpty()) {
      log.error("Invoice with id:{} not found", id);
      throw new RecordNotFoundException("Invoice with id " + id + " not found");
    }
    InvoiceEntity invoice = found.get();
    invoice.setInvoiceDate(updatedInvoice.getInvoiceZoneDateTime());
    invoice.setInvoiceNo(updatedInvoice.getInvoiceNo());
    invoice.setSeller(updatedInvoice.getSeller());
    invoice.setSellerTaxCode(updatedInvoice.getSellerTaxCode());
    invoice.setSellerAddress(updatedInvoice.getSellerAddress());
    invoice.setBuyer(updatedInvoice.getBuyer());
    invoice.setBuyerTaxCode(updatedInvoice.getBuyerTaxCode());
    invoice.setBuyerAddress(updatedInvoice.getBuyerAddress());
    invoice.setSubTotal(updatedInvoice.getSubTotal());
    invoice.setVatRate(updatedInvoice.getVatRate());
    invoice.setVatAmount(updatedInvoice.getVatAmount());
    invoice.setTotalPayment(updatedInvoice.getTotalPayment());
    invoice.setInvoiceStatus(InvoiceStatus.SUCCESS);
    invoice.setErrorMessage(null);
    invoice.setModifiedTime(ZonedDateTime.now());
    invoiceRepository.save(invoice);

    invoiceUpdateRepository.save(InvoiceUpdateEntity.builder()
        .invoiceEntity(invoice)
        .changeContent(
            "Update Invoice Info")
        .updateType(InvoiceUpdateType.MANUAL)
        .createdTime(ZonedDateTime.now())
        .modifiedTime(ZonedDateTime.now())
        .build());

    log.info("InvoiceId: {} has been updated to new info {} ", id, JsonUtil.toJson(invoice));
  }

  @Transactional
  public void updateStatus(InvoiceEntity invoiceEntity, InvoiceStatus status,
      InvoiceUpdateType updateType) {
    final String oldStatus = invoiceEntity.getInvoiceStatus().name();
    invoiceEntity.setInvoiceStatus(status);
    invoiceRepository.save(invoiceEntity);
    invoiceUpdateRepository.save(InvoiceUpdateEntity.builder()
        .invoiceEntity(invoiceEntity)
        .changeContent(
            "Change invoice status from " + oldStatus + " to " + status.name())
        .updateType(updateType)
        .createdTime(ZonedDateTime.now())
        .modifiedTime(ZonedDateTime.now())
        .build());

    log.info("InvoiceId: " + invoiceEntity.getId()
            + " has been updated status from {} to new status: {} ", oldStatus,
        status.name());
  }

}