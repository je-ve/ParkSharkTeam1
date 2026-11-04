package com.switchfully.spring.parkshark.feature.invoice.service;

import com.switchfully.spring.parkshark.feature.invoice.domain.InvoiceItemRepository;
import com.switchfully.spring.parkshark.feature.invoice.domain.InvoiceRepository;
import com.switchfully.spring.parkshark.feature.member.domain.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class InvoiceService {
    private InvoiceRepository invoiceRepository;
    private InvoiceItemRepository invoiceItemRepository;
    private MemberRepository memberRepository;

    public InvoiceService(InvoiceRepository invoiceRepository, InvoiceItemRepository invoiceItemRepository, MemberRepository memberRepository) {
        this.invoiceRepository = invoiceRepository;
        this.invoiceItemRepository = invoiceItemRepository;
        this.memberRepository = memberRepository;
    }


}
