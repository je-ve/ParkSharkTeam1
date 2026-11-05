package com.switchfully.spring.parkshark.feature.allocation.domain;

import com.switchfully.spring.parkshark.feature.member.domain.Member;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.ParkingLot;
import jakarta.persistence.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "allocation")
public class Allocation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    private AllocationStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    private String licensePlateNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    private ParkingLot parkingLot;

    public Allocation(LocalDateTime startTime, Member member, String licensePlateNumber, ParkingLot parkingLot) {
        this.startTime = startTime;
        this.endTime = null;
        this.member = member;
        this.licensePlateNumber = licensePlateNumber;
        this.parkingLot = parkingLot;
        this.status = AllocationStatus.ONGOING;
    }

    protected Allocation() {
    }

    public void markAsInvoiced() {
        if (status != AllocationStatus.STOPPED) {
            throw new IllegalStateException("Only a stopped allocation can be invoiced, allocation %s is %s"
                    .formatted(id, status));
        }
        this.status = AllocationStatus.INVOICED;
    }

    public Duration getParkedDuration() {
        if (endTime == null) {
            throw new IllegalStateException("Allocation %s is not stopped yet".formatted(id));
        }
        return Duration.between(startTime, endTime);
    }

    public UUID getId() {
        return id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public AllocationStatus getStatus() {
        return status;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public Member getMember() {
        return member;
    }

    public String getLicensePlateNumber() {
        return licensePlateNumber;
    }

    public ParkingLot getParkingLot() {
        return parkingLot;
    }
}
