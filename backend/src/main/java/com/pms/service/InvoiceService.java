package com.pms.service;

import com.pms.dto.InvoiceDTO;
import com.pms.dto.InvoiceItemDTO;
import com.pms.entity.*;
import com.pms.exception.BadRequestException;
import com.pms.exception.ResourceNotFoundException;
import com.pms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {

    private static final BigDecimal GST_RATE = new BigDecimal("0.18");

    private final InvoiceRepository invoiceRepository;
    private final BookingRepository bookingRepository;
    private final PosOrderRepository posOrderRepository;
    private final GuestRepository guestRepository;

    public List<InvoiceDTO> getAllInvoices() {
        return invoiceRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public InvoiceDTO getInvoiceById(Long id) {
        return toDTO(findInvoice(id));
    }

    public InvoiceDTO getInvoiceByBooking(Long bookingId) {
        return invoiceRepository.findByBookingId(bookingId).map(this::toDTO).orElse(null);
    }

    @Transactional
    public InvoiceDTO generateInvoice(Long bookingId, BigDecimal discountPercent) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));

        if (invoiceRepository.findByBookingId(bookingId).isPresent()) {
            throw new BadRequestException("Invoice already exists for this booking");
        }

        Invoice invoice = Invoice.builder()
                .invoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .booking(booking)
                .guest(booking.getGuest())
                .status("PENDING")
                .invoiceItems(new ArrayList<>())
                .build();

        BigDecimal subtotal = BigDecimal.ZERO;

        // Room charges
        InvoiceItem roomCharge = InvoiceItem.builder()
                .invoice(invoice)
                .description(
                        "Room " + booking.getRoom().getRoomNumber() + " - " + booking.getRoom().getRoomType().getName())
                .itemType("ROOM_CHARGE")
                .amount(booking.getTotalAmount())
                .referenceId(booking.getId())
                .build();
        invoice.getInvoiceItems().add(roomCharge);
        subtotal = subtotal.add(booking.getTotalAmount());

        // POS charges
        List<PosOrder> posOrders = posOrderRepository.findByBookingId(bookingId);
        for (PosOrder posOrder : posOrders) {
            if (!"CANCELLED".equals(posOrder.getStatus())) {
                InvoiceItem posCharge = InvoiceItem.builder()
                        .invoice(invoice)
                        .description("POS Order " + posOrder.getOrderNumber() + " (" + posOrder.getOrderType() + ")")
                        .itemType("POS_CHARGE")
                        .amount(posOrder.getTotalAmount())
                        .referenceId(posOrder.getId())
                        .build();
                invoice.getInvoiceItems().add(posCharge);
                subtotal = subtotal.add(posOrder.getTotalAmount());
            }
        }

        // Apply discount
        if (discountPercent != null && discountPercent.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discountAmount = subtotal.multiply(discountPercent).divide(BigDecimal.valueOf(100), 2,
                    RoundingMode.HALF_UP);
            InvoiceItem discountItem = InvoiceItem.builder()
                    .invoice(invoice)
                    .description("Discount (" + discountPercent + "%)")
                    .itemType("DISCOUNT")
                    .amount(discountAmount.negate())
                    .build();
            invoice.getInvoiceItems().add(discountItem);
            subtotal = subtotal.subtract(discountAmount);
        }

        BigDecimal taxAmount = subtotal.multiply(GST_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotal.add(taxAmount);

        invoice.setSubtotal(subtotal);
        invoice.setTaxAmount(taxAmount);
        invoice.setTotalAmount(totalAmount);

        return toDTO(invoiceRepository.save(invoice));
    }

    @Transactional
    public InvoiceDTO updateInvoiceStatus(Long id, String status) {
        Invoice invoice = findInvoice(id);
        invoice.setStatus(status);
        return toDTO(invoiceRepository.save(invoice));
    }

    private Invoice findInvoice(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
    }

    private InvoiceDTO toDTO(Invoice inv) {
        InvoiceDTO dto = InvoiceDTO.builder()
                .id(inv.getId())
                .invoiceNumber(inv.getInvoiceNumber())
                .bookingId(inv.getBooking().getId())
                .bookingNumber(inv.getBooking().getBookingNumber())
                .guestId(inv.getGuest().getId())
                .guestName(inv.getGuest().getFirstName() + " " + inv.getGuest().getLastName())
                .roomNumber(inv.getBooking().getRoom().getRoomNumber())
                .subtotal(inv.getSubtotal())
                .taxAmount(inv.getTaxAmount())
                .totalAmount(inv.getTotalAmount())
                .status(inv.getStatus())
                .generatedAt(inv.getGeneratedAt().toString())
                .notes(inv.getNotes())
                .items(inv.getInvoiceItems().stream().map(this::toItemDTO).collect(Collectors.toList()))
                .build();
        return dto;
    }

    private InvoiceItemDTO toItemDTO(InvoiceItem item) {
        return InvoiceItemDTO.builder()
                .id(item.getId())
                .description(item.getDescription())
                .itemType(item.getItemType())
                .amount(item.getAmount())
                .referenceId(item.getReferenceId())
                .build();
    }
}
