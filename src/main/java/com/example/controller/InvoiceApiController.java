package com.example.controller;

import com.example.dto.Email;
import com.example.dto.Invoice;
import com.example.dto.Quarter;
import com.example.exception.RecordNotFoundException;
import com.example.persistence.invoice.InvoiceEntity;
import com.example.persistence.invoice.InvoiceService;
import com.example.persistence.invoice.InvoiceStatus;
import com.example.persistence.invoice.InvoiceUpdateEntity;
import com.example.persistence.invoice.InvoiceUpdateType;
import com.example.persistence.receiver.ReceiverEntity;
import com.example.persistence.receiver.ReceiverService;
import com.example.service.ProcessorService;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

public class InvoiceApiController {

  private final InvoiceService invoiceService;
  private final ReceiverService receiverService;
  private final ProcessorService processorService;

  @GetMapping("/invoices/list")
  public List<InvoiceEntity> getInvoices(@RequestParam("quarter") String quarterString,
      @RequestParam("email") String email,
      @RequestParam("status") InvoiceStatus status) {
    // Implement logic to retrieve invoices with the specified criteria
    // and return the list of invoices
    Quarter quarter = Quarter.fromString(quarterString);

    log.info("Request invoices email: {}, from {} to {} ", email, quarter.getStartDate(), quarter.getEndDate());
    if(status != null){
      return invoiceService.searchInvoices(quarter.getStartDate(), quarter.getEndDate(), email, status);
    }
    else{
      return invoiceService.searchInvoices(quarter.getStartDate(), quarter.getEndDate(), email);
    }

  }

  @DeleteMapping("/invoices/delete/{id}")
  public ResponseEntity<String> deleteInvoice(@PathVariable Long id) {

    try {
      invoiceService.deleteById(id);
    } catch (RecordNotFoundException e) {
      return ResponseEntity.badRequest().body("Invoice not found");
    }
    return ResponseEntity.ok("Invoice deleted successfully");
  }

  @PutMapping("/invoices/{id}/update")
  public ResponseEntity<String> updateInvoice(@PathVariable Long id, @RequestParam(required = false) String email, @RequestParam(required = false) InvoiceStatus status) {
    // Perform the update logic here
    // You may want to validate and sanitize the input data before updating
    try {
      Optional<InvoiceEntity> invoiceFind = invoiceService.findById(id);
      if (invoiceFind.isEmpty()) {
        log.error("InvoiceId: {} not found", id);
        throw new RecordNotFoundException("Invoice with id " + id + " not found");
      }
      InvoiceEntity invoiceEntity = invoiceFind.get();
      if(email != null && !email.equalsIgnoreCase(invoiceFind.get().getReceiver())){
        invoiceService.updateReceiver(invoiceEntity, email, InvoiceUpdateType.MANUAL);
      }
      if(status != null &&  status != invoiceFind.get().getInvoiceStatus()){
        invoiceService.updateStatus(invoiceEntity, status, InvoiceUpdateType.MANUAL);
      }

    } catch (RecordNotFoundException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
    // Return a response (success or error message)
    return ResponseEntity.ok("Invoice updated successfully");
  }


  @GetMapping("/invoices/{invoiceId}/retry")
  public ResponseEntity<String> getInvoiceChanges(
      @PathVariable("invoiceId") Long invoiceId) {
    // Implement logic to retrieve the invoice changes
    try {
      processorService.retryFailedInvoice(invoiceId);
    } catch (Exception e) {
      log.error("InvoiceId: {}, Error when retrying", invoiceId, e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    }
    return ResponseEntity.ok("Retry successfully");

  }

  @GetMapping("/emails")
  public ResponseEntity<String> getEmails(@RequestParam("from") final ZonedDateTime from,
                                          @RequestParam("to") final ZonedDateTime to)
          throws Exception {
    //if to > from + 1 day, loop through the days and call the processorService.process
    // end run it in the new thread
//else call the processorService.process
    if (to.isAfter(from.plusDays(1))) {
      Thread thread = new Thread(() -> {
        ZonedDateTime fromTime = from;
        while (fromTime.isBefore(to)) {
          ZonedDateTime nextTime = fromTime.plusDays(1);
          if (nextTime.isAfter(to)) {
            nextTime = to;
          }
          try {
            processorService.process(fromTime, nextTime);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
          fromTime = nextTime;
        }
      }
      );
      thread.start();
      return ResponseEntity.ok("Emails Reading in progress, please check the logs for more details");
    } else {
      log.info("Received request to process emails from {} to {}", from, to);
      processorService.process(from, to);
      return ResponseEntity.ok("Emails processed successfully");

    }


  }

}


