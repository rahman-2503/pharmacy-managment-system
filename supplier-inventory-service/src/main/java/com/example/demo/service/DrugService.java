package com.example.demo.service;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.Exception.InsufficientStockException;
import com.example.demo.Exception.ResourceNotFoundException;
import com.example.demo.entity.Drug;
import com.example.demo.repository.DrugRepository;
import com.example.demo.service.NotificationProducer;

@Service
public class DrugService {

    @Autowired
    private DrugRepository repo;

    @Autowired
    private NotificationProducer notificationProducer;

    // ✅ Add new drug (with photoUrl)
    public Drug addDrug(Drug drug) {
        if (drug.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        if (drug.getPrice() <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }

        Drug saved = repo.save(drug);

        try {
            String msg = "New drug added to inventory: " + saved.getName() + " (₹" + saved.getPrice() + ", " + saved.getQuantity() + " units)";
            notificationProducer.sendNotification(msg, "DRUG");
        } catch (Exception e) {
            // Notification failure should not block drug creation
        }

        return saved;
    }

    // ✅ Get all drugs
    public List<Drug> getAllDrugs() {
        return repo.findAll();
    }

    // ✅ Get drug by ID
    public Drug getDrugById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Drug not found with id: " + id));
    }

    // ✅ Update drug (including photoUrl)
    public Drug updateDrug(Long id, Drug drug) {
        Drug existing = getDrugById(id);

        if (drug.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        if (drug.getPrice() <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }

        existing.setName(drug.getName());
        existing.setCategory(drug.getCategory());
        existing.setQuantity(drug.getQuantity());
        existing.setPrice(drug.getPrice());

        // ✅ Update photoUrl
        existing.setPhotoUrl(drug.getPhotoUrl());

        return repo.save(existing);
    }

    // ✅ Delete drug
    public void deleteDrug(Long id) {
        if (!repo.existsById(id)) {
            throw new ResourceNotFoundException("Drug not found with id: " + id);
        }
        repo.deleteById(id);
    }

    // ✅ Reduce stock
    public Drug reduceQuantity(Long id, int qty) {
        Drug drug = getDrugById(id);

        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (drug.getQuantity() < qty) {
            throw new InsufficientStockException("Not enough stock for drug id: " + id);
        }

        drug.setQuantity(drug.getQuantity() - qty);
        return repo.save(drug);
    }

    // ✅ Increase stock
    public Drug increaseQuantity(Long id, int qty) {
        Drug drug = getDrugById(id);

        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        drug.setQuantity(drug.getQuantity() + qty);
        return repo.save(drug);
    }
}
