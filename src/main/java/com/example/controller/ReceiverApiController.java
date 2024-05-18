package com.example.controller;

import com.example.persistence.invoice.InvoiceEntity;
import com.example.persistence.invoice.InvoiceService;
import com.example.persistence.invoice.InvoiceStatus;
import com.example.persistence.receiver.ReceiverEntity;
import com.example.persistence.receiver.ReceiverService;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@Slf4j
public class ReceiverApiController {

  private final ReceiverService receiverService;

  @GetMapping("/receivers/list")
  public List<ReceiverEntity> getInvoices() {
    return receiverService.getAllReceivers();
  }

}


