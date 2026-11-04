package com.switchfully.spring.parkshark.feature.parkinglot.api;

import com.switchfully.spring.parkshark.common.api.AddressMapper;
import com.switchfully.spring.parkshark.feature.parkinglot.api.dto.ContactPersonDto;
import com.switchfully.spring.parkshark.feature.parkinglot.api.request.CreateContactPersonRequest;
import com.switchfully.spring.parkshark.feature.parkinglot.domain.ContactPerson;
import org.springframework.stereotype.Component;

@Component
public class ContactPersonMapper {

    private final AddressMapper addressMapper;

    public ContactPersonMapper(AddressMapper addressMapper) {
        this.addressMapper = addressMapper;
    }

    public ContactPerson toEntity(CreateContactPersonRequest request) {
        return new ContactPerson(
                request.name(),
                request.mobilePhone(),
                request.telephoneNumber(),
                request.email(),
                addressMapper.toEntity(request.address()));
    }

    public ContactPersonDto toDto(ContactPerson contactPerson) {
        return new ContactPersonDto(
                contactPerson.getId(),
                contactPerson.getName(),
                contactPerson.getMobilePhone(),
                contactPerson.getTelephoneNumber(),
                contactPerson.getEmail(),
                addressMapper.toDto(contactPerson.getAddress()));
    }
}
