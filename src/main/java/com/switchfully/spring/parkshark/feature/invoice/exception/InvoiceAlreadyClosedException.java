package com.switchfully.spring.parkshark.feature.invoice.exception;

import com.switchfully.spring.parkshark.common.exception.ConflictException;

public class InvoiceAlreadyClosedException extends ConflictException {
    public InvoiceAlreadyClosedException(Long reference) {
        super("Invoice %d is already closed.".formatted(reference));
    }
}
