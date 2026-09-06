package com.example.inventory.controller;

import com.example.inventory.dto.ProductDtos.*;
import com.example.inventory.entity.Product;
import com.example.inventory.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @Operation(summary = "Lists products — paged, filterable and sortable")
    @GetMapping
    public Page<ProductResponse> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "false") boolean inStockOnly,
            @PageableDefault(size = 20, sort = "createdAt",
                             direction = Sort.Direction.DESC) Pageable pageable) {

        return service.search(name, maxPrice, inStockOnly, pageable)
                      .map(ProductController::toDto);
    }

    @Operation(summary = "Returns a single product by id")
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(p -> ResponseEntity.ok(toDto(p)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Creates a product")
    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody CreateProductRequest req) {
        Product created = service.create(
                new Product(req.name(), req.description(), req.price(), req.quantity()));

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();

        return ResponseEntity.created(location).body(toDto(created));
    }

    @Operation(summary = "Replaces a product")
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable Long id,
                                       @Valid @RequestBody UpdateProductRequest req) {
        Product incoming = new Product(req.name(), req.description(), req.price(), req.quantity());
        return service.update(id, incoming).isPresent()
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Deletes a product")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    private static ProductResponse toDto(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getDescription(),
                                   p.getPrice(), p.getQuantity(), p.getCreatedAt());
    }
}
