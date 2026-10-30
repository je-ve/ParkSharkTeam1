package com.switchfully.spring.parkshark.feature.division.service;

import com.switchfully.spring.parkshark.common.exception.ConflictException;
import com.switchfully.spring.parkshark.feature.division.api.DivisionMapper;
import com.switchfully.spring.parkshark.feature.division.api.dto.DivisionDto;
import com.switchfully.spring.parkshark.feature.division.api.request.CreateDivisionRequest;
import com.switchfully.spring.parkshark.feature.division.domain.Division;
import com.switchfully.spring.parkshark.feature.division.domain.DivisionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

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

    @Mock
    private DivisionMapper mapper;

    @InjectMocks
    private DivisionService service;

    @DisplayName("Creating a division")
    @Test
    void givenRequest_whenCallingCreateDivision_thenADivisionObjectIsReturned() throws InstantiationException, IllegalAccessException {
        CreateDivisionRequest request = new CreateDivisionRequest("MyCompany", "MyOldCompany", "MyBoss");
        when(repository.existsByName("MyCompany")).thenReturn(false);
        when(repository.save(any(Division.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.mapToDto(any(Division.class))).thenReturn(new DivisionDto(UUID.randomUUID(), "MyCompany", "MyOldCompany", "MyBoss"));
        DivisionDto divisionDto = service.createDivision(request);

        assertThat(divisionDto.name()).isEqualTo(request.name());
        assertThat(divisionDto.originalName()).isEqualTo(request.originalName());
        assertThat(divisionDto.director()).isEqualTo(request.director());
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