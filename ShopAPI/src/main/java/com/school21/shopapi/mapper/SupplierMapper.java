package com.school21.shopapi.mapper;

import com.school21.shopapi.dto.SupplierDto;
import com.school21.shopapi.entity.Supplier;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {AddressMapper.class})
public interface SupplierMapper {
    SupplierDto toDto(Supplier supplier);
    Supplier toEntity(SupplierDto supplierDto);
}
