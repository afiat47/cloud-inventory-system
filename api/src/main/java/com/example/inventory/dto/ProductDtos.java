package com.example.inventory.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;

public class ProductDtos {

    public record CreateProductRequest(
            @NotBlank @Size(max = 120) String name,
            @Size(max = 500) String description,
            @NotNull @DecimalMin("0.01") BigDecimal price,
            @NotNull @Min(0) Integer quantity) { }

    public record UpdateProductRequest(
            @NotBlank @Size(max = 120) String name,
            @Size(max = 500) String description,
            @NotNull @DecimalMin("0.01") BigDecimal price,
            @NotNull @Min(0) Integer quantity) { }

    public record ProductResponse(
            Long id, String name, String description,
            BigDecimal price, Integer quantity, Instant createdAt) { }
}
