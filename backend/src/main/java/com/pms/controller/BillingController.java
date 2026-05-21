package com.pms.controller;

import com.pms.dto.InvoiceDTO;
import com.pms.dto.PaymentDTO;
import com.pms.service.InvoiceService;
import com.pms.service.PaymentService;
import com.pms.service.PdfInvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final InvoiceService invoiceService;
    private final PaymentService paymentService;
    private final PdfInvoiceService pdfInvoiceService;

    @GetMapping("/invoices")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK')")
    public ResponseEntity<List<InvoiceDTO>> getAllInvoices() {
        return ResponseEntity.ok(invoiceService.getAllInvoices());
    }

    @GetMapping("/invoices/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK')")
    public ResponseEntity<InvoiceDTO> getInvoiceById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    @GetMapping("/invoices/booking/{bookingId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK')")
    public ResponseEntity<InvoiceDTO> getInvoiceByBooking(@PathVariable Long bookingId) {
        InvoiceDTO invoice = invoiceService.getInvoiceByBooking(bookingId);
        return invoice != null ? ResponseEntity.ok(invoice) : ResponseEntity.notFound().build();
    }

    @PostMapping("/invoices/generate/{bookingId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK')")
    public ResponseEntity<InvoiceDTO> generateInvoice(
            @PathVariable Long bookingId,
            @RequestParam(required = false) BigDecimal discount) {
        return ResponseEntity.ok(invoiceService.generateInvoice(bookingId, discount));
    }

    @PostMapping("/payments")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK')")
    public ResponseEntity<PaymentDTO> processPayment(@Valid @RequestBody PaymentDTO dto) {
        return ResponseEntity.ok(paymentService.processPayment(dto));
    }

    @PostMapping("/payments/split")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK')")
    public ResponseEntity<List<PaymentDTO>> processSplitPayment(@Valid @RequestBody List<PaymentDTO> payments) {
        return ResponseEntity.ok(paymentService.processSplitPayment(payments));
    }

    @GetMapping("/payments/invoice/{invoiceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK')")
    public ResponseEntity<List<PaymentDTO>> getPaymentsByInvoice(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(paymentService.getPaymentsByInvoice(invoiceId));
    }

    @GetMapping("/invoices/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN', 'FRONT_DESK')")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long id) {
        byte[] pdf = pdfInvoiceService.generateInvoicePdf(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "invoice-" + id + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
