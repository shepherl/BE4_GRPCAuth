package com.school21.shopapi.controller;

import com.school21.shopapi.dto.AddressDto;
import com.school21.shopapi.dto.ClientDto;
import com.school21.shopapi.service.ClientService;
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
@RequestMapping("/api/v1/clients")
@RequiredArgsConstructor
@Tag(name = "Client API", description = "Operations related to clients")
public class ClientController {

    private final ClientService clientService;

    @PostMapping
    @Operation(summary = "Add a new client")
    public ResponseEntity<ClientDto> addClient(@Valid @RequestBody ClientDto clientDto) {
        return new ResponseEntity<>(clientService.createClient(clientDto), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete client by ID")
    public ResponseEntity<Void> deleteClient(@PathVariable UUID id) {
        clientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(summary = "Get clients by name and surname")
    public ResponseEntity<List<ClientDto>> getClientsByNameAndSurname(
            @RequestParam String name, @RequestParam String surname) {
        return ResponseEntity.ok(clientService.getClientsByNameAndSurname(name, surname));
    }

    @GetMapping
    @Operation(summary = "Get all clients with optional pagination")
    public ResponseEntity<List<ClientDto>> getAllClients(
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset) {
        return ResponseEntity.ok(clientService.getAllClients(limit, offset));
    }

    @PatchMapping("/{id}/address")
    @Operation(summary = "Change client's address")
    public ResponseEntity<ClientDto> updateClientAddress(
            @PathVariable UUID id, @Valid @RequestBody AddressDto addressDto) {
        return ResponseEntity.ok(clientService.updateAddress(id, addressDto));
    }
}
