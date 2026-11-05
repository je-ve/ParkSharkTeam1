package com.switchfully.spring.parkshark.feature.invoice.service;

import com.switchfully.spring.parkshark.feature.allocation.domain.Allocation;
import com.switchfully.spring.parkshark.feature.allocation.domain.AllocationRepository;
import com.switchfully.spring.parkshark.feature.allocation.domain.AllocationStatus;
import com.switchfully.spring.parkshark.feature.invoice.api.InvoiceMapper;
import com.switchfully.spring.parkshark.feature.invoice.api.dto.InvoiceDto;
import com.switchfully.spring.parkshark.feature.invoice.domain.Invoice;
import com.switchfully.spring.parkshark.feature.invoice.domain.InvoiceRepository;
import com.switchfully.spring.parkshark.feature.invoice.domain.ParkingPrice;
import com.switchfully.spring.parkshark.feature.invoice.exception.InvoiceConflictException;
import com.switchfully.spring.parkshark.feature.invoice.exception.InvoiceNotFoundException;
import com.switchfully.spring.parkshark.feature.member.domain.Member;
import com.switchfully.spring.parkshark.feature.member.domain.MemberRepository;
import com.switchfully.spring.parkshark.feature.member.exception.MemberNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class InvoiceService {

    static final int PAYMENT_TERM_DAYS = 30;

    private final InvoiceRepository invoiceRepository;
    private final AllocationRepository allocationRepository;
    private final MemberRepository memberRepository;
    private final InvoiceMapper invoiceMapper;
    private final Clock clock;

    public InvoiceService(InvoiceRepository invoiceRepository, AllocationRepository allocationRepository,
                          MemberRepository memberRepository, InvoiceMapper invoiceMapper, Clock clock) {
        this.invoiceRepository = invoiceRepository;
        this.allocationRepository = allocationRepository;
        this.memberRepository = memberRepository;
        this.invoiceMapper = invoiceMapper;
        this.clock = clock;
    }

    public MonthlyInvoice getMonthlyInvoice(UUID credentialsId) {
        Member member = memberRepository.findByCredentialsId(credentialsId)
                .orElseThrow(() -> MemberNotFoundException.forLoggedInUser(credentialsId));
        LocalDate today = LocalDate.now(clock);

        return invoiceRepository.findByMemberIdAndBillingMonth(member.getId(), Invoice.billingMonthOf(today))
                .map(existing -> new MonthlyInvoice(invoiceMapper.toDto(existing), false))
                .orElseGet(() -> new MonthlyInvoice(generateInvoice(member, today), true));
    }

    private InvoiceDto generateInvoice(Member member, LocalDate today) {
        Invoice invoice = new Invoice(member, today, today.plusDays(PAYMENT_TERM_DAYS));

        List<Allocation> stoppedAllocations =
                allocationRepository.findWithParkingLotByMemberIdAndStatus(member.getId(), AllocationStatus.STOPPED);
        for (Allocation allocation : stoppedAllocations) {
            invoice.addItem(allocation, ParkingPrice.calculate(
                    allocation.getParkedDuration(),
                    allocation.getParkingLot().getPricePerHour(),
                    member.getMembershipLevel()));
        }

        try {
            return invoiceMapper.toDto(invoiceRepository.saveAndFlush(invoice));
        } catch (DataIntegrityViolationException e) {
            throw new InvoiceConflictException(e);
        }
    }

    /** INV2 */
    @Transactional(readOnly = true)
    public List<InvoiceDto> getAllInvoices() {
        return invoiceMapper.toDto(invoiceRepository.findAllWithItems());
    }

    /** INV3 */
    public InvoiceDto closeInvoice(Long reference) {
        Invoice invoice = invoiceRepository.findByReference(reference)
                .orElseThrow(() -> new InvoiceNotFoundException(reference));
        invoice.close(LocalDate.now(clock));
        return invoiceMapper.toDto(invoice);
    }

    public record MonthlyInvoice(InvoiceDto invoice, boolean created) {
    }
}
