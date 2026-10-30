package com.switchfully.spring.parkshark.feature.division.service;

import com.switchfully.spring.parkshark.common.exception.ConflictException;
import com.switchfully.spring.parkshark.feature.division.api.DivisionMapper;
import com.switchfully.spring.parkshark.feature.division.api.dto.DivisionDto;
import com.switchfully.spring.parkshark.feature.division.api.dto.DivisionOverviewDto;
import com.switchfully.spring.parkshark.feature.division.api.request.CreateDivisionRequest;
import com.switchfully.spring.parkshark.feature.division.domain.Division;
import com.switchfully.spring.parkshark.feature.division.domain.DivisionRepository;
import com.switchfully.spring.parkshark.feature.division.exception.DivisionNotFoundException;
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

    // ---------- createSubdivision ----------

    @DisplayName("Creating a subdivision of an existing division")
    @Test
    void givenAnExistingParent_whenCallingCreateSubdivision_thenTheSubdivisionIsSavedWithItsParent() {
        UUID parentId = UUID.randomUUID();
        Division parent = new Division("Parent", "OldParent", "ParentBoss");
        CreateDivisionRequest request = new CreateDivisionRequest("Sub", "OldSub", "SubBoss");
        when(repository.existsById(parentId)).thenReturn(true);
        when(repository.existsByName("Sub")).thenReturn(false);
        when(repository.saveAndFlush(any(Division.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(repository.getById(parentId)).thenReturn(parent);

        DivisionOverviewDto overview = service.createSubdivision(request, parentId);

        ArgumentCaptor<Division> saved = ArgumentCaptor.forClass(Division.class);
        verify(repository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getName()).isEqualTo("Sub");
        assertThat(saved.getValue().getOriginalName()).isEqualTo("OldSub");
        assertThat(saved.getValue().getDirector()).isEqualTo("SubBoss");
        assertThat(saved.getValue().getParentId()).isEqualTo(parentId);
        assertThat(overview.name()).isEqualTo("Sub");
        assertThat(overview.parentDivision().name()).isEqualTo("Parent");
        assertThat(overview.childrenDivisions()).isEmpty();
    }

    @DisplayName("Creating a subdivision of an unknown division")
    @Test
    void givenAnUnknownParent_whenCallingCreateSubdivision_thenANotFoundExceptionIsThrown() {
        UUID parentId = UUID.randomUUID();
        CreateDivisionRequest request = new CreateDivisionRequest("Sub", "OldSub", "SubBoss");
        when(repository.existsById(parentId)).thenReturn(false);

        assertThatThrownBy(() -> service.createSubdivision(request, parentId))
                .isInstanceOf(DivisionNotFoundException.class)
                .hasMessage("Division %s not found.".formatted(parentId));

        verify(repository, never()).saveAndFlush(any());
    }

    @DisplayName("Creating a subdivision with an existing name")
    @Test
    void givenDuplicateName_whenCallingCreateSubdivision_thenAConflictIsThrown() {
        UUID parentId = UUID.randomUUID();
        CreateDivisionRequest request = new CreateDivisionRequest("Sub", "OldSub", "SubBoss");
        when(repository.existsById(parentId)).thenReturn(true);
        when(repository.existsByName("Sub")).thenReturn(true);

        assertThatThrownBy(() -> service.createSubdivision(request, parentId))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Division Sub already exists.");

        verify(repository, never()).saveAndFlush(any());
    }

    @DisplayName("Creating a subdivision while another request saves the same name")
    @Test
    void givenTheSameNameSavedConcurrently_whenCallingCreateSubdivision_thenAConflictIsThrown() {
        UUID parentId = UUID.randomUUID();
        CreateDivisionRequest request = new CreateDivisionRequest("Sub", "OldSub", "SubBoss");
        when(repository.existsById(parentId)).thenReturn(true);
        when(repository.existsByName("Sub")).thenReturn(false);
        when(repository.saveAndFlush(any(Division.class))).thenThrow(new DataIntegrityViolationException("unique constraint"));

        assertThatThrownBy(() -> service.createSubdivision(request, parentId))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Division Sub already exists.");
    }

    // ---------- getDivisionOverview ----------

    @DisplayName("Getting an unknown division")
    @Test
    void givenAnUnknownId_whenCallingGetDivisionOverview_thenANotFoundExceptionIsThrown() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> service.getDivisionOverview(id))
                .isInstanceOf(DivisionNotFoundException.class)
                .hasMessage("Division %s not found.".formatted(id));
    }

    @DisplayName("Getting a top-level division: no parent, its subdivisions as children")
    @Test
    void givenATopLevelDivision_whenCallingGetDivisionOverview_thenParentIsNullAndChildrenAreReturned() {
        UUID id = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);
        when(repository.getById(id)).thenReturn(new Division("Top", "OldTop", "TopBoss"));
        when(repository.getAllByParentId(id)).thenReturn(List.of(
                new Division("ChildA", "OldChildA", "BossA", id),
                new Division("ChildB", "OldChildB", "BossB", id)));

        DivisionOverviewDto overview = service.getDivisionOverview(id);

        assertThat(overview.name()).isEqualTo("Top");
        assertThat(overview.originalName()).isEqualTo("OldTop");
        assertThat(overview.director()).isEqualTo("TopBoss");
        assertThat(overview.parentDivision()).isNull();
        assertThat(overview.childrenDivisions()).extracting(DivisionDto::name).containsExactly("ChildA", "ChildB");
    }

    @DisplayName("Getting a subdivision: its parent is returned")
    @Test
    void givenASubdivision_whenCallingGetDivisionOverview_thenItsParentIsReturned() {
        UUID id = UUID.randomUUID();
        UUID parentId = UUID.randomUUID();
        when(repository.existsById(id)).thenReturn(true);
        when(repository.getById(id)).thenReturn(new Division("Sub", "OldSub", "SubBoss", parentId));
        when(repository.existsById(parentId)).thenReturn(true);
        when(repository.getById(parentId)).thenReturn(new Division("Parent", "OldParent", "ParentBoss"));
        when(repository.getAllByParentId(id)).thenReturn(List.of());

        DivisionOverviewDto overview = service.getDivisionOverview(id);

        assertThat(overview.name()).isEqualTo("Sub");
        assertThat(overview.parentDivision().name()).isEqualTo("Parent");
        assertThat(overview.childrenDivisions()).isEmpty();
    }
}
