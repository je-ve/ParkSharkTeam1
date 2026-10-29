package com.switchfully.spring.parkshark.feature.division.service;

import com.switchfully.spring.parkshark.feature.division.api.request.CreateDivisionRequest;
import com.switchfully.spring.parkshark.feature.division.domain.Division;
import com.switchfully.spring.parkshark.feature.division.domain.DivisionRepository;
import com.switchfully.spring.parkshark.feature.division.exception.DivisionConflictException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.StreamSupport;

@Service
public class DivisionService {

    private final DivisionRepository divisionRepository;

    public DivisionService(DivisionRepository divisionRepository) {
        this.divisionRepository = divisionRepository;
    }

    public Division createDivision(CreateDivisionRequest request) {
        if(divisionRepository.existsByName(request.name()))
            throw DivisionConflictException.duplicateDivisionException(request.name());
        return divisionRepository.save(new Division(request.name(), request.originalName(), request.director()));
    }

    public List<Division> listAllDivisions() {
        return StreamSupport.stream(divisionRepository.findAll().spliterator(), false).toList();
    }
}
