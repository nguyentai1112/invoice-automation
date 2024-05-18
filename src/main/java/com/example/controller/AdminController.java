package com.example.controller;

import com.example.dto.Invoice;
import com.example.dto.Quarter;
import com.example.dto.InvoiceDataInfo;
import com.example.exception.DuplicateInfoException;
import com.example.exception.PdfParsingException;
import com.example.exception.RecordNotFoundException;
import com.example.persistence.config.RunLogEntity;
import com.example.persistence.config.RunLogService;
import com.example.persistence.invoice.InvoiceEntity;
import com.example.persistence.invoice.InvoiceService;
import com.example.persistence.invoice.InvoiceStatus;
import com.example.persistence.invoice.InvoiceUpdateEntity;
import com.example.persistence.receiver.ReceiverService;
import com.example.service.ExcelService;
import com.example.service.ProcessorService;
import com.example.util.StringUtil;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@AllArgsConstructor
@Slf4j
public class AdminController {

    private final ProcessorService processorService;
    private final InvoiceService invoiceService;
    private final ReceiverService receiverService;
    private final ExcelService excelService;
    private final RunLogService runLogService;
    private static final String CURRENT_DIRECTORY = System.getProperty("user.dir");
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");




    @GetMapping("/invoices")
    public String listInvoices(Model model) {
        List<String> quarters = Quarter.getQuarterList(
                ZonedDateTime.now().withZoneSameInstant(ZoneId.systemDefault()));
        List<InvoiceEntity> invoices = invoiceService.getAllInvoices();
        model.addAttribute("quarters", quarters);
        model.addAttribute("invoices", invoices);
        return "invoices";
    }

    @GetMapping("/invoices/{invoiceId}/details")
    public String getInvoiceChanges(Model model,
                                    @PathVariable("invoiceId") Long invoiceId) throws RecordNotFoundException {
        // Implement logic to retrieve the invoice changes
        // and return the list of invoices
        Optional<InvoiceEntity> optional = invoiceService.findById(invoiceId);
        if (optional.isEmpty()) {
            throw new RecordNotFoundException("Invoice not found");
        }
        model.addAttribute("invoice", optional.get());
        model.addAttribute("outbox", optional.get().getOutBoxEntity());

        List<InvoiceUpdateEntity> updates = invoiceService.getInvoiceChanges(invoiceId);
        model.addAttribute("updates", updates);
        return "details";
    }

    @GetMapping("/invoices/download-excel")
    public ResponseEntity<byte[]> downloadExcelFile(@RequestParam("quarter") String quarterString,
                                                    @RequestParam("email") String email) {
        Quarter quarter = Quarter.fromString(quarterString);
        try {
            List<InvoiceEntity> invoices = invoiceService.searchInvoicesForExcelExport(
                    quarter.getStartDate(),
                    quarter.getEndDate(), email, InvoiceStatus.SUCCESS);
            // JsonUtil.writeInvoiceListToFile(invoices, "tests/invoices.json");
            // Call the service method to create the Excel file
            String fileName = StringUtil.generateFileName(email);
            Workbook workbook = excelService.processInvoices(invoices);
            // Write the Workbook content to a ByteArrayOutputStream
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            workbook.write(byteArrayOutputStream);
            byte[] excelBytes = byteArrayOutputStream.toByteArray();

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName + ".xlsx ");

            // Set the content type of the response
            headers.add(HttpHeaders.CONTENT_TYPE,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            // Return the Excel file bytes with appropriate headers
            return new ResponseEntity<>(excelBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            // Handle exceptions appropriately (e.g., log the error)
            log.error("Error while downloading excel file for email: {} and quarter: {}", email,
                    quarterString, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/invoices/unsuccessful-invoices")
    public String getInvoices(Model model) {
        List<InvoiceDataInfo> invoices = new ArrayList<>();
        List<InvoiceEntity> invoiceEntities = invoiceService.searchUnsuccessfulInvoices();
        for (InvoiceEntity invoiceEntity : invoiceEntities) {
            InvoiceDataInfo invoice = InvoiceDataInfo.builder()
                    .id(invoiceEntity.getId())
                    .filePath(invoiceEntity.getFilePath())
                    .fileName(invoiceEntity.getFileName())
                    .emailTitle(invoiceEntity.getOutBoxEntity().getTitle())
                    .sentTime(invoiceEntity.getOutBoxEntity().getSentTime())
                    .receiver(invoiceEntity.getOutBoxEntity().getReceiver().getEmail())
                    .status(invoiceEntity.getInvoiceStatus().name())
                    .errorMessage(invoiceEntity.getErrorMessage())
                    .messageId(invoiceEntity.getOutBoxEntity().getMessageId())
                    .build();

            invoices.add(invoice);
        }
        model.addAttribute("invoices", invoices);
        return "unsuccessful-invoices";


    }


    @GetMapping("/invoices/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) throws RecordNotFoundException {
        Optional<InvoiceEntity> found = invoiceService.findById(id);
        if (found.isEmpty()) {
            throw new RecordNotFoundException("Invoice id: " + id + " not found");
        }
        model.addAttribute("invoice", found.get());
        return "edit-invoice";
    }

    @PostMapping("/invoices/{id}/edit")
    public String handleEditForm(@PathVariable Long id, Invoice updatedInvoice)
            throws PdfParsingException, RecordNotFoundException, DuplicateInfoException {
        // Assuming you have a method in your service to update the invoice
        updatedInvoice.checkValidation();
        Optional<InvoiceEntity> found = invoiceService.findExistingInvoice(updatedInvoice);
        if (found.isPresent() && found.get().getId().longValue() != id) {
            throw new DuplicateInfoException("Invoice Info already exists, please use another info");
        }
        ;

        invoiceService.updateInvoiceInfo(id, updatedInvoice);
        return "redirect:/invoices/" + id + "/details";
    }

    @GetMapping("/invoices/by-sent-date")
    public String getInvoicesByDate(Model model, @RequestParam(value = "date", required = false)  String dateString) {
        LocalDate localDate;
        if (dateString != null) {
            localDate = LocalDate.parse(dateString, DATE_FORMATTER);
        } else {
            localDate = LocalDate.now();
        }

        ZonedDateTime date = localDate.atStartOfDay(ZoneId.systemDefault());
        List<InvoiceEntity> invoiceEntities = invoiceService.searchInvoicesBySentDate(date);
        List<InvoiceDataInfo> invoices = InvoiceDataInfo.fromInvoiceEntityData(invoiceEntities);

        model.addAttribute("invoices", invoices);
        model.addAttribute("date", localDate.format(DATE_FORMATTER));
        return "by-sent-date-invoices";

    }

    @GetMapping("/invoices/by-invoice-date")
    public String getInvoicesByInvoiceDate(Model model, @RequestParam(value = "date", required = false)  String dateString) {
        LocalDate localDate;
        if (dateString != null) {
            localDate = LocalDate.parse(dateString, DATE_FORMATTER);
        } else {
            localDate = LocalDate.now();
        }

        ZonedDateTime date = localDate.atStartOfDay(ZoneId.systemDefault());
        List<InvoiceEntity> invoiceEntities = invoiceService.searchInvoicesByInvoiceDate(date);
        List<InvoiceDataInfo> invoices = InvoiceDataInfo.fromInvoiceEntityData(invoiceEntities);

        model.addAttribute("invoices", invoices);
        model.addAttribute("date", localDate.format(DATE_FORMATTER));
        return "by-invoice-date-invoices";

    }
    @GetMapping("/config/run-log")
    public String getRunLog(Model model) {
        List<RunLogEntity> config = runLogService.getAll();
        model.addAttribute("runLogs", config);
        return "run-log";
    }

}