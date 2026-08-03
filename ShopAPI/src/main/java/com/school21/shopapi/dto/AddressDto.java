package com.school21.shopapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class AddressDto {
    @Schema(description = "Address ID", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @NotBlank(message = "Country is mandatory")
    @Schema(description = "Country", example = "Russia")
    private String country;

    @NotBlank(message = "City is mandatory")
    @Schema(description = "City", example = "Moscow")
    private String city;

    @NotBlank(message = "Street is mandatory")
    @Schema(description = "Street", example = "Tverskaya, 1")
    private String street;
}
