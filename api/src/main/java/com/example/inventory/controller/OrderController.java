package com.example.inventory.controller;

import com.example.inventory.dto.OrderDtos.*;
import com.example.inventory.entity.Order;
import com.example.inventory.entity.OrderItem;
import com.example.inventory.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) { this.service = service; }

    @Operation(summary = "Places an order across several products, in one transaction")
    @PostMapping
    public ResponseEntity<OrderResponse> place(@Valid @RequestBody CreateOrderRequest request) {
        Order order = service.place(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                          .path("/{id}").buildAndExpand(order.getId()).toUri();

        return ResponseEntity.created(location).body(toDto(order));
    }

    @Operation(summary = "Fetches an order with its lines")
    @GetMapping("/{id}")
    public OrderResponse getById(@PathVariable Long id) {
        return toDto(service.findByIdWithItems(id));
    }

    @Operation(summary = "Cancels a pending order and returns the stock")
    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@PathVariable Long id) {
        return toDto(service.cancel(id));
    }

    private static OrderResponse toDto(Order o) {
        List<OrderLineResponse> lines = o.getItems().stream()
                .map(OrderController::toLineDto)
                .toList();

        return new OrderResponse(o.getId(), o.getUser().getId(), o.getStatus().name(),
                                 o.getTotal(), o.getCreatedAt(), lines);
    }

    private static OrderLineResponse toLineDto(OrderItem i) {
        BigDecimal lineTotal = i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity()));
        return new OrderLineResponse(i.getProduct().getId(), i.getProduct().getName(),
                                     i.getQuantity(), i.getUnitPrice(), lineTotal);
    }
}
