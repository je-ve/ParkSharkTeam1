package com.switchfully.spring.parkshark.feature.division.service;

import com.switchfully.spring.parkshark.common.exception.ConflictException;
import com.switchfully.spring.parkshark.feature.division.api.DivisionMapper;
import com.switchfully.spring.parkshark.feature.division.api.dto.DivisionDto;
import com.switchfully.spring.parkshark.feature.division.api.request.CreateDivisionRequest;
import com.switchfully.spring.parkshark.feature.division.domain.Division;
import com.switchfully.spring.parkshark.feature.division.domain.DivisionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DivisionServiceTest {

    @Mock
    private DivisionRepository repository;

    private DivisionService service;

    @BeforeEach
    void setUp() {
        service = new DivisionService(repository, new DivisionMapper());
    }

    @DisplayName("Creating a division")
    @Test
    void givenRequest_whenCallingCreateDivision_thenTheDivisionIsSavedAndReturned() {
        CreateDivisionRequest request = new CreateDivisionRequest("MyCompany", "MyOldCompany", "MyBoss");
        when(repository.existsByName("MyCompany")).thenReturn(false);
        when(repository.saveAndFlush(any(Division.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DivisionDto divisionDto = service.createDivision(request);

        ArgumentCaptor<Division> saved = ArgumentCaptor.forClass(Division.class);
        verify(repository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getName()).isEqualTo("MyCompany");
        assertThat(saved.getValue().getOriginalName()).isEqualTo("MyOldCompany");
        assertThat(saved.getValue().getDirector()).isEqualTo("MyBoss");
        assertThat(divisionDto.name()).isEqualTo("MyCompany");
        assertThat(divisionDto.originalName()).isEqualTo("MyOldCompany");
        assertThat(divisionDto.director()).isEqualTo("MyBoss");
    }

    @DisplayName("Creating a division with an existing name")
    @Test
    void givenDuplicateNameDivision_whenCallingCreateDivision_thenAnExceptionIsThrown() {
        CreateDivisionRequest request = new CreateDivisionRequest("MyCompany", "MyOldCompany", "MyBoss");
        when(repository.existsByName("MyCompany")).thenReturn(true);

        assertThatThrownBy(() -> service.createDivision(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Division MyCompany already exists.");

        verify(repository, never()).saveAndFlush(any());
    }

    @DisplayName("Creating a division while another request saves the same name")
    @Test
    void givenTheSameNameSavedConcurrently_whenCallingCreateDivision_thenAConflictIsThrown() {
        CreateDivisionRequest request = new CreateDivisionRequest("MyCompany", "MyOldCompany", "MyBoss");
        when(repository.existsByName("MyCompany")).thenReturn(false);
        when(repository.saveAndFlush(any(Division.class))).thenThrow(new DataIntegrityViolationException("unique constraint"));

        assertThatThrownBy(() -> service.createDivision(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Division MyCompany already exists.");
    }

    @DisplayName("Listing all divisions")
    @Test
    void givenSavedDivisions_whenCallingListAllDivisions_thenReturnsThemSortedByName() {
        when(repository.findAll(Sort.by("name"))).thenReturn(List.of(
                new Division("Alpha", "OldAlpha", "Alice"),
                new Division("Beta", "OldBeta", "Bob")));

        List<DivisionDto> divisions = service.listAllDivisions();

        assertThat(divisions).extracting(DivisionDto::name).containsExactly("Alpha", "Beta");
        verify(repository).findAll(Sort.by("name"));
    }
}
