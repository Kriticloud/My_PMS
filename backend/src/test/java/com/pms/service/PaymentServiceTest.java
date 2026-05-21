package com.pms.service;

import com.pms.dto.PaymentDTO;
import com.pms.entity.Invoice;
import com.pms.entity.Payment;
import com.pms.exception.BadRequestException;
import com.pms.exception.ResourceNotFoundException;
import com.pms.repository.InvoiceRepository;
import com.pms.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Invoice invoice;

    @BeforeEach
    void setUp() {
        invoice = Invoice.builder()
                .id(1L)
                .invoiceNumber("INV-00000001")
                .totalAmount(new BigDecimal("10000"))
                .status("PENDING")
                .build();
    }

    @Test
    void processPayment_fullPayment_setsInvoicePaid() {
        PaymentDTO dto = new PaymentDTO();
        dto.setInvoiceId(1L);
        dto.setAmount(new BigDecimal("10000"));
        dto.setPaymentMode("CASH");

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(paymentRepository.findByInvoiceId(1L)).thenReturn(Collections.emptyList());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(1L);
            p.setPaidAt(LocalDateTime.now());
            return p;
        });

        PaymentDTO result = paymentService.processPayment(dto);

        assertThat(invoice.getStatus()).isEqualTo("PAID");
        verify(invoiceRepository).save(invoice);
    }

    @Test
    void processPayment_partialPayment_setsPartiallyPaid() {
        PaymentDTO dto = new PaymentDTO();
        dto.setInvoiceId(1L);
        dto.setAmount(new BigDecimal("5000"));
        dto.setPaymentMode("CARD");

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(paymentRepository.findByInvoiceId(1L)).thenReturn(Collections.emptyList());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(1L);
            p.setPaidAt(LocalDateTime.now());
            return p;
        });

        PaymentDTO result = paymentService.processPayment(dto);

        assertThat(invoice.getStatus()).isEqualTo("PARTIALLY_PAID");
    }

    @Test
    void processPayment_invoiceNotFound_throwsNotFound() {
        PaymentDTO dto = new PaymentDTO();
        dto.setInvoiceId(99L);
        dto.setAmount(new BigDecimal("1000"));
        dto.setPaymentMode("CASH");

        when(invoiceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.processPayment(dto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void processPayment_exceedsTotal_throwsBadRequest() {
        PaymentDTO dto = new PaymentDTO();
        dto.setInvoiceId(1L);
        dto.setAmount(new BigDecimal("15000"));
        dto.setPaymentMode("CASH");

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(paymentRepository.findByInvoiceId(1L)).thenReturn(Collections.emptyList());

        assertThatThrownBy(() -> paymentService.processPayment(dto))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("exceeds");
    }

    @Test
    void processSplitPayment_multiplePayments_allProcessed() {
        PaymentDTO dto1 = new PaymentDTO();
        dto1.setInvoiceId(1L);
        dto1.setAmount(new BigDecimal("6000"));
        dto1.setPaymentMode("CASH");

        PaymentDTO dto2 = new PaymentDTO();
        dto2.setInvoiceId(1L);
        dto2.setAmount(new BigDecimal("4000"));
        dto2.setPaymentMode("CARD");

        when(invoiceRepository.findById(1L)).thenReturn(Optional.of(invoice));
        when(paymentRepository.findByInvoiceId(1L)).thenReturn(Collections.emptyList());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p.setId(1L);
            p.setPaidAt(LocalDateTime.now());
            return p;
        });

        List<PaymentDTO> results = paymentService.processSplitPayment(List.of(dto1, dto2));

        assertThat(results).hasSize(2);
        assertThat(invoice.getStatus()).isEqualTo("PAID");
    }

    @Test
    void getPaymentsByInvoice_returnsList() {
        Payment payment = Payment.builder()
                .id(1L)
                .invoice(invoice)
                .amount(new BigDecimal("5000"))
                .paymentMode("CASH")
                .status("COMPLETED")
                .paidAt(LocalDateTime.now())
                .build();

        when(paymentRepository.findByInvoiceId(1L)).thenReturn(List.of(payment));

        List<PaymentDTO> result = paymentService.getPaymentsByInvoice(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAmount()).isEqualByComparingTo("5000");
    }
}
