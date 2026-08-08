package com.school21.shopapi.service;

import com.school21.shopapi.dto.AddressDto;
import com.school21.shopapi.dto.ClientDto;
import java.util.List;
import java.util.UUID;

public interface ClientService {
    ClientDto createClient(ClientDto clientDto);
    void deleteClient(UUID id);
    List<ClientDto> getClientsByNameAndSurname(String name, String surname);
    List<ClientDto> getAllClients(Integer limit, Integer offset);
    ClientDto updateAddress(UUID id, AddressDto addressDto);
}
