package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Supplier;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {
    Supplier findByContact(String contact);
    List<Supplier> findByNameContainingIgnoreCase(String name);
}
