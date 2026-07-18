package com.example.demo.OrderDTO;

import jakarta.validation.constraints.*;

public class OrderDTO {

    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Drug ID is required")
    private Long drugId;

    @Min(value = 1, message = "Quantity must be at least 1")
    private int quantity;

    // Getters & Setters
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public Long getDrugId() { return drugId; }
    public void setDrugId(Long drugId) { this.drugId = drugId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
