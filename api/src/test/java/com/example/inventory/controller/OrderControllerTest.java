package com.example.inventory.controller;

import com.example.inventory.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired MockMvc mvc;

    // Boot 4 removed @MockBean; @MockitoBean is the replacement
    @MockitoBean OrderService service;

    @Test
    void place_rejectsEmptyLineList() throws Exception {
        mvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\":1,\"lines\":[]}"))
           .andExpect(status().isBadRequest());
    }

    @Test
    void place_rejectsNegativeQuantityInsideALine() throws Exception {
        // proves @Valid on the nested list is actually descending into each line
        mvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"customerId\":1,\"lines\":[{\"productId\":1,\"quantity\":-5}]}"))
           .andExpect(status().isBadRequest());
    }

    @Test
    void place_rejectsMissingCustomerId() throws Exception {
        mvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"lines\":[{\"productId\":1,\"quantity\":2}]}"))
           .andExpect(status().isBadRequest());
    }
}
