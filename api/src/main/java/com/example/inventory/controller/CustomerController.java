package com.example.inventory.controller;

import com.example.inventory.dto.CustomerDtos.*;
import com.example.inventory.entity.User;
import com.example.inventory.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) { this.service = service; }

    @Operation(summary = "Creates a customer")
    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CreateCustomerRequest req) {
        User created = service.create(req.email(), req.fullName());

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                          .path("/{id}").buildAndExpand(created.getId()).toUri();

        return ResponseEntity.created(location).body(toDto(created));
    }

    @Operation(summary = "Returns a single customer by id")
    @GetMapping("/{id}")
    public CustomerResponse getById(@PathVariable Long id) {
        return toDto(service.findById(id));
    }

    private static CustomerResponse toDto(User u) {
        return new CustomerResponse(u.getId(), u.getEmail(), u.getFullName(), u.getCreatedAt());
    }
}
