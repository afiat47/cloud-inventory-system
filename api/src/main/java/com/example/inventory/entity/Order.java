package com.example.inventory.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    public enum Status { PENDING, PAID, SHIPPED, CANCELLED }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)                 // store "PAID", not 1
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    protected Order() { }

    public Order(User user) {
        this.user = user;
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }

    public Status getStatus() { return status; }

    public void setStatus(Status status) { this.status = status; }

    public BigDecimal getTotal() { return total; }

    public void setTotal(BigDecimal total) { this.total = total; }

    public Instant getCreatedAt() { return createdAt; }

    public List<OrderItem> getItems() { return items; }
}
