package com.example.demo.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Supplier;
import com.example.demo.service.SupplierService;

import jakarta.validation.Valid;

@RestController
@RequestMapping({"/inventory/supplier", "/inventory/supplier/"})
public class SupplierController {

    @Autowired
    private SupplierService service;

    // ✅ Add supplier
    @PostMapping
    public ResponseEntity<Supplier> addSupplier(@Valid @RequestBody Supplier supplier) {
        return ResponseEntity.ok(service.addSupplier(supplier));
    }

    // ✅ Get all suppliers
    @GetMapping
    public ResponseEntity<List<Supplier>> getAllSuppliers() {
        return ResponseEntity.ok(service.getAllSuppliers());
    }

    // ✅ Get supplier by ID
    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getSupplierById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getSupplierById(id));
    }

    // ✅ Update supplier
    @PutMapping("/{id}")
    public ResponseEntity<Supplier> updateSupplier(@PathVariable Long id, @RequestBody Supplier supplier) {
        return ResponseEntity.ok(service.updateSupplier(id, supplier));
    }

    // ✅ Delete supplier
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSupplier(@PathVariable Long id) {
        service.deleteSupplier(id);
        return ResponseEntity.ok("Supplier deleted successfully");
    }
}
