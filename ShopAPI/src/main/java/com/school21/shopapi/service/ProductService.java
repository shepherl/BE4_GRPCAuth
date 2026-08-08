package com.school21.shopapi.service;

import com.school21.shopapi.dto.ProductDto;
import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductDto createProduct(ProductDto productDto);
    ProductDto reduceStock(UUID id, Integer amount);
    ProductDto getProductById(UUID id);
    List<ProductDto> getAvailableProducts();
    void deleteProduct(UUID id);
}
