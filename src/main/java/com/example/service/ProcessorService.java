package com.example.service;

import com.example.dto.Attachment;
import com.example.dto.EmailOutBox;
import com.example.dto.Invoice;
import com.example.exception.ConcurrentExecutionException;
import com.example.exception.InternalException;
import com.example.exception.PdfParsingException;
import com.example.exception.RecordNotFoundException;
import com.example.persistence.invoice.InvoiceEntity;
import com.example.persistence.invoice.InvoiceService;
import com.example.persistence.invoice.InvoiceStatus;
import com.example.persistence.invoice.InvoiceUpdateType;
import com.example.persistence.outbox.OutBoxEntity;
import com.example.persistence.outbox.OutBoxService;

import java.io.IOException;
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
public class ProcessorService {

    private final GmailApiService gmailApiService;
    private final ImageService imageService;
    private final PdfService pdfService;
    private final OutBoxService outBoxService;
    private final InvoiceService invoiceService;

    public static boolean isLocked = false;


    public int process(ZonedDateTime fromTime, ZonedDateTime toTime)
            throws InternalException, IOException, PdfParsingException, InterruptedException {
        if(isLocked){
            log.warn("Processing is running by another thread, please wait for a while");
            throw new ConcurrentExecutionException("Processing is running by another thread, please wait for a while");
        }
        isLocked = true;
        //Thread.sleep(20000);

        try {
            log.info("Start processing from start time: {} to end time: {}",
                    fromTime, toTime);
            List<EmailOutBox> outboxes = gmailApiService.getEmailMessageInfo(
                    fromTime, toTime);
            int successNum = 0;
            int duplicateNum = 0;
            for (EmailOutBox outbox : outboxes) {
                if (outBoxService.checkIfExisting(outbox.getMessageId())) {
                    log.warn("Message {} is existing", outbox.getMessageId());
                    duplicateNum++;
                    continue;
                }
                readPdfAndPersistData(outbox);
                successNum++;
            }
            log.info("{} have been retrieved, {} have been processed, "
                    + "{} was duplicated ", outboxes.size(), successNum, duplicateNum);
            return successNum;
        } finally {
            isLocked = false;
        }
    }

    public void readPdfAndPersistData(EmailOutBox outbox)
            throws PdfParsingException, IOException, InternalException {

        OutBoxEntity outBoxEntity = outBoxService.insertOutBoxInfo(outbox);
        List<Attachment> attachments = outbox.getAttachments();
        String failedInvoices = "";
        for (Attachment attachment : attachments) {
            String savedPath = pdfService.backupPdfAttachment(outbox.getMessageId(), attachment,
                    outbox.getSentTime());
            Invoice invoice = null;
            try {
                invoice = pdfService.readPdf(attachment.getData());
                //Thread.sleep(200000);
            } catch (Exception e) {
                log.error("Error when parsing pdf attachment, please check at path: {}", savedPath, e);
                failedInvoices += savedPath + ",";
                invoiceService.insertFailedInvoice(outBoxEntity, attachment.getAttachmentId(), savedPath, e.getMessage());
                continue;
            }

            Optional<InvoiceEntity> foundInvoice = invoiceService.findExistingInvoice(
                    invoice);
            if (foundInvoice.isPresent()) {
                try {
                    invoiceService.updateReceiver(foundInvoice.get(),
                            outBoxEntity.getReceiver().getEmail(), InvoiceUpdateType.SYSTEM);
                } catch (RecordNotFoundException e) {
                    log.error("Error when updating receiver for invoice id: {}", foundInvoice.get().getId(),
                            e);
                    throw new RuntimeException(e);
                }
            } else {
                invoice.setAttachmentId(attachment.getAttachmentId());
                invoice.setFilePath(savedPath);
                invoiceService.insertInvoice(outBoxEntity, invoice);
            }
        }

        outBoxEntity.setFailedInvoices(failedInvoices);
        outBoxEntity.setInvoiceNum(attachments.size());
        outBoxService.update(outBoxEntity);
    }

    public void retryFailedInvoice(long invoiceId) throws RecordNotFoundException {
        log.info("InvoiceId: {} Start retrying...", invoiceId);
        final Optional<InvoiceEntity> found = invoiceService.findById(invoiceId);
        if (found.isEmpty()) {
            log.error("Invoice {} is not found", invoiceId);
            throw new RecordNotFoundException("Invoice " + invoiceId + " is not found");
        }
        final InvoiceEntity invoiceEntity = found.get();
        // if(invoiceEntity.getInvoiceStatus() != InvoiceStatus.PARSE_FAILED){
        // log.error("InvoiceId: {}, status is {} not the same as", invoiceId,invoiceEntity.getInvoiceStatus(), InvoiceStatus.PARSE_FAILED );
        //throw new RecordNotFoundException("InvoiceId: " + invoiceId + ", status is " + invoiceEntity.getInvoiceStatus() + " not the same as " + InvoiceStatus.PARSE_FAILED);
        //}

        try {
            Invoice invoice = pdfService.readPdf(invoiceEntity.getFilePath());
            invoiceEntity.updateFromInvoice(invoice);
            invoiceEntity.setInvoiceStatus(InvoiceStatus.SUCCESS);
            invoiceEntity.setErrorMessage(null);
            invoiceService.updateForRetry(invoiceEntity);
        } catch (Exception e) {
            log.error("Error when retrying invoice {}", invoiceId, e);
            invoiceEntity.setErrorMessage(e.getMessage());
            invoiceEntity.setInvoiceStatus(InvoiceStatus.PARSE_FAILED);
            invoiceService.updateForRetry(invoiceEntity);

            throw new RuntimeException(e);
        }

    }
}

