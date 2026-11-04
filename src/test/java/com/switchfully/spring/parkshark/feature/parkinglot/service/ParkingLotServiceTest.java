package com.switchfully.spring.parkshark.feature.parkinglot.service;

import com.switchfully.spring.parkshark.common.api.AddressMapper;
import com.switchfully.spring.parkshark.common.api.dto.AddressDto;
import com.switchfully.spring.parkshark.common.domain.Address;
import com.switchfully.spring.parkshark.feature.division.api.DivisionMapper;
import com.switchfully.spring.parkshark.feature.division.domain.Division;
import com.switchfully.spring.parkshark.feature.division.service.DivisionService;
import com.switchfully.spring.parkshark.feature.parkinglot.api.ContactPersonMapper;
import com.switchfully.spring.parkshark.feature.parkinglot.api.ParkingLotMapper;
import com.switchfully.spring.parkshark.feature.parkinglot.api.request.CreateContactPersonRequest;
import com.switchfully.spring.parkshark.feature.parkinglot.api.request.CreateParkingLotRequest;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.Category;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.ContactPerson;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.ContactPersonRepository;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.ParkingLot;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.ParkingLotRepository;
import com.switchfully.spring.parkshark.feature.parkinglot.exception.ContactPersonConflictException;
import com.switchfully.spring.parkshark.feature.parkinglot.exception.ParkingLotNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ParkingLotServiceTest {

    private static final AddressDto ADDRESS = new AddressDto("Meir", "1", "2000", "Antwerp");

    @Mock
    private ParkingLotRepository parkingLotRepository;
    @Mock
    private ContactPersonRepository contactPersonRepository;
    @Mock
    private DivisionService divisionService;

    private ParkingLotService service;
    private final Division division = new Division("MyCompany", "MyOldCompany", "MyBoss");

    @BeforeEach
    void setUp() {
        AddressMapper addressMapper = new AddressMapper();
        DivisionMapper divisionMapper = new DivisionMapper();
        ContactPersonMapper contactPersonMapper = new ContactPersonMapper(addressMapper);
        service = new ParkingLotService(parkingLotRepository, contactPersonRepository, divisionService,
                new ParkingLotMapper(addressMapper, contactPersonMapper, divisionMapper), contactPersonMapper);
    }

    private static CreateContactPersonRequest newContact(String email) {
        return new CreateContactPersonRequest("Jane Doe", "+32 470 12 34 56", null, email, ADDRESS);
    }

    private static CreateParkingLotRequest request(UUID contactPersonId, CreateContactPersonRequest contactPerson) {
        return new CreateParkingLotRequest("Meir Underground", Category.UNDERGROUND_BUILDING, 250, ADDRESS,
                new BigDecimal("2.50"), contactPersonId, contactPerson, UUID.randomUUID());
    }

    private static ContactPerson existingContact() {
        return new ContactPerson("Jane Doe", "+32 470 12 34 56", null, "jane@company.be",
                new Address("Meir", "1", "2000", "Antwerp"));
    }

    @DisplayName("Reusing an existing contact person never saves a new one")
    @Test
    void givenExistingContactPersonId_whenCreateParkingLot_thenNoContactPersonIsSaved() {
        UUID contactId = UUID.randomUUID();
        ContactPerson contactPerson = existingContact();
        when(divisionService.getDivisionById(any())).thenReturn(division);
        when(contactPersonRepository.findById(contactId)).thenReturn(Optional.of(contactPerson));
        when(parkingLotRepository.save(any(ParkingLot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var dto = service.createParkingLot(request(contactId, null));

        verify(contactPersonRepository, never()).save(any());
        verify(contactPersonRepository, never()).saveAndFlush(any());
        assertThat(dto.contactPerson().email()).isEqualTo("jane@company.be");
    }

    @DisplayName("A new contact person is saved with a normalized email")
    @Test
    void givenNewContactPerson_whenCreateParkingLot_thenContactIsSavedWithNormalizedEmail() {
        when(divisionService.getDivisionById(any())).thenReturn(division);
        when(contactPersonRepository.findByEmail("jane@company.be")).thenReturn(Optional.empty());
        when(contactPersonRepository.saveAndFlush(any(ContactPerson.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(parkingLotRepository.save(any(ParkingLot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var dto = service.createParkingLot(request(null, newContact("  Jane@Company.BE ")));

        assertThat(dto.contactPerson().email()).isEqualTo("jane@company.be");
        assertThat(dto.name()).isEqualTo("Meir Underground");
    }

    @DisplayName("A new contact person with an existing email is rejected")
    @Test
    void givenNewContactWithExistingEmail_whenCreateParkingLot_thenThrowsConflictAndSavesNothing() {
        when(divisionService.getDivisionById(any())).thenReturn(division);
        when(contactPersonRepository.findByEmail("jane@company.be")).thenReturn(Optional.of(existingContact()));

        assertThatThrownBy(() -> service.createParkingLot(request(null, newContact("jane@company.be"))))
                .isInstanceOf(ContactPersonConflictException.class)
                .hasMessageContaining("jane@company.be");
        verify(contactPersonRepository, never()).saveAndFlush(any());
        verify(parkingLotRepository, never()).save(any());
    }

    @DisplayName("A concurrent insert with the same email is turned into a conflict")
    @Test
    void givenUniqueConstraintViolation_whenCreateParkingLot_thenThrowsConflict() {
        when(divisionService.getDivisionById(any())).thenReturn(division);
        when(contactPersonRepository.findByEmail("jane@company.be")).thenReturn(Optional.empty());
        when(contactPersonRepository.saveAndFlush(any(ContactPerson.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> service.createParkingLot(request(null, newContact("jane@company.be"))))
                .isInstanceOf(ContactPersonConflictException.class);
        verify(parkingLotRepository, never()).save(any());
    }

    @DisplayName("No parking lots gives an empty overview")
    @Test
    void givenNoParkingLots_whenGetOverview_thenReturnsEmptyList() {
        when(parkingLotRepository.findAllWithContactPerson()).thenReturn(List.of());

        assertThat(service.getParkingLotsOverview()).isEmpty();
    }

    @DisplayName("The overview only maps id, name, capacity and the contact details")
    @Test
    void givenParkingLot_whenGetOverview_thenMapsTheOverviewFields() {
        ParkingLot parkingLot = new ParkingLot("Meir Underground", Category.UNDERGROUND_BUILDING, 250,
                existingContact(), new Address("Meir", "1", "2000", "Antwerp"), division, new BigDecimal("2.50"));
        when(parkingLotRepository.findAllWithContactPerson()).thenReturn(List.of(parkingLot));

        var overview = service.getParkingLotsOverview();

        assertThat(overview).singleElement().satisfies(dto -> {
            assertThat(dto.name()).isEqualTo("Meir Underground");
            assertThat(dto.capacity()).isEqualTo(250);
            assertThat(dto.contactEmail()).isEqualTo("jane@company.be");
            assertThat(dto.contactMobilePhone()).isEqualTo("+32 470 12 34 56");
            assertThat(dto.contactTelephoneNumber()).isNull();
        });
    }

    @DisplayName("Getting an unknown parking lot throws not found")
    @Test
    void givenUnknownId_whenGetParkingLot_thenThrowsNotFound() {
        UUID id = UUID.randomUUID();
        when(parkingLotRepository.findWithContactAndDivisionById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getParkingLotWithContactAndDivisionById(id))
                .isInstanceOf(ParkingLotNotFoundException.class)
                .hasMessageContaining(id.toString());
    }

    @DisplayName("Getting a parking lot includes its division")
    @Test
    void givenExistingId_whenGetParkingLot_thenReturnsItWithItsDivision() {
        UUID id = UUID.randomUUID();
        ParkingLot parkingLot = new ParkingLot("Meir Underground", Category.UNDERGROUND_BUILDING, 250,
                existingContact(), new Address("Meir", "1", "2000", "Antwerp"), division, new BigDecimal("2.50"));
        when(parkingLotRepository.findWithContactAndDivisionById(id)).thenReturn(Optional.of(parkingLot));

        var dto = service.getParkingLotWithContactAndDivisionById(id);

        assertThat(dto.name()).isEqualTo("Meir Underground");
        assertThat(dto.contactPerson().email()).isEqualTo("jane@company.be");
        assertThat(dto.division().name()).isEqualTo("MyCompany");
        assertThat(dto.division().director()).isEqualTo("MyBoss");
    }
}
