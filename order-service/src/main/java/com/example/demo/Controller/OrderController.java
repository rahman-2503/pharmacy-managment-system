package com.example.demo.Controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.example.demo.OrderDTO.OrderDTO;
import com.example.demo.Service.OrderService;
import com.example.demo.entity.Order;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    // ✅ CREATE ORDER (default PENDING)
    @PostMapping
    public Order placeOrder(@Valid @RequestBody OrderDTO dto) {
        return service.placeOrder(dto);
    }

    // ✅ GET ALL ORDERS
    @GetMapping
    public List<Order> getAll() {
        return service.getAllOrders();
    }

    // ✅ VERIFY ORDER (Admin only)
    @PutMapping("/verify/{id}")
    public Order verify(@PathVariable Long id) {
        return service.verifyOrder(id);
    }

    // ✅ PICK ORDER (Admin only)
    @PutMapping("/pick/{id}")
    public Order pick(@PathVariable Long id) {
        return service.pickOrder(id);
    }

    // ✅ CANCEL ORDER (Doctor can cancel before verification)
    @PutMapping("/cancel/{id}")
    public Order cancel(@PathVariable Long id) {
        return service.cancelOrder(id);
    }

    // ✅ DELETE ORDER (Admin only, after cancellation)
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        service.deleteOrder(id);
        return "Order deleted successfully";
    }

    // ✅ MARK PAYMENT FAILED (used by Payment Service)
    @PutMapping("/fail/{id}")
    public Order fail(@PathVariable Long id) {
        return service.failOrder(id);
    }

    // ✅ RETRY PAYMENT (Doctor/Payment Service)
    @PutMapping("/retry/{id}")
    public Order retryPayment(@PathVariable Long id) {
        return service.retryPayment(id);
    }

    // ✅ SALES REPORT (Admin only)
    @GetMapping("/sales")
    public List<Order> sales() {
        return service.salesReport();
    }

    // ✅ GENERIC STATUS UPDATE (internal use by Payment Service)
    @PutMapping("/update-status/{orderId}/{status}")
    public Order updateStatus(@PathVariable Long orderId,
                              @PathVariable String status) {
        return service.updateOrderStatus(orderId, status);
    }
}
