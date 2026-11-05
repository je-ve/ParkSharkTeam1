package com.switchfully.spring.parkshark.feature.invoice.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Optional<Invoice> findByReference(Long reference);

    Optional<Invoice> findByMemberIdAndBillingMonth(UUID memberId, LocalDate billingMonth);

    @Query("""
            select distinct i from Invoice i
            join fetch i.member
            left join fetch i.items it
            left join fetch it.allocation
            order by i.reference desc""")
    List<Invoice> findAllWithItems();
}
