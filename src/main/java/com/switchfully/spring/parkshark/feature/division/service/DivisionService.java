package com.switchfully.spring.parkshark.feature.division.service;

import com.switchfully.spring.parkshark.feature.division.api.DivisionMapper;
import com.switchfully.spring.parkshark.feature.division.api.dto.DivisionDto;
import com.switchfully.spring.parkshark.feature.division.api.request.CreateDivisionRequest;
import com.switchfully.spring.parkshark.feature.division.domain.Division;
import com.switchfully.spring.parkshark.feature.division.domain.DivisionRepository;
import com.switchfully.spring.parkshark.feature.division.exception.DivisionConflictException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class DivisionService {

    private final DivisionRepository divisionRepository;
    private final DivisionMapper divisionMapper;

    public DivisionService(DivisionRepository divisionRepository, DivisionMapper divisionMapper) {
        this.divisionRepository = divisionRepository;
        this.divisionMapper = divisionMapper;
    }

    public DivisionDto createDivision(CreateDivisionRequest request) {
        if(divisionRepository.existsByName(request.name()))
            throw DivisionConflictException.duplicateDivisionException(request.name());
        return divisionMapper.mapToDto(
                divisionRepository.save(
                        new Division(
                                request.name(),
                                request.originalName(),
                                request.director())
                )
        );
    }

    public List<DivisionDto> listAllDivisions() {
        return divisionMapper.mapToDto(
                divisionRepository.findAll()
        );
    }
}
