package com.school21.shopapi.service;

import com.school21.shopapi.dto.ProductDto;
import com.school21.shopapi.entity.Product;
import com.school21.shopapi.entity.Supplier;
import com.school21.shopapi.exception.ResourceNotFoundException;
import com.school21.shopapi.mapper.ProductMapper;
import com.school21.shopapi.repository.ProductRepository;
import com.school21.shopapi.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final SupplierRepository supplierRepository;
    private final ProductMapper productMapper;

    @Transactional
    public ProductDto createProduct(ProductDto productDto) {
        Product product = productMapper.toEntity(productDto);
        product.setLastUpdateDate(LocalDate.now());

        if (productDto.getSupplierId() != null) {
            Supplier supplier = supplierRepository.findById(productDto.getSupplierId())
                    .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + productDto.getSupplierId()));
            product.setSupplier(supplier);
        }

        return productMapper.toDto(productRepository.save(product));
    }

    @Transactional
    public ProductDto reduceStock(UUID id, Integer amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (product.getAvailableStock() < amount) {
            throw new IllegalArgumentException("Not enough stock available");
        }

        product.setAvailableStock(product.getAvailableStock() - amount);
        return productMapper.toDto(productRepository.save(product));
    }

    public ProductDto getProductById(UUID id) {
        return productRepository.findById(id)
                .map(productMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    }

    public List<ProductDto> getAvailableProducts() {
        return productRepository.findByAvailableStockGreaterThan(0).stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteProduct(UUID id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }
}
