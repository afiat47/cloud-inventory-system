package com.example.inventory.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items",
       uniqueConstraints = @UniqueConstraint(columnNames = {"order_id", "product_id"}))
public class OrderItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;                // price snapshot at purchase time

    protected OrderItem() { }

    public OrderItem(Order order, Product product, Integer quantity, BigDecimal unitPrice) {
        this.order = order;
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public Order getOrder() { return order; }

    public void setOrder(Order order) { this.order = order; }

    public Product getProduct() { return product; }

    public void setProduct(Product product) { this.product = product; }

    public Integer getQuantity() { return quantity; }

    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }

    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}
