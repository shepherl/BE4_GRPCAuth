package com.school21.shopapi.mapper;

import com.school21.shopapi.dto.ClientDto;
import com.school21.shopapi.entity.Client;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {AddressMapper.class})
public interface ClientMapper {
    ClientDto toDto(Client client);
    Client toEntity(ClientDto clientDto);
}
