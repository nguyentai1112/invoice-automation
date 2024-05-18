package com.example.persistence.receiver;

import com.example.persistence.invoice.InvoiceEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiverRepository extends JpaRepository<ReceiverEntity, String> {

  List<ReceiverEntity> findAllByOrderByEmailAsc();

}
