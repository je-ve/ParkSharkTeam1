package com.switchfully.spring.parkshark.feature.division.service;

import com.switchfully.spring.parkshark.feature.division.api.DivisionMapper;
import com.switchfully.spring.parkshark.feature.division.api.dto.DivisionDto;
import com.switchfully.spring.parkshark.feature.division.api.dto.DivisionOverviewDto;
import com.switchfully.spring.parkshark.feature.division.api.request.CreateDivisionRequest;
import com.switchfully.spring.parkshark.feature.division.domain.Division;
import com.switchfully.spring.parkshark.feature.division.domain.DivisionRepository;
import com.switchfully.spring.parkshark.feature.division.exception.DivisionConflictException;
import com.switchfully.spring.parkshark.feature.division.exception.DivisionNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

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
        if (divisionRepository.existsByName(request.name()))
            throw DivisionConflictException.duplicateDivisionException(request.name());
        try {
            Division division = divisionRepository.saveAndFlush(
                    new Division(
                            request.name(),
                            request.originalName(),
                            request.director()));
            return divisionMapper.mapToDto(division);
        } catch (DataIntegrityViolationException e) {
            throw DivisionConflictException.duplicateDivisionException(request.name(), e);
        }
    }

    public DivisionOverviewDto createSubdivision(CreateDivisionRequest request, UUID parentId) {
        verifyParameterIsFilledIn(parentId.toString(), "ID");
        if (!divisionRepository.existsById(parentId))
            throw DivisionNotFoundException.divisionNotFoundException(parentId.toString());
        if (divisionRepository.existsByName(request.name()))
            throw DivisionConflictException.duplicateDivisionException(request.name());
        try {
            Division division = divisionRepository.saveAndFlush(
                    new Division(
                            request.name(),
                            request.originalName(),
                            request.director(),
                            parentId
                    )
            );
            return divisionMapper.mapToDto(
                    division,
                    divisionRepository.getById(parentId),
                    List.of()
            );
        } catch (DataIntegrityViolationException e) {
            throw DivisionConflictException.duplicateDivisionException(request.name(), e);
        }
    }

    @Transactional(readOnly = true)
    public DivisionOverviewDto getDivisionOverview(UUID id) {
        verifyParameterIsFilledIn(id.toString(), "ID");
        if (!divisionRepository.existsById(id))
            throw DivisionNotFoundException.divisionNotFoundException(id.toString());

        Division division = divisionRepository.getById(id);

        DivisionDto parent = null;
        if (division.getParentId() != null && divisionRepository.existsById(division.getParentId()))
            parent = divisionMapper.mapToDto(divisionRepository.getById(division.getParentId()));

        List<DivisionDto> children = divisionMapper.mapToDto(divisionRepository.getAllByParentId(id));

        return new DivisionOverviewDto(
                division.getId(),
                division.getName(),
                division.getOriginalName(),
                division.getDirector(),
                parent,
                children
        );
    }

    @Transactional(readOnly = true)
    public List<DivisionDto> listAllDivisions() {
        return divisionMapper.mapToDto(
                divisionRepository.findAll(Sort.by("name"))
        );
    }

    private void verifyParameterIsFilledIn(String value, String parameterName) {
        if (value == null || value.isEmpty()) throw DivisionNotFoundException.divisionNotFoundException(parameterName);
    }
}
