package com.school21.shopapi.controller;

import com.school21.shopapi.dto.ProductDto;
import com.school21.shopapi.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Product API", description = "Operations related to products")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Add a new product")
    public ResponseEntity<ProductDto> addProduct(@Valid @RequestBody ProductDto productDto) {
        return new ResponseEntity<>(productService.createProduct(productDto), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/reduce-stock")
    @Operation(summary = "Reduce product stock quantity")
    public ResponseEntity<ProductDto> reduceProductStock(
            @PathVariable UUID id, @RequestParam Integer amount) {
        return ResponseEntity.ok(productService.reduceStock(id, amount));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductDto> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/available")
    @Operation(summary = "Get all available products (stock > 0)")
    public ResponseEntity<List<ProductDto>> getAvailableProducts() {
        return ResponseEntity.ok(productService.getAvailableProducts());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product by ID")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
