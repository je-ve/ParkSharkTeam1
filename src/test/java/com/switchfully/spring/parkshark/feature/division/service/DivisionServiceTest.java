package com.switchfully.spring.parkshark.feature.division.service;

import com.switchfully.spring.parkshark.common.exception.ConflictException;
import com.switchfully.spring.parkshark.feature.division.api.request.CreateDivisionRequest;
import com.switchfully.spring.parkshark.feature.division.domain.Division;
import com.switchfully.spring.parkshark.feature.division.domain.DivisionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @InjectMocks
    private DivisionService service;

    @DisplayName("Creating a division")
    @Test
    void givenRequest_whenCallingCreateDivision_thenADivisionObjectIsReturned() {
        CreateDivisionRequest request = new CreateDivisionRequest("MyCompany", "MyOldCompany", "MyBoss");
        when(repository.existsByName("MyCompany")).thenReturn(false);
        when(repository.save(any(Division.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Division division = service.createDivision(request);

        assertThat(division.getName()).isEqualTo(request.name());
        assertThat(division.getOriginalName()).isEqualTo(request.originalName());
        assertThat(division.getDirector()).isEqualTo(request.director());
        verify(repository).save(any(Division.class));
    }

    @DisplayName("Creating conflict")
    @Test
    void givenDuplicateNameDivision_whenCallingCreateDivision_thenAnExceptionIsThrown() {
        CreateDivisionRequest request = new CreateDivisionRequest("MyCompany", "MyOldCompany", "MyBoss");
        when(repository.existsByName("MyCompany")).thenReturn(true);

        assertThatThrownBy(() -> service.createDivision(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Division MyCompany already exists.");

        verify(repository, never()).save(any());
    }
}