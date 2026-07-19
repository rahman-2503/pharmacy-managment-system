package com.example.demo.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Drug;
import com.example.demo.service.DrugService;

import jakarta.validation.Valid;

@RestController
@RequestMapping({"/inventory/drug", "/inventory/drug/"})
public class DrugController {

    @Autowired
    private DrugService service;

    // ✅ Add new drug
    @PostMapping
    public ResponseEntity<Drug> addDrug(@Valid @RequestBody Drug drug) {
        return ResponseEntity.ok(service.addDrug(drug));
    }

    // ✅ Get all drugs
    @GetMapping
    public ResponseEntity<List<Drug>> getAllDrugs() {
        return ResponseEntity.ok(service.getAllDrugs());
    }

    // ✅ Get drug by ID
    @GetMapping("/{id}")
    public ResponseEntity<Drug> getDrugById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getDrugById(id));
    }

    // ✅ Update drug
    @PutMapping("/{id}")
    public ResponseEntity<Drug> updateDrug(@PathVariable Long id, @RequestBody Drug drug) {
        return ResponseEntity.ok(service.updateDrug(id, drug));
    }

    // ✅ Delete drug
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteDrug(@PathVariable Long id) {
        service.deleteDrug(id);
        return ResponseEntity.ok("Drug deleted successfully");
    }

    // ✅ Reduce stock
    @PutMapping("/reduce/{id}/{qty}")
    public ResponseEntity<Drug> reduceStock(@PathVariable Long id, @PathVariable int qty) {
        return ResponseEntity.ok(service.reduceQuantity(id, qty));
    }

    // ✅ Increase stock (missing earlier)
    @PutMapping("/increase/{id}/{qty}")
    public ResponseEntity<Drug> increaseStock(@PathVariable Long id, @PathVariable int qty) {
        return ResponseEntity.ok(service.increaseQuantity(id, qty));
    }
}
