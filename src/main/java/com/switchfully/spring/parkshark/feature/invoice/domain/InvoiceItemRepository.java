package com.switchfully.spring.parkshark.feature.invoice.domain;

import com.switchfully.spring.parkshark.feature.division.domain.Division;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InvoiceItemRepository  extends JpaRepository<InvoiceItem, UUID> {

}
