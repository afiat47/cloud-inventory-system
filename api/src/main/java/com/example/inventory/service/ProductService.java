package com.example.inventory.service;

import com.example.inventory.entity.Product;
import com.example.inventory.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Product> findById(Long id) {
        return repository.findById(id);
    }

    @Transactional(readOnly = true)
    public Page<Product> search(String name, BigDecimal maxPrice,
                                boolean inStockOnly, Pageable pageable) {

        Specification<Product> spec = Specification.unrestricted();

        if (name != null && !name.isBlank())
            spec = spec.and((root, q, cb) ->
                    cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"));

        if (maxPrice != null)
            spec = spec.and((root, q, cb) -> cb.lessThanOrEqualTo(root.get("price"), maxPrice));

        if (inStockOnly)
            spec = spec.and((root, q, cb) -> cb.greaterThan(root.get("quantity"), 0));

        return repository.findAll(spec, pageable);
    }

    @Transactional
    public Product create(Product product) {
        return repository.save(product);
    }

    @Transactional
    public Optional<Product> update(Long id, Product incoming) {
        return repository.findById(id).map(existing -> {
            existing.setName(incoming.getName());
            existing.setDescription(incoming.getDescription());
            existing.setPrice(incoming.getPrice());
            existing.setQuantity(incoming.getQuantity());
            return existing;    // no explicit save() needed — dirty checking
        });
    }

    @Transactional
    public boolean delete(Long id) {
        if (!repository.existsById(id)) return false;
        repository.deleteById(id);
        return true;
    }
}
