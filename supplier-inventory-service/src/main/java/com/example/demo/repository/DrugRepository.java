package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Drug;
public interface DrugRepository extends JpaRepository<Drug, Long> {
    List<Drug> findByCategory(String category);
    List<Drug> findByNameContainingIgnoreCase(String name);
}
