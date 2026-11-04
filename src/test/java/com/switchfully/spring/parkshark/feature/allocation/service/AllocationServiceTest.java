package com.switchfully.spring.parkshark.feature.allocation.service;

import com.switchfully.spring.parkshark.common.MutableClock;
import com.switchfully.spring.parkshark.common.domain.Address;
import com.switchfully.spring.parkshark.common.exception.ConflictException;
import com.switchfully.spring.parkshark.common.exception.NotFoundException;
import com.switchfully.spring.parkshark.feature.allocation.api.dto.CreatedAllocationDto;
import com.switchfully.spring.parkshark.feature.allocation.api.dto.AllocationDto;
import com.switchfully.spring.parkshark.feature.allocation.api.request.StartingParkingAllocationRequest;
import com.switchfully.spring.parkshark.feature.allocation.api.request.StopParkingAllocationRequest;
import com.switchfully.spring.parkshark.feature.allocation.domain.Allocation;
import com.switchfully.spring.parkshark.feature.allocation.domain.AllocationRepository;
import com.switchfully.spring.parkshark.feature.allocation.domain.AllocationStatus;
import com.switchfully.spring.parkshark.feature.allocation.exception.AllocationBadRequestException;
import com.switchfully.spring.parkshark.feature.allocation.exception.AllocationNotFoundException;
import com.switchfully.spring.parkshark.feature.allocation.api.AllocationMapper;
import com.switchfully.spring.parkshark.feature.division.domain.Division;
import com.switchfully.spring.parkshark.feature.member.domain.LicensePlate;
import com.switchfully.spring.parkshark.feature.member.domain.Member;
import com.switchfully.spring.parkshark.feature.member.domain.MemberRepository;
import com.switchfully.spring.parkshark.feature.member.domain.MembershipLevel;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.Category;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.ContactPerson;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.ParkingLot;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.ParkingLotRepository;
import com.switchfully.spring.parkshark.feature.security.domain.Credentials;
import com.switchfully.spring.parkshark.feature.security.domain.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AllocationServiceTest {

    private static final Address ADDRESS = new Address("Meir", "1", "2000", "Antwerp");
    private static final String MEMBER_PLATE = "1-ABC-123";
    private static final String OTHER_PLATE = "2-XYZ-999";
    private static final ZonedDateTime NOW = ZonedDateTime.of(2026, 1, 1, 9, 0, 0, 0, ZoneId.of("Europe/Brussels"));

    @Mock
    private AllocationRepository allocationRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private ParkingLotRepository parkingLotRepository;

    private AllocationService service;

    private final UUID credentialsId = UUID.randomUUID();
    private final UUID parkingLotId = UUID.randomUUID();
    private final UUID allocationId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new AllocationService(allocationRepository, memberRepository, parkingLotRepository,
                new AllocationMapper(), new MutableClock(NOW));
    }

    private static Member member(MembershipLevel level) {
        return new Member("John", "Doe", ADDRESS, "0258748582", new LicensePlate(MEMBER_PLATE, "BE"),
                LocalDate.of(2026, 1, 1), level, new Credentials("john@doe.com", "hash", Role.MEMBER));
    }

    private ParkingLot parkingLot(int capacity) {
        ParkingLot parkingLot = new ParkingLot("Meir Underground", Category.UNDERGROUND_BUILDING, capacity,
                new ContactPerson("Jane Doe", "+32 470 12 34 56", null, "jane@company.be", ADDRESS),
                ADDRESS, new Division("MyCompany", "MyOldCompany", "MyBoss"), new BigDecimal("2.50"));
        ReflectionTestUtils.setField(parkingLot, "id", parkingLotId);
        return parkingLot;
    }

    private StartingParkingAllocationRequest request(String licencePlate) {
        return new StartingParkingAllocationRequest(licencePlate, parkingLotId);
    }

    private void givenMemberAndParkingLot(Member member, ParkingLot parkingLot) {
        when(memberRepository.findByCredentialsId(credentialsId)).thenReturn(Optional.of(member));
        when(parkingLotRepository.findById(parkingLotId)).thenReturn(Optional.of(parkingLot));
    }

    @DisplayName("A member parking with their own plate gets an ongoing allocation starting now")
    @Test
    void givenMemberWithOwnPlateAndFreeSpot_whenCreateAllocation_thenAllocationIsSavedAndStartTimeComesFromClock() {
        Member member = member(MembershipLevel.BRONZE);
        ParkingLot parkingLot = parkingLot(2);
        givenMemberAndParkingLot(member, parkingLot);
        when(allocationRepository.countByParkingLotIdAndStatus(parkingLotId, AllocationStatus.ONGOING)).thenReturn(1);
        when(allocationRepository.saveAndFlush(any(Allocation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreatedAllocationDto dto = service.createAllocation(request(MEMBER_PLATE), credentialsId);

        ArgumentCaptor<Allocation> saved = ArgumentCaptor.forClass(Allocation.class);
        verify(allocationRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getMember()).isSameAs(member);
        assertThat(saved.getValue().getParkingLot()).isSameAs(parkingLot);
        assertThat(saved.getValue().getLicensePlateNumber()).isEqualTo(MEMBER_PLATE);
        assertThat(saved.getValue().getStatus()).isEqualTo(AllocationStatus.ONGOING);
        assertThat(saved.getValue().getEndTime()).isNull();
        assertThat(dto.startTime()).isEqualTo(LocalDateTime.of(2026, 1, 1, 9, 0));
    }

    @DisplayName("Only ongoing allocations count against the capacity")
    @Test
    void givenFreeSpot_whenCreateAllocation_thenOnlyOngoingAllocationsAreCounted() {
        givenMemberAndParkingLot(member(MembershipLevel.BRONZE), parkingLot(1));
        when(allocationRepository.countByParkingLotIdAndStatus(parkingLotId, AllocationStatus.ONGOING)).thenReturn(0);
        when(allocationRepository.saveAndFlush(any(Allocation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.createAllocation(request(MEMBER_PLATE), credentialsId);

        verify(allocationRepository).countByParkingLotIdAndStatus(parkingLotId, AllocationStatus.ONGOING);
    }

    @DisplayName("A gold member may park with another plate than their own")
    @Test
    void givenGoldMemberWithOtherPlate_whenCreateAllocation_thenAllocationIsSavedWithTheGivenPlate() {
        givenMemberAndParkingLot(member(MembershipLevel.GOLD), parkingLot(10));
        when(allocationRepository.countByParkingLotIdAndStatus(parkingLotId, AllocationStatus.ONGOING)).thenReturn(0);
        when(allocationRepository.saveAndFlush(any(Allocation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.createAllocation(request(OTHER_PLATE), credentialsId);

        ArgumentCaptor<Allocation> saved = ArgumentCaptor.forClass(Allocation.class);
        verify(allocationRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getLicensePlateNumber()).isEqualTo(OTHER_PLATE);
    }

    @DisplayName("Bronze and silver members cannot park with another plate than their own")
    @ParameterizedTest
    @EnumSource(value = MembershipLevel.class, names = {"BRONZE", "SILVER"})
    void givenBronzeOrSilverMemberWithOtherPlate_whenCreateAllocation_thenThrowsBadRequestAndSavesNothing(MembershipLevel level) {
        givenMemberAndParkingLot(member(level), parkingLot(10));

        assertThatThrownBy(() -> service.createAllocation(request(OTHER_PLATE), credentialsId))
                .isInstanceOf(AllocationBadRequestException.class)
                .hasMessageContaining(level.name());
        verify(allocationRepository, never()).saveAndFlush(any());
    }

    @DisplayName("An unknown member is rejected before anything else is looked up")
    @Test
    void givenUnknownMember_whenCreateAllocation_thenThrowsNotFoundAndSavesNothing() {
        when(memberRepository.findByCredentialsId(credentialsId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createAllocation(request(MEMBER_PLATE), credentialsId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(credentialsId.toString());
        verify(allocationRepository, never()).saveAndFlush(any());
    }

    @DisplayName("An unknown parking lot is rejected and the message names the parking lot")
    @Test
    void givenUnknownParkingLot_whenCreateAllocation_thenThrowsNotFoundNamingTheParkingLot() {
        when(memberRepository.findByCredentialsId(credentialsId)).thenReturn(Optional.of(member(MembershipLevel.BRONZE)));
        when(parkingLotRepository.findById(parkingLotId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.createAllocation(request(MEMBER_PLATE), credentialsId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining(parkingLotId.toString());
        verify(allocationRepository, never()).saveAndFlush(any());
    }

    @DisplayName("A full parking lot refuses new allocations with a conflict")
    @Test
    void givenFullParkingLot_whenCreateAllocation_thenThrowsConflictAndSavesNothing() {
        givenMemberAndParkingLot(member(MembershipLevel.BRONZE), parkingLot(2));
        when(allocationRepository.countByParkingLotIdAndStatus(parkingLotId, AllocationStatus.ONGOING)).thenReturn(2);

        assertThatThrownBy(() -> service.createAllocation(request(MEMBER_PLATE), credentialsId))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining(parkingLotId.toString())
                .hasMessageContaining("full");
        verify(allocationRepository, never()).saveAndFlush(any());
    }

    @DisplayName("A parking lot over capacity is also considered full")
    @Test
    void givenParkingLotOverCapacity_whenCreateAllocation_thenThrowsConflict() {
        givenMemberAndParkingLot(member(MembershipLevel.BRONZE), parkingLot(2));
        when(allocationRepository.countByParkingLotIdAndStatus(parkingLotId, AllocationStatus.ONGOING)).thenReturn(3);

        assertThatThrownBy(() -> service.createAllocation(request(MEMBER_PLATE), credentialsId))
                .isInstanceOf(ConflictException.class);
        verify(allocationRepository, never()).saveAndFlush(any());
    }

    @DisplayName("A database constraint violation on save is turned into a conflict")
    @Test
    void givenConstraintViolationOnSave_whenCreateAllocation_thenThrowsConflict() {
        givenMemberAndParkingLot(member(MembershipLevel.BRONZE), parkingLot(2));
        when(allocationRepository.countByParkingLotIdAndStatus(parkingLotId, AllocationStatus.ONGOING)).thenReturn(0);
        when(allocationRepository.saveAndFlush(any(Allocation.class)))
                .thenThrow(new DataIntegrityViolationException("constraint"));

        assertThatThrownBy(() -> service.createAllocation(request(MEMBER_PLATE), credentialsId))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("full");
    }

    @DisplayName("The member is resolved from the logged-in user, never from the request")
    @Test
    void givenLoggedInUser_whenCreateAllocation_thenMemberIsLookedUpByTheirCredentialsId() {
        givenMemberAndParkingLot(member(MembershipLevel.BRONZE), parkingLot(2));
        when(allocationRepository.countByParkingLotIdAndStatus(parkingLotId, AllocationStatus.ONGOING)).thenReturn(0);
        when(allocationRepository.saveAndFlush(any(Allocation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        service.createAllocation(request(MEMBER_PLATE), credentialsId);

        verify(memberRepository).findByCredentialsId(credentialsId);
        verify(memberRepository, never()).findById(any());
    }

    private static final LocalDateTime STARTED_AT = LocalDateTime.of(2026, 1, 1, 8, 0);
    private static final LocalDateTime STOPPED_AT = LocalDateTime.of(2026, 1, 1, 9, 0);

    private Member memberWithCredentialsId(UUID memberCredentialsId) {
        Member member = member(MembershipLevel.BRONZE);
        ReflectionTestUtils.setField(member, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(member.getCredentials(), "id", memberCredentialsId);
        return member;
    }

    private Allocation ongoingAllocationOf(Member member) {
        Allocation allocation = new Allocation(STARTED_AT, member, MEMBER_PLATE, parkingLot(2));
        ReflectionTestUtils.setField(allocation, "id", allocationId);
        return allocation;
    }

    private StopParkingAllocationRequest stopRequest() {
        return new StopParkingAllocationRequest(allocationId);
    }

    @DisplayName("The owner stops an ongoing allocation: status and stop time are set from the clock")
    @Test
    void givenOwnerAndOngoingAllocation_whenStopAllocation_thenAllocationIsStoppedNow() {
        Allocation allocation = ongoingAllocationOf(memberWithCredentialsId(credentialsId));
        when(allocationRepository.findById(allocationId)).thenReturn(Optional.of(allocation));
        when(allocationRepository.saveAndFlush(any(Allocation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AllocationDto dto = service.stopAllocation(stopRequest(), credentialsId);

        assertThat(allocation.getStatus()).isEqualTo(AllocationStatus.STOPPED);
        assertThat(allocation.getEndTime()).isEqualTo(STOPPED_AT);
        assertThat(dto.id()).isEqualTo(allocationId);
        assertThat(dto.status()).isEqualTo("STOPPED");
        assertThat(dto.licensePlateNumber()).isEqualTo(MEMBER_PLATE);
        assertThat(dto.startTime()).isEqualTo(STARTED_AT);
        assertThat(dto.stopTime()).isEqualTo(STOPPED_AT).isAfter(dto.startTime());
    }

    @DisplayName("An unknown allocation is rejected and the message names the allocation")
    @Test
    void givenUnknownAllocation_whenStopAllocation_thenThrowsNotFoundAndSavesNothing() {
        when(allocationRepository.findById(allocationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.stopAllocation(stopRequest(), credentialsId))
                .isInstanceOf(AllocationNotFoundException.class)
                .hasMessageContaining(allocationId.toString());
        verify(allocationRepository, never()).saveAndFlush(any());
    }

    @DisplayName("An allocation that is already stopped cannot be stopped again and keeps its stop time")
    @Test
    void givenAlreadyStoppedAllocation_whenStopAllocation_thenThrowsBadRequestAndStopTimeIsUnchanged() {
        Allocation allocation = ongoingAllocationOf(memberWithCredentialsId(credentialsId));
        LocalDateTime firstStopTime = LocalDateTime.of(2026, 1, 1, 8, 30);
        ReflectionTestUtils.setField(allocation, "status", AllocationStatus.STOPPED);
        ReflectionTestUtils.setField(allocation, "endTime", firstStopTime);
        when(allocationRepository.findById(allocationId)).thenReturn(Optional.of(allocation));

        assertThatThrownBy(() -> service.stopAllocation(stopRequest(), credentialsId))
                .isInstanceOf(AllocationBadRequestException.class);
        assertThat(allocation.getEndTime()).isEqualTo(firstStopTime);
        assertThat(allocation.getStatus()).isEqualTo(AllocationStatus.STOPPED);
        verify(allocationRepository, never()).saveAndFlush(any());
    }

    @DisplayName("A member cannot stop another member's allocation, which stays ongoing")
    @Test
    void givenAllocationOfAnotherMember_whenStopAllocation_thenThrowsBadRequestAndAllocationStaysOngoing() {
        Allocation allocation = ongoingAllocationOf(memberWithCredentialsId(UUID.randomUUID()));
        when(allocationRepository.findById(allocationId)).thenReturn(Optional.of(allocation));

        assertThatThrownBy(() -> service.stopAllocation(stopRequest(), credentialsId))
                .isInstanceOf(AllocationBadRequestException.class);
        assertThat(allocation.getStatus()).isEqualTo(AllocationStatus.ONGOING);
        assertThat(allocation.getEndTime()).isNull();
        verify(allocationRepository, never()).saveAndFlush(any());
    }

    @DisplayName("Ownership is checked on the credentials id, not on the member id")
    @Test
    void givenMemberIdInsteadOfCredentialsId_whenStopAllocation_thenThrowsBadRequest() {
        Member owner = memberWithCredentialsId(credentialsId);
        Allocation allocation = ongoingAllocationOf(owner);
        when(allocationRepository.findById(allocationId)).thenReturn(Optional.of(allocation));

        assertThatThrownBy(() -> service.stopAllocation(stopRequest(), owner.getId()))
                .isInstanceOf(AllocationBadRequestException.class);
        assertThat(allocation.getStatus()).isEqualTo(AllocationStatus.ONGOING);
        verify(allocationRepository, never()).saveAndFlush(any());
    }
}