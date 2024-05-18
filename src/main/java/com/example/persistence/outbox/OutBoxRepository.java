package com.example.persistence.outbox;

import com.example.persistence.invoice.InvoiceEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutBoxRepository extends JpaRepository<OutBoxEntity, Long> {
  Optional<OutBoxEntity> findByMessageId(String messageId);


  Optional<OutBoxEntity> findFirstByOrderBySentTimeDesc();
}
