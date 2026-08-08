package com.school21.shopapi.service;

import com.school21.shopapi.dto.AddressDto;
import com.school21.shopapi.dto.SupplierDto;
import java.util.List;
import java.util.UUID;

public interface SupplierService {
    SupplierDto createSupplier(SupplierDto supplierDto);
    SupplierDto updateAddress(UUID id, AddressDto addressDto);
    void deleteSupplier(UUID id);
    List<SupplierDto> getAllSuppliers();
    SupplierDto getSupplierById(UUID id);
}
