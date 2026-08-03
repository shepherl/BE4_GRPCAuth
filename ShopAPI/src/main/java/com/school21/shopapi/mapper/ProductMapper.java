package com.school21.shopapi.mapper;

import com.school21.shopapi.dto.ProductDto;
import com.school21.shopapi.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "supplier.id", target = "supplierId")
    @Mapping(source = "image.id", target = "imageId")
    ProductDto toDto(Product product);

    @Mapping(target = "supplier", ignore = true)
    @Mapping(target = "image", ignore = true)
    Product toEntity(ProductDto productDto);
}
