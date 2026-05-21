package com.pms.service;

import com.pms.dto.PaymentDTO;
import com.pms.entity.Invoice;
import com.pms.entity.Payment;
import com.pms.exception.BadRequestException;
import com.pms.exception.ResourceNotFoundException;
import com.pms.repository.InvoiceRepository;
import com.pms.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    public List<PaymentDTO> getPaymentsByInvoice(Long invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public PaymentDTO processPayment(PaymentDTO dto) {
        Invoice invoice = invoiceRepository.findById(dto.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        if ("PAID".equals(invoice.getStatus())) {
            throw new BadRequestException("Invoice is already fully paid");
        }

        BigDecimal totalPaid = paymentRepository.findByInvoiceId(invoice.getId()).stream()
                .filter(p -> "COMPLETED".equals(p.getStatus()))
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remaining = invoice.getTotalAmount().subtract(totalPaid);
        if (dto.getAmount().compareTo(remaining) > 0) {
            throw new BadRequestException("Payment amount exceeds remaining balance of " + remaining);
        }

        Payment payment = Payment.builder()
                .invoice(invoice)
                .amount(dto.getAmount())
                .paymentMode(dto.getPaymentMode())
                .transactionRef(dto.getTransactionRef())
                .status("COMPLETED")
                .build();
        payment = paymentRepository.save(payment);

        BigDecimal newTotalPaid = totalPaid.add(dto.getAmount());
        if (newTotalPaid.compareTo(invoice.getTotalAmount()) >= 0) {
            invoice.setStatus("PAID");
        } else {
            invoice.setStatus("PARTIAL");
        }
        invoiceRepository.save(invoice);

        return toDTO(payment);
    }

    @Transactional
    public List<PaymentDTO> processSplitPayment(List<PaymentDTO> payments) {
        return payments.stream().map(this::processPayment).collect(Collectors.toList());
    }

    private PaymentDTO toDTO(Payment p) {
        PaymentDTO dto = new PaymentDTO();
        dto.setId(p.getId());
        dto.setInvoiceId(p.getInvoice().getId());
        dto.setInvoiceNumber(p.getInvoice().getInvoiceNumber());
        dto.setAmount(p.getAmount());
        dto.setPaymentMode(p.getPaymentMode());
        dto.setTransactionRef(p.getTransactionRef());
        dto.setStatus(p.getStatus());
        dto.setPaidAt(p.getPaidAt().toString());
        return dto;
    }
}
