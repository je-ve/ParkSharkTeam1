package com.switchfully.spring.parkshark.feature.parkinglot.service;

import com.switchfully.spring.parkshark.feature.division.domain.Division;
import com.switchfully.spring.parkshark.feature.division.service.DivisionService;
import com.switchfully.spring.parkshark.feature.parkinglot.api.ContactPersonMapper;
import com.switchfully.spring.parkshark.feature.parkinglot.api.ParkingLotMapper;
import com.switchfully.spring.parkshark.feature.parkinglot.api.dto.ParkingLotDto;
import com.switchfully.spring.parkshark.feature.parkinglot.api.request.CreateContactPersonRequest;
import com.switchfully.spring.parkshark.feature.parkinglot.api.request.CreateParkingLotRequest;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.ContactPerson;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.ContactPersonRepository;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.ParkingLot;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.ParkingLotRepository;
import com.switchfully.spring.parkshark.feature.parkinglot.exception.ContactPersonConflictException;
import com.switchfully.spring.parkshark.feature.parkinglot.exception.ContactPersonNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ParkingLotService {

    private final ParkingLotRepository parkingLotRepository;
    private final ContactPersonRepository contactPersonRepository;
    private final DivisionService divisionService;
    private final ParkingLotMapper parkingLotMapper;
    private final ContactPersonMapper contactPersonMapper;

    public ParkingLotService(ParkingLotRepository parkingLotRepository,
                             ContactPersonRepository contactPersonRepository,
                             DivisionService divisionService,
                             ParkingLotMapper parkingLotMapper,
                             ContactPersonMapper contactPersonMapper) {
        this.parkingLotRepository = parkingLotRepository;
        this.contactPersonRepository = contactPersonRepository;
        this.divisionService = divisionService;
        this.parkingLotMapper = parkingLotMapper;
        this.contactPersonMapper = contactPersonMapper;
    }

    public ParkingLotDto createParkingLot(CreateParkingLotRequest request) {
        Division division = divisionService.getDivisionById(request.divisionId());
        ContactPerson contactPerson = request.contactPersonId() != null
                ? findContactPerson(request.contactPersonId())
                : createContactPerson(request.contactPerson());

        ParkingLot parkingLot = parkingLotRepository.save(
                parkingLotMapper.toEntity(request, contactPerson, division));
        return parkingLotMapper.toDto(parkingLot);
    }

    private ContactPerson findContactPerson(UUID id) {
        return contactPersonRepository.findById(id)
                .orElseThrow(() -> new ContactPersonNotFoundException(id));
    }

    private ContactPerson createContactPerson(CreateContactPersonRequest request) {
        ContactPerson contactPerson = contactPersonMapper.toEntity(request);
        contactPersonRepository.findByEmail(contactPerson.getEmail()).ifPresent(existing -> {
            throw ContactPersonConflictException.emailAlreadyUsed(existing.getEmail(), existing.getId());
        });
        try {
            return contactPersonRepository.saveAndFlush(contactPerson);
        } catch (DataIntegrityViolationException e) {
            throw ContactPersonConflictException.emailAlreadyUsed(contactPerson.getEmail(), e);
        }
    }
}
