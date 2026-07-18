package com.example.demo.entity;

public enum OrderStatus {
    PENDING,    // Doctor created order but payment not done
    PLACED,     // Payment completed successfully
    FAILED,     // Payment failed (retry option)
    CANCELLED,  // Doctor cancelled before Admin verification
    VERIFIED,   // Admin verified order
    PICKED      // Order picked up
}
