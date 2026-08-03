package com.school21.shopapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class SupplierDto {
    @Schema(description = "Supplier ID", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @NotBlank(message = "Name is mandatory")
    @Schema(description = "Supplier Name", example = "Tech Corp")
    private String name;

    @NotBlank(message = "Phone number is mandatory")
    @Schema(description = "Phone Number", example = "+1234567890")
    private String phoneNumber;

    @Schema(description = "Supplier Address")
    private AddressDto address;
}
