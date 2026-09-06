package com.example.inventory.service;

import com.example.inventory.dto.OrderDtos.CreateOrderRequest;
import com.example.inventory.dto.OrderDtos.OrderLineRequest;
import com.example.inventory.entity.Order;
import com.example.inventory.entity.OrderItem;
import com.example.inventory.entity.Product;
import com.example.inventory.entity.User;
import com.example.inventory.exception.InsufficientStockException;
import com.example.inventory.repository.OrderRepository;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock ProductRepository products;
    @Mock UserRepository    users;
    @Mock OrderRepository   orders;
    @InjectMocks OrderService service;

    @Test
    void place_throws_whenStockInsufficient() {
        var customer = new User("a@b.com", "Ann");
        var product  = new Product("Keyboard", null, new BigDecimal("50.00"), 2);
        product.setId(1L);

        when(users.findById(9L)).thenReturn(Optional.of(customer));
        when(products.findAllById(any())).thenReturn(List.of(product));

        var request = new CreateOrderRequest(9L, List.of(new OrderLineRequest(1L, 5)));

        assertThatThrownBy(() -> service.place(request))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("available 2");

        verify(orders, never()).save(any());     // nothing was persisted
    }

    @Test
    void place_mergesDuplicateLinesForSameProduct() {
        var customer = new User("a@b.com", "Ann");
        var product  = new Product("Keyboard", null, new BigDecimal("50.00"), 10);
        product.setId(1L);

        when(users.findById(9L)).thenReturn(Optional.of(customer));
        when(products.findAllById(any())).thenReturn(List.of(product));
        when(orders.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        // the same product twice: 2 + 3 must collapse into a single line of 5
        var request = new CreateOrderRequest(9L, List.of(
                new OrderLineRequest(1L, 2),
                new OrderLineRequest(1L, 3)));

        Order order = service.place(request);

        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().get(0).getQuantity()).isEqualTo(5);
        assertThat(order.getTotal()).isEqualByComparingTo("250.00");
        assertThat(product.getQuantity()).isEqualTo(5);   // 10 - 5
    }

    @Test
    void place_snapshotsThePriceAtPurchaseTime() {
        var customer = new User("a@b.com", "Ann");
        var product  = new Product("Mouse", null, new BigDecimal("20.00"), 10);
        product.setId(7L);

        when(users.findById(9L)).thenReturn(Optional.of(customer));
        when(products.findAllById(any())).thenReturn(List.of(product));
        when(orders.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order order = service.place(new CreateOrderRequest(9L, List.of(new OrderLineRequest(7L, 2))));

        OrderItem line = order.getItems().get(0);
        assertThat(line.getUnitPrice()).isEqualByComparingTo("20.00");

        // editing the product afterwards must not rewrite history
        product.setPrice(new BigDecimal("35.00"));
        assertThat(line.getUnitPrice()).isEqualByComparingTo("20.00");
    }

    @Test
    void cancel_rejectsShippedOrders() {
        var customer = new User("a@b.com", "Ann");
        var order = new Order(customer);
        order.setId(4L);
        order.setStatus(Order.Status.SHIPPED);

        when(orders.findByIdWithItems(4L)).thenReturn(Optional.of(order));

        assertThatThrownBy(() -> service.cancel(4L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only PENDING orders can be cancelled");

        assertThat(order.getStatus()).isEqualTo(Order.Status.SHIPPED);
    }

    @Test
    void cancel_returnsTheStock() {
        var customer = new User("a@b.com", "Ann");
        var product  = new Product("Cable", null, new BigDecimal("5.00"), 1);
        product.setId(2L);

        var order = new Order(customer);
        order.setId(5L);
        order.getItems().add(new OrderItem(order, product, 4, new BigDecimal("5.00")));

        when(orders.findByIdWithItems(5L)).thenReturn(Optional.of(order));

        Order cancelled = service.cancel(5L);

        assertThat(cancelled.getStatus()).isEqualTo(Order.Status.CANCELLED);
        assertThat(product.getQuantity()).isEqualTo(5);   // 1 + 4 returned
    }
}
