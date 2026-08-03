package com.school21.shopapi.mapper;

import com.school21.shopapi.dto.AddressDto;
import com.school21.shopapi.entity.Address;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AddressMapper {
    AddressDto toDto(Address address);
    Address toEntity(AddressDto addressDto);
}
