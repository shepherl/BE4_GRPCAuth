package com.school21.shopapi.service;

import com.school21.shopapi.dto.AddressDto;
import com.school21.shopapi.dto.ClientDto;
import com.school21.shopapi.entity.Address;
import com.school21.shopapi.entity.Client;
import com.school21.shopapi.exception.ResourceNotFoundException;
import com.school21.shopapi.mapper.AddressMapper;
import com.school21.shopapi.mapper.ClientMapper;
import com.school21.shopapi.repository.AddressRepository;
import com.school21.shopapi.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {

    private final ClientRepository clientRepository;
    private final AddressRepository addressRepository;
    private final ClientMapper clientMapper;
    private final AddressMapper addressMapper;

    @Transactional
    public ClientDto createClient(ClientDto clientDto) {
        Client client = clientMapper.toEntity(clientDto);
        client.setRegistrationDate(LocalDate.now());
        if (client.getAddress() != null) {
            Address savedAddress = addressRepository.save(client.getAddress());
            client.setAddress(savedAddress);
        }
        return clientMapper.toDto(clientRepository.save(client));
    }

    @Transactional
    public void deleteClient(UUID id) {
        if (!clientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Client not found with id: " + id);
        }
        clientRepository.deleteById(id);
    }

    public List<ClientDto> getClientsByNameAndSurname(String name, String surname) {
        return clientRepository.findByClientNameAndClientSurname(name, surname).stream()
                .map(clientMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ClientDto> getAllClients(Integer limit, Integer offset) {
        if (limit != null && offset != null) {
            Page<Client> page = clientRepository.findAll(PageRequest.of(offset / limit, limit));
            return page.getContent().stream().map(clientMapper::toDto).collect(Collectors.toList());
        }
        return clientRepository.findAll().stream().map(clientMapper::toDto).collect(Collectors.toList());
    }

    @Transactional
    public ClientDto updateAddress(UUID id, AddressDto addressDto) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

        Address newAddress = addressMapper.toEntity(addressDto);
        Address savedAddress = addressRepository.save(newAddress);
        client.setAddress(savedAddress);
        return clientMapper.toDto(clientRepository.save(client));
    }
}
