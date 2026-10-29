package com.switchfully.spring.parkshark.feature.division.api;

import com.switchfully.spring.parkshark.feature.division.api.dto.DivisionDto;
import com.switchfully.spring.parkshark.feature.division.api.request.CreateDivisionRequest;
import com.switchfully.spring.parkshark.feature.division.domain.Division;
import com.switchfully.spring.parkshark.feature.division.service.DivisionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DivisionControllerTest {

    @Mock
    private DivisionMapper divisionMapper;
    @Mock
    private DivisionService divisionService;
    @InjectMocks
    private DivisionController controller;

    @DisplayName("Creating a division returns the mapped dto")
    @Test
    void givenValidRequest_whenCreateDivision_thenCorrectDtoIsReturned() {
        CreateDivisionRequest request = new CreateDivisionRequest("MyCompany", "MyOldCompany", "MyBoss");
        Division division = new Division("MyCompany", "MyOldCompany", "MyBoss");
        DivisionDto expected = new DivisionDto(UUID.randomUUID(), "MyCompany", "MyOldCompany", "MyBoss");

        when(divisionService.createDivision(request)).thenReturn(division);
        when(divisionMapper.mapToDto(division)).thenReturn(expected);

        DivisionDto result = controller.createDivision(request);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expected);
        assertThat(result.id()).isNotNull();
        System.out.println(result.id());
        assertThat(result.name()).isEqualTo(request.name());
        assertThat(result.originalName()).isEqualTo(request.originalName());
        assertThat(result.director()).isEqualTo(request.director());
        verify(divisionService).createDivision(request);
        verify(divisionMapper).mapToDto(division);
    }
}