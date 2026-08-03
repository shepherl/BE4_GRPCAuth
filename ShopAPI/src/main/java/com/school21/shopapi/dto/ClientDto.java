package com.school21.shopapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class ClientDto {
    @Schema(description = "Client ID", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @NotBlank(message = "Name is mandatory")
    @Schema(description = "Client Name", example = "Ivan")
    private String clientName;

    @NotBlank(message = "Surname is mandatory")
    @Schema(description = "Client Surname", example = "Ivanov")
    private String clientSurname;

    @NotNull(message = "Birthday is mandatory")
    @Schema(description = "Client Birthday", example = "1990-01-01")
    private LocalDate birthday;

    @NotBlank(message = "Gender is mandatory")
    @Schema(description = "Client Gender", example = "Male")
    private String gender;

    @Schema(description = "Registration Date", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDate registrationDate;

    @Schema(description = "Client Address")
    private AddressDto address;
}
