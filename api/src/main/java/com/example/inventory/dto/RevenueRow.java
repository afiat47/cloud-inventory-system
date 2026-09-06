package com.example.inventory.dto;

import java.math.BigDecimal;

public record RevenueRow(String productName, Long unitsSold, BigDecimal revenue) { }
