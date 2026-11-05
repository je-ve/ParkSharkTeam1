package com.switchfully.spring.parkshark.feature.invoice.exception;

import com.switchfully.spring.parkshark.common.exception.NotFoundException;

public class InvoiceNotFoundException extends NotFoundException {
    public InvoiceNotFoundException(Long reference) {
        super("Could not find invoice with reference " + reference);
    }
}
