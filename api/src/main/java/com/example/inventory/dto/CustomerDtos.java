package com.example.inventory.dto;

import jakarta.validation.constraints.*;

import java.time.Instant;

public class CustomerDtos {

    public record CreateCustomerRequest(
            @NotBlank @Email @Size(max = 256) String email,
            @NotBlank @Size(max = 120) String fullName) { }

    public record CustomerResponse(
            Long id, String email, String fullName, Instant createdAt) { }
}
