package com.switchfully.spring.parkshark.feature.division.service;

import com.switchfully.spring.parkshark.feature.division.domain.DivisionRepository;
import org.springframework.stereotype.Service;

@Service
public class DivisionService {

    private final DivisionRepository divisionRepository;

    public DivisionService(DivisionRepository divisionRepository) {
        this.divisionRepository = divisionRepository;
    }

    public Division createDivision()
}
