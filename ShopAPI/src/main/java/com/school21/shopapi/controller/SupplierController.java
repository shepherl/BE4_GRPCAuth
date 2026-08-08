package com.school21.shopapi.controller;

import com.school21.shopapi.dto.AddressDto;
import com.school21.shopapi.dto.SupplierDto;
import com.school21.shopapi.security.RequiresAuth;
import com.school21.shopapi.service.SupplierService;
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
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
@Tag(name = "Supplier API", description = "Operations related to suppliers")
@RequiresAuth
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    @Operation(summary = "Add a new supplier")
    public ResponseEntity<SupplierDto> addSupplier(@Valid @RequestBody SupplierDto supplierDto) {
        return new ResponseEntity<>(supplierService.createSupplier(supplierDto), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/address")
    @Operation(summary = "Change supplier's address")
    public ResponseEntity<SupplierDto> updateSupplierAddress(
            @PathVariable UUID id, @Valid @RequestBody AddressDto addressDto) {
        return ResponseEntity.ok(supplierService.updateAddress(id, addressDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete supplier by ID")
    @RequiresAuth(roles = {"ADMIN"})
    public ResponseEntity<Void> deleteSupplier(@PathVariable UUID id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get all suppliers")
    public ResponseEntity<List<SupplierDto>> getAllSuppliers() {
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get supplier by ID")
    public ResponseEntity<SupplierDto> getSupplierById(@PathVariable UUID id) {
        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }
}
