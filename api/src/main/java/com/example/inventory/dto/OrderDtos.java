package com.example.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class OrderDtos {

    public record OrderLineRequest(
            @NotNull Long productId,
            @NotNull @Min(1) @Max(1000) Integer quantity) { }

    public record CreateOrderRequest(
            @NotNull Long customerId,
            @NotEmpty(message = "An order must contain at least one line")
            @Size(max = 50)
            @Valid List<OrderLineRequest> lines) { }

    public record OrderLineResponse(
            Long productId, String productName, int quantity,
            BigDecimal unitPrice, BigDecimal lineTotal) { }

    public record OrderResponse(
            Long id, Long customerId, String status,
            BigDecimal total, Instant createdAt,
            List<OrderLineResponse> lines) { }
}
