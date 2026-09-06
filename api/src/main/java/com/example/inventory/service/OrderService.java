package com.example.inventory.service;

import com.example.inventory.dto.OrderDtos.*;
import com.example.inventory.entity.*;
import com.example.inventory.exception.*;
import com.example.inventory.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class OrderService {

    private final OrderRepository   orders;
    private final ProductRepository products;
    private final UserRepository    users;

    public OrderService(OrderRepository orders, ProductRepository products, UserRepository users) {
        this.orders = orders; this.products = products; this.users = users;
    }

    @Transactional
    public Order place(CreateOrderRequest request) {

        User customer = users.findById(request.customerId())
                .orElseThrow(() -> new NotFoundException("Customer", request.customerId()));

        // Merge duplicate lines for the same product before doing anything else
        Map<Long, Integer> wanted = new LinkedHashMap<>();
        for (var line : request.lines()) {
            wanted.merge(line.productId(), line.quantity(), Integer::sum);
        }

        // One query for all products, not one per line — avoids N+1 by design
        Map<Long, Product> found = new HashMap<>();
        products.findAllById(wanted.keySet()).forEach(p -> found.put(p.getId(), p));

        Order order = new Order(customer);
        BigDecimal total = BigDecimal.ZERO;

        for (var entry : wanted.entrySet()) {
            Product product = found.get(entry.getKey());
            if (product == null) throw new NotFoundException("Product", entry.getKey());

            int qty = entry.getValue();
            if (product.getQuantity() < qty) {
                throw new InsufficientStockException(
                        product.getName(), qty, product.getQuantity());
            }

            product.setQuantity(product.getQuantity() - qty);   // dirty checking writes this

            OrderItem item = new OrderItem(order, product, qty, product.getPrice());
            order.getItems().add(item);                          // cascade persists it

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(qty)));
        }

        order.setTotal(total);
        return orders.save(order);
    }

    @Transactional(readOnly = true)
    public Order findByIdWithItems(Long orderId) {
        return orders.findByIdWithItems(orderId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));
    }

    @Transactional
    public Order cancel(Long orderId) {
        Order order = orders.findByIdWithItems(orderId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));

        if (order.getStatus() != Order.Status.PENDING) {
            throw new IllegalStateException(
                    "Only PENDING orders can be cancelled; this one is " + order.getStatus());
        }

        // Return the stock
        order.getItems().forEach(item ->
            item.getProduct().setQuantity(item.getProduct().getQuantity() + item.getQuantity()));

        order.setStatus(Order.Status.CANCELLED);
        return order;
    }
}
