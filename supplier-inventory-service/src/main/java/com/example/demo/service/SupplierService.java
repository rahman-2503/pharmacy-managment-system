package com.example.demo.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Supplier;
import com.example.demo.Exception.ResourceNotFoundException;
import com.example.demo.repository.SupplierRepository;

@Service
public class SupplierService {

    @Autowired
    private SupplierRepository repo;

    // ✅ Add supplier
    public Supplier addSupplier(Supplier supplier) {
        return repo.save(supplier);
    }

    // ✅ Get all suppliers
    public List<Supplier> getAllSuppliers() {
        return repo.findAll();
    }

    // ✅ Get supplier by ID
    public Supplier getSupplierById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found with id: " + id));
    }

    // ✅ Update supplier
    public Supplier updateSupplier(Long id, Supplier supplier) {
        Supplier existing = getSupplierById(id);
        existing.setName(supplier.getName());
        existing.setContact(supplier.getContact());
        existing.setAddress(supplier.getAddress());
        existing.setEmail(supplier.getEmail());
        return repo.save(existing);
    }

    // ✅ Delete supplier
    public void deleteSupplier(Long id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Supplier not found with id: " + id);
        }
        repo.deleteById(id);
    }
}
