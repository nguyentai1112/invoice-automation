package com.example.persistence.invoice;

import com.example.persistence.receiver.ReceiverEntity;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InvoiceRepository extends JpaRepository<InvoiceEntity, Long> {

  List<InvoiceEntity> findByInvoiceDateBetweenAndReceiverAndInvoiceStatusOrderByInvoiceDateDescInvoiceNoDesc(ZonedDateTime fromTime,
      ZonedDateTime toTime, String email, InvoiceStatus status);
  List<InvoiceEntity> findByInvoiceDateBetweenAndReceiverAndInvoiceStatusOrderBySellerAscInvoiceDateDescInvoiceNoDesc(ZonedDateTime fromTime,
      ZonedDateTime toTime, String email, InvoiceStatus status);
  List<InvoiceEntity> findByInvoiceDateBetweenAndReceiverOrderByInvoiceDateDescInvoiceNoDesc(ZonedDateTime fromTime,
      ZonedDateTime toTime, String email);

  List<InvoiceEntity> findByInvoiceStatusNotOrderByIdDesc(InvoiceStatus status);
  Optional<InvoiceEntity> findByInvoiceDateAndInvoiceNoAndSellerTaxCode(ZonedDateTime invoiceDate, String invoiceNo, String sellerTaxCode);

    List<InvoiceEntity> findByOutBoxEntitySentTimeBetween(ZonedDateTime date, ZonedDateTime endOfDay);
  List<InvoiceEntity> findByInvoiceDateBetweenOrderByIdDesc(ZonedDateTime date, ZonedDateTime endOfDay);

}
