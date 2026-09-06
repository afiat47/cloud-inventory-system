package com.example.inventory;

import com.example.inventory.dto.OrderDtos.CreateOrderRequest;
import com.example.inventory.dto.OrderDtos.OrderLineRequest;
import com.example.inventory.entity.Product;
import com.example.inventory.entity.User;
import com.example.inventory.exception.InsufficientStockException;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.UserRepository;
import com.example.inventory.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
class OrderIntegrationTest {

    @Container @ServiceConnection
    static PostgreSQLContainer<?> db = new PostgreSQLContainer<>("postgres:16");

    @Autowired OrderService      service;
    @Autowired ProductRepository products;
    @Autowired UserRepository    users;

    @Test
    void placingAnOrder_decrementsStock_andRollsBackOnFailure() {
        User customer = users.save(new User("rollback@example.com", "Rollback Tester"));

        var ok  = products.save(new Product("Mouse", null, new BigDecimal("20.00"), 10));
        var low = products.save(new Product("Cable", null, new BigDecimal("5.00"), 1));

        var request = new CreateOrderRequest(customer.getId(), List.of(
                new OrderLineRequest(ok.getId(), 2),
                new OrderLineRequest(low.getId(), 5)));     // this line fails

        assertThatThrownBy(() -> service.place(request))
                .isInstanceOf(InsufficientStockException.class);

        // The critical assertion: the FIRST product's stock was not touched
        assertThat(products.findById(ok.getId()).orElseThrow().getQuantity())
                .isEqualTo(10);
    }

    @Test
    void placingAValidOrder_decrementsStock_andStoresLinesAtPurchasePrice() {
        User customer = users.save(new User("happy@example.com", "Happy Path"));

        var keyboard = products.save(new Product("Keyboard", null, new BigDecimal("89.99"), 12));

        var order = service.place(new CreateOrderRequest(customer.getId(),
                List.of(new OrderLineRequest(keyboard.getId(), 3))));

        assertThat(order.getId()).isNotNull();
        assertThat(order.getTotal()).isEqualByComparingTo("269.97");
        assertThat(products.findById(keyboard.getId()).orElseThrow().getQuantity()).isEqualTo(9);

        var reloaded = service.findByIdWithItems(order.getId());
        assertThat(reloaded.getItems()).hasSize(1);
        assertThat(reloaded.getItems().get(0).getUnitPrice()).isEqualByComparingTo("89.99");
    }
}
