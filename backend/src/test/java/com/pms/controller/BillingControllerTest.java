package com.pms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.dto.InvoiceDTO;
import com.pms.dto.InvoiceItemDTO;
import com.pms.dto.PaymentDTO;
import com.pms.security.JwtTokenProvider;
import com.pms.security.CustomUserDetailsService;
import com.pms.service.InvoiceService;
import com.pms.service.PaymentService;
import com.pms.service.PdfInvoiceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.context.annotation.Import;
import com.pms.config.TestSecurityConfig;
import org.springframework.context.annotation.Import;
import com.pms.config.TestSecurityConfig;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestSecurityConfig.class)
@Import(TestSecurityConfig.class)
@WebMvcTest(BillingController.class)
class BillingControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InvoiceService invoiceService;
    @MockitoBean
    private PaymentService paymentService;
    @MockitoBean
    private PdfInvoiceService pdfInvoiceService;
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private InvoiceDTO sampleInvoice() {
        return InvoiceDTO.builder()
                .id(1L)
                .invoiceNumber("INV-TEST0001")
                .bookingId(1L)
                .bookingNumber("BK-TEST0001")
                .guestId(1L)
                .guestName("John Doe")
                .roomNumber("101")
                .subtotal(new BigDecimal("9000"))
                .taxAmount(new BigDecimal("1620"))
                .totalAmount(new BigDecimal("10620"))
                .status("PENDING")
                .generatedAt("2026-05-21T10:00:00")
                .items(List.of(InvoiceItemDTO.builder()
                        .id(1L)
                        .description("Room 101 - Deluxe")
                        .itemType("ROOM_CHARGE")
                        .amount(new BigDecimal("9000"))
                        .build()))
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getAllInvoices_returns200() throws Exception {
        when(invoiceService.getAllInvoices()).thenReturn(List.of(sampleInvoice()));

        mockMvc.perform(get("/api/billing/invoices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].invoiceNumber").value("INV-TEST0001"));
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void generateInvoice_returns201() throws Exception {
        when(invoiceService.generateInvoice(eq(1L), any())).thenReturn(sampleInvoice());

        mockMvc.perform(post("/api/billing/invoices/generate/1").with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalAmount").value(10620));
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void generateInvoice_withDiscount_returns201() throws Exception {
        when(invoiceService.generateInvoice(eq(1L), any())).thenReturn(sampleInvoice());

        mockMvc.perform(post("/api/billing/invoices/generate/1").with(csrf())
                .param("discount", "10"))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void processPayment_valid_returns200() throws Exception {
        PaymentDTO payment = new PaymentDTO();
        payment.setInvoiceId(1L);
        payment.setAmount(new BigDecimal("10620"));
        payment.setPaymentMode("CASH");

        PaymentDTO result = new PaymentDTO();
        result.setId(1L);
        result.setInvoiceId(1L);
        result.setAmount(new BigDecimal("10620"));
        result.setPaymentMode("CASH");
        result.setStatus("COMPLETED");

        when(paymentService.processPayment(any(PaymentDTO.class))).thenReturn(result);

        mockMvc.perform(post("/api/billing/payments").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void processPayment_missingAmount_returns400() throws Exception {
        PaymentDTO payment = new PaymentDTO();
        payment.setInvoiceId(1L);
        payment.setPaymentMode("CASH");
        // amount is null

        mockMvc.perform(post("/api/billing/payments").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payment)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void processSplitPayment_returns200() throws Exception {
        PaymentDTO p1 = new PaymentDTO();
        p1.setInvoiceId(1L);
        p1.setAmount(new BigDecimal("5000"));
        p1.setPaymentMode("CASH");

        PaymentDTO p2 = new PaymentDTO();
        p2.setInvoiceId(1L);
        p2.setAmount(new BigDecimal("5620"));
        p2.setPaymentMode("CARD");

        when(paymentService.processSplitPayment(any())).thenReturn(List.of(p1, p2));

        mockMvc.perform(post("/api/billing/payments/split").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(List.of(p1, p2))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void downloadPdf_returns200WithPdf() throws Exception {
        when(pdfInvoiceService.generateInvoicePdf(1L)).thenReturn(new byte[] { 1, 2, 3 });

        mockMvc.perform(get("/api/billing/invoices/1/pdf"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/pdf"));
    }

    @Test
    @WithMockUser(roles = "RESTAURANT_STAFF")
    void getAllInvoices_forbiddenForRestaurantStaff() throws Exception {
        mockMvc.perform(get("/api/billing/invoices"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "FRONT_DESK")
    void getPaymentsByInvoice_returns200() throws Exception {
        when(paymentService.getPaymentsByInvoice(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/billing/payments/invoice/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}
