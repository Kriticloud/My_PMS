package com.pms.service;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.pms.entity.Invoice;
import com.pms.entity.InvoiceItem;
import com.pms.exception.ResourceNotFoundException;
import com.pms.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class PdfInvoiceService {

    private final InvoiceRepository invoiceRepository;

    public byte[] generateInvoicePdf(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document doc = new Document(pdf);

            DeviceRgb headerColor = new DeviceRgb(79, 70, 229);

            // Header
            doc.add(new Paragraph("PROPERTY MANAGEMENT SYSTEM")
                    .setFontSize(22).setBold().setFontColor(headerColor)
                    .setTextAlignment(TextAlignment.CENTER));
            doc.add(new Paragraph("TAX INVOICE")
                    .setFontSize(16).setFontColor(ColorConstants.GRAY)
                    .setTextAlignment(TextAlignment.CENTER).setMarginBottom(20));

            // Invoice details
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .useAllAvailableWidth().setMarginBottom(15);

            infoTable.addCell(createInfoCell("Invoice #", invoice.getInvoiceNumber()));
            infoTable.addCell(createInfoCell("Date", invoice.getGeneratedAt().toLocalDate().toString()));
            infoTable.addCell(createInfoCell("Guest", invoice.getGuest().getFirstName() + " " + invoice.getGuest().getLastName()));
            infoTable.addCell(createInfoCell("Room", invoice.getBooking().getRoom().getRoomNumber()));
            infoTable.addCell(createInfoCell("Booking #", invoice.getBooking().getBookingNumber()));
            infoTable.addCell(createInfoCell("Status", invoice.getStatus()));
            doc.add(infoTable);

            // Items table
            Table itemTable = new Table(UnitValue.createPercentArray(new float[]{4, 2, 2}))
                    .useAllAvailableWidth().setMarginBottom(15);

            itemTable.addHeaderCell(new Cell().add(new Paragraph("Description").setBold())
                    .setBackgroundColor(headerColor).setFontColor(ColorConstants.WHITE));
            itemTable.addHeaderCell(new Cell().add(new Paragraph("Type").setBold())
                    .setBackgroundColor(headerColor).setFontColor(ColorConstants.WHITE));
            itemTable.addHeaderCell(new Cell().add(new Paragraph("Amount").setBold())
                    .setBackgroundColor(headerColor).setFontColor(ColorConstants.WHITE)
                    .setTextAlignment(TextAlignment.RIGHT));

            for (InvoiceItem item : invoice.getInvoiceItems()) {
                itemTable.addCell(new Cell().add(new Paragraph(item.getDescription())));
                itemTable.addCell(new Cell().add(new Paragraph(item.getItemType())));
                itemTable.addCell(new Cell().add(new Paragraph("₹" + item.getAmount().toString()))
                        .setTextAlignment(TextAlignment.RIGHT));
            }
            doc.add(itemTable);

            // Totals
            Table totals = new Table(UnitValue.createPercentArray(new float[]{3, 1}))
                    .useAllAvailableWidth();
            totals.addCell(new Cell().add(new Paragraph("Subtotal").setBold())
                    .setBorder(null).setTextAlignment(TextAlignment.RIGHT));
            totals.addCell(new Cell().add(new Paragraph("₹" + invoice.getSubtotal()))
                    .setBorder(null).setTextAlignment(TextAlignment.RIGHT));
            totals.addCell(new Cell().add(new Paragraph("GST (18%)").setBold())
                    .setBorder(null).setTextAlignment(TextAlignment.RIGHT));
            totals.addCell(new Cell().add(new Paragraph("₹" + invoice.getTaxAmount()))
                    .setBorder(null).setTextAlignment(TextAlignment.RIGHT));
            totals.addCell(new Cell().add(new Paragraph("TOTAL").setBold().setFontSize(14))
                    .setBorder(null).setTextAlignment(TextAlignment.RIGHT));
            totals.addCell(new Cell().add(new Paragraph("₹" + invoice.getTotalAmount()).setBold().setFontSize(14))
                    .setBorder(null).setTextAlignment(TextAlignment.RIGHT));
            doc.add(totals);

            doc.add(new Paragraph("\nThank you for your stay!")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY).setMarginTop(30));

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF", e);
        }
    }

    private Cell createInfoCell(String label, String value) {
        return new Cell().add(new Paragraph(label + ": " + value).setFontSize(10))
                .setBorder(null);
    }
}
