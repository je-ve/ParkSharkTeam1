package com.switchfully.spring.parkshark.feature.invoice.api;

import com.switchfully.spring.parkshark.feature.invoice.api.dto.InvoiceDto;
import com.switchfully.spring.parkshark.feature.invoice.service.InvoiceService;
import com.switchfully.spring.parkshark.feature.invoice.service.InvoiceService.MonthlyInvoice;
import com.switchfully.spring.parkshark.feature.security.authentication.AuthenticatedUser;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping
    @Secured("GET_MONTHLY_INVOICE")
    @Operation(summary = "Get my monthly invoice: generated (201) on the first call of the month, returned as is (200) afterwards")
    public ResponseEntity<InvoiceDto> getMonthlyInvoice(@AuthenticationPrincipal AuthenticatedUser user) {
        MonthlyInvoice monthlyInvoice = invoiceService.getMonthlyInvoice(user.id());
        return ResponseEntity
                .status(monthlyInvoice.created() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(monthlyInvoice.invoice());
    }

    @GetMapping
    @Secured("VIEW_INVOICES")
    @Operation(summary = "Get all invoices, most recent first")
    public List<InvoiceDto> getAllInvoices() {
        return invoiceService.getAllInvoices();
    }

    @PostMapping("/{reference}/close")
    @Secured("CLOSE_INVOICE")
    @Operation(summary = "Mark an invoice as closed (paid) by its reference")
    public InvoiceDto closeInvoice(@PathVariable Long reference) {
        return invoiceService.closeInvoice(reference);
    }
}
