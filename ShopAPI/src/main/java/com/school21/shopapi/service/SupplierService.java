package com.school21.shopapi.service;

import com.school21.shopapi.dto.AddressDto;
import com.school21.shopapi.dto.SupplierDto;
import com.school21.shopapi.entity.Address;
import com.school21.shopapi.entity.Supplier;
import com.school21.shopapi.exception.ResourceNotFoundException;
import com.school21.shopapi.mapper.AddressMapper;
import com.school21.shopapi.mapper.SupplierMapper;
import com.school21.shopapi.repository.AddressRepository;
import com.school21.shopapi.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final AddressRepository addressRepository;
    private final SupplierMapper supplierMapper;
    private final AddressMapper addressMapper;

    @Transactional
    public SupplierDto createSupplier(SupplierDto supplierDto) {
        Supplier supplier = supplierMapper.toEntity(supplierDto);
        if (supplier.getAddress() != null) {
            Address savedAddress = addressRepository.save(supplier.getAddress());
            supplier.setAddress(savedAddress);
        }
        return supplierMapper.toDto(supplierRepository.save(supplier));
    }

    @Transactional
    public SupplierDto updateAddress(UUID id, AddressDto addressDto) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));

        Address newAddress = addressMapper.toEntity(addressDto);
        Address savedAddress = addressRepository.save(newAddress);
        supplier.setAddress(savedAddress);
        return supplierMapper.toDto(supplierRepository.save(supplier));
    }

    @Transactional
    public void deleteSupplier(UUID id) {
        if (!supplierRepository.existsById(id)) {
            throw new ResourceNotFoundException("Supplier not found with id: " + id);
        }
        supplierRepository.deleteById(id);
    }

    public List<SupplierDto> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(supplierMapper::toDto)
                .collect(Collectors.toList());
    }

    public SupplierDto getSupplierById(UUID id) {
        return supplierRepository.findById(id)
                .map(supplierMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
    }
}
