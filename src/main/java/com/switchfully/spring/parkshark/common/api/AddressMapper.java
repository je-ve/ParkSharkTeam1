package com.switchfully.spring.parkshark.common.api;

import com.switchfully.spring.parkshark.common.api.dto.AddressDto;
import com.switchfully.spring.parkshark.common.domain.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public Address toEntity(AddressDto dto) {
        return new Address(dto.streetName(), dto.streetNumber(), dto.postalCode(), dto.city());
    }

    public AddressDto toDto(Address address) {
        return new AddressDto(address.getStreetName(), address.getStreetNumber(), address.getPostalCode(), address.getCity());
    }
}
