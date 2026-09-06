package com.example.inventory.repository;

import com.example.inventory.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository
        extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    List<Product> findByPriceLessThan(BigDecimal max);
    List<Product> findByNameContainingIgnoreCase(String fragment);
    List<Product> findByQuantityGreaterThanOrderByPriceAsc(int min);
    Page<Product>  findByPriceBetween(BigDecimal lo, BigDecimal hi, Pageable page);
    boolean        existsByName(String name);
    long           countByQuantity(int quantity);

    // Native SQL when you need a database-specific feature
    @Query(value = "SELECT * FROM products WHERE to_tsvector(name) @@ to_tsquery(:q)",
           nativeQuery = true)
    List<Product> fullTextSearch(@Param("q") String q);
}
