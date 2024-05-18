package com.example.persistence.invoice;

import com.example.persistence.receiver.ReceiverEntity;
import java.time.ZonedDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceUpdateRepository extends JpaRepository<InvoiceUpdateEntity, Long> {


  List<InvoiceUpdateEntity> findByInvoiceEntityId(long invoiceId);

}
