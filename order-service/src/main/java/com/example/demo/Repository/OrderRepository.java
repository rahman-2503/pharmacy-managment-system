package com.example.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    // No extra methods needed for now
}
