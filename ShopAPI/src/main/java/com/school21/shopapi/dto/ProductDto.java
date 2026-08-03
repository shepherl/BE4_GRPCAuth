package com.school21.shopapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class ProductDto {
    @Schema(description = "Product ID", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID id;

    @NotBlank(message = "Name is mandatory")
    @Schema(description = "Product Name", example = "Laptop")
    private String name;

    @NotBlank(message = "Category is mandatory")
    @Schema(description = "Product Category", example = "Electronics")
    private String category;

    @NotNull(message = "Price is mandatory")
    @Min(value = 0, message = "Price must be positive")
    @Schema(description = "Product Price", example = "999.99")
    private BigDecimal price;

    @NotNull(message = "Available stock is mandatory")
    @Min(value = 0, message = "Stock cannot be negative")
    @Schema(description = "Available Stock", example = "10")
    private Integer availableStock;

    @Schema(description = "Last update date", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDate lastUpdateDate;

    @Schema(description = "Supplier ID", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID supplierId;

    @Schema(description = "Image ID", accessMode = Schema.AccessMode.READ_ONLY)
    private UUID imageId;
}
