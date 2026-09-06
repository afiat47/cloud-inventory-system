package com.example.inventory.repository;

import com.example.inventory.dto.RevenueRow;
import com.example.inventory.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 1. JPQL fetch join — one query, explicit
    @Query("select distinct o from Order o " +
           "join fetch o.user " +
           "join fetch o.items i join fetch i.product " +
           "where o.id = :id")
    Optional<Order> findByIdWithDetails(@Param("id") Long id);

    // Day 3: used by getById and cancel so neither of them N+1s
    @Query("select distinct o from Order o " +
           "join fetch o.items i join fetch i.product " +
           "where o.id = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    // 2. An entity graph — declarative
    @EntityGraph(attributePaths = {"user", "items", "items.product"})
    List<Order> findByStatus(Order.Status status);

    Page<Order> findByUserId(Long userId, Pageable pageable);

    @Query("""
           select new com.example.inventory.dto.RevenueRow(
                  i.product.name, sum(i.quantity), sum(i.quantity * i.unitPrice))
           from OrderItem i
           group by i.product.name
           order by sum(i.quantity * i.unitPrice) desc
           """)
    List<RevenueRow> revenuePerProduct();
}
