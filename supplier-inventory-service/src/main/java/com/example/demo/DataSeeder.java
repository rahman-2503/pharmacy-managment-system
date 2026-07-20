package com.example.demo;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.example.demo.entity.Supplier;
import com.example.demo.repository.SupplierRepository;

@Component
public class DataSeeder implements CommandLineRunner {

    private final SupplierRepository supplierRepository;

    public DataSeeder(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    @Override
    public void run(String... args) {
        // Seed default suppliers only if the table is empty
        if (supplierRepository.count() == 0) {
            List<Supplier> defaults = List.of(
                create("MediCorp Wholesale", "9876500001", "12 Industrial Area, Mumbai", "mediwholesale@pharm.co"),
                create("HealthPlus Distributors", "9876500002", "45 MG Road, Bengaluru", "healthplus@pharm.co"),
                create("LifeCare Pharma Supply", "9876500003", "8 Lake View, Kolkata", "lifecare@pharm.co"),
                create("Wellness Traders", "9876500004", "23 Nehru Nagar, Pune", "wellness@pharm.co"),
                create("Prime Medical Agencies", "9876500005", "67 Anna Salai, Chennai", "primemed@pharm.co"),
                create("Safemed Logistics", "9876500006", "5 Civil Lines, Delhi", "safemed@pharm.co"),
                create("BioGen Supply Co.", "9876500007", "90 Banjara Hills, Hyderabad", "biogen@pharm.co"),
                create("CareLink Distributors", "9876500008", "31 Gomti Nagar, Lucknow", "carelink@pharm.co"),
                create("PharmaRoot Wholesale", "9876500009", "14 Bistro Road, Ahmedabad", "pharmaroot@pharm.co"),
                create("VitalStock Suppliers", "9876500010", "77 Salt Lake, Kolkata", "vitalstock@pharm.co")
            );
            supplierRepository.saveAll(defaults);
        }
    }

    private Supplier create(String name, String contact, String address, String email) {
        Supplier s = new Supplier();
        s.setName(name);
        s.setContact(contact);
        s.setAddress(address);
        s.setEmail(email);
        return s;
    }
}
