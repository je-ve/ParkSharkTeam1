package com.switchfully.spring.parkshark.feature.member.api;

import com.switchfully.spring.parkshark.feature.member.api.dto.LicensePlateDto;
import com.switchfully.spring.parkshark.feature.member.domain.LicensePlate;
import org.springframework.stereotype.Component;

@Component
public class LicensePlateMapper {

    public LicensePlate toEntity(LicensePlateDto dto) {
        return new LicensePlate(dto.number(), dto.issuingCountry());
    }

    public LicensePlateDto toDto(LicensePlate licensePlate) {
        return new LicensePlateDto(licensePlate.getNumber(), licensePlate.getIssuingCountry());
    }
}
