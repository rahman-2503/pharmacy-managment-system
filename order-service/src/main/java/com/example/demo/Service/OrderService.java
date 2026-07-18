package com.example.demo.Service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.demo.OrderDTO.Drug;
import com.example.demo.OrderDTO.OrderDTO;
import com.example.demo.Repository.OrderRepository;
import com.example.demo.entity.Order;
import com.example.demo.entity.OrderStatus;
import com.example.demo.exception.*;

import com.example.demo.feign.InventoryClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.transaction.Transactional;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository repo;
    private final InventoryClient inventoryClient;
    private final RabbitMQProducer producer;

    public OrderService(OrderRepository repo,
                        InventoryClient inventoryClient,
                        RabbitMQProducer producer) {
        this.repo = repo;
        this.inventoryClient = inventoryClient;
        this.producer = producer;
    }

    // ✅ CREATE ORDER (default PENDING, no stock change yet)
    @Transactional
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "inventoryFallback")
    public Order placeOrder(OrderDTO dto) {
        Drug drug = inventoryClient.getDrug(dto.getDrugId());

        if (drug == null) {
            throw new DrugNotFoundException("Drug not found with id: " + dto.getDrugId());
        }

        if (drug.getQuantity() < dto.getQuantity()) {
            throw new InsufficientStockException("Insufficient stock for drug id: " + dto.getDrugId());
        }

        Order order = new Order();
        order.setDoctorId(dto.getDoctorId());
        order.setDrugId(dto.getDrugId());
        order.setQuantity(dto.getQuantity());
        order.setStatus(OrderStatus.PENDING);

        Order saved = repo.save(order);
        producer.sendMessage("Order " + saved.getId() + " created with status PENDING");
        return saved;
    }

    // ✅ INVENTORY FAILURE FALLBACK
    public Order inventoryFallback(OrderDTO dto, Throwable ex) {
        Order order = new Order();
        order.setDoctorId(dto.getDoctorId());
        order.setDrugId(dto.getDrugId());
        order.setQuantity(dto.getQuantity());
        order.setStatus(OrderStatus.FAILED);

        Order saved = repo.save(order);
        producer.sendMessage("Order " + saved.getId() + " FAILED due to inventory error");
        return saved;
    }

    // ✅ GET ALL ORDERS
    public List<Order> getAllOrders() {
        return repo.findAll();
    }

    // ✅ VERIFY (Admin only) → PLACED → VERIFIED
    public Order verifyOrder(Long id) {
        Order order = getOrder(id);

        if (order.getStatus() != OrderStatus.PLACED && order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("Order must be PENDING or PLACED to VERIFY");
        }

        order.setStatus(OrderStatus.VERIFIED);
        Order saved = repo.save(order);

        producer.sendMessage("Order " + saved.getId() + " VERIFIED by Admin");
        return saved;
    }

    // ✅ PICK (Admin only) → VERIFIED → PICKED
    public Order pickOrder(Long id) {
        Order order = getOrder(id);

        if (order.getStatus() != OrderStatus.VERIFIED) {
            throw new InvalidOrderStateException("Order must be VERIFIED first");
        }

        order.setStatus(OrderStatus.PICKED);
        Order saved = repo.save(order);

        producer.sendMessage("Order " + saved.getId() + " PICKED up");
        return saved;
    }

    // ✅ CANCEL (Doctor can cancel if PENDING or PLACED)
    public Order cancelOrder(Long id) {
        Order order = getOrder(id);

        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.CANCELLED);
        } else if (order.getStatus() == OrderStatus.PLACED) {
            // return stock if cancelled after payment
            try {
                inventoryClient.increaseStock(order.getDrugId(), order.getQuantity());
            } catch (Exception e) {
                log.error("Failed to increase stock via inventory Feign client: {}", e.getMessage());
            }
            order.setStatus(OrderStatus.CANCELLED);
        } else {
            throw new InvalidOrderStateException("Order cannot be cancelled after verification or pickup");
        }

        Order saved = repo.save(order);
        producer.sendMessage("Order " + saved.getId() + " CANCELLED (refund in 10-15 min)");
        return saved;
    }

    // ✅ DELETE (Admin only, after cancellation)
    public void deleteOrder(Long id) {
        Order order = getOrder(id);

        if (order.getStatus() == OrderStatus.CANCELLED) {
            repo.delete(order);
            producer.sendMessage("Order " + id + " deleted by Admin");
        } else {
            throw new InvalidOrderStateException("Only CANCELLED orders can be deleted");
        }
    }

    // ✅ FAIL (Payment failure) → PENDING → FAILED
    public Order failOrder(Long id) {
        Order order = getOrder(id);

        if (order.getStatus() == OrderStatus.PENDING) {
            order.setStatus(OrderStatus.FAILED);
            Order saved = repo.save(order);

            producer.sendMessage("Order " + saved.getId() + " FAILED during payment");
            return saved;
        } else {
            throw new InvalidOrderStateException("Only PENDING orders can fail payment");
        }
    }

    // ✅ RETRY PAYMENT → FAILED → PLACED → reduce stock
    public Order retryPayment(Long id) {
        Order order = getOrder(id);

        if (order.getStatus() == OrderStatus.FAILED) {
            inventoryClient.reduceStock(order.getDrugId(), order.getQuantity());
            order.setStatus(OrderStatus.PLACED);
            Order saved = repo.save(order);

            producer.sendMessage("Order " + saved.getId() + " retried and moved to PLACED");
            return saved;
        } else {
            throw new InvalidOrderStateException("Retry only allowed for FAILED orders");
        }
    }

    // ✅ UPDATE STATUS (used internally by Payment Service)
    public Order updateOrderStatus(Long orderId, String status) {
        Order order = getOrder(orderId);
        OrderStatus newStatus = OrderStatus.valueOf(status.toUpperCase());

        // reduce stock when payment succeeds
        if (newStatus == OrderStatus.PLACED && order.getStatus() == OrderStatus.PENDING) {
            try {
                inventoryClient.reduceStock(order.getDrugId(), order.getQuantity());
            } catch (Exception e) {
                log.error("Failed to reduce stock via inventory Feign client: {}", e.getMessage());
            }
        }

        order.setStatus(newStatus);
        Order saved = repo.save(order);

        producer.sendMessage("Order " + saved.getId() + " updated to status " + newStatus);
        return saved;
    }

    // ✅ SALES REPORT (Admin only)
    public List<Order> salesReport() {
        return repo.findAll()
                .stream()
                .filter(o -> o.getStatus() == OrderStatus.PLACED
                          || o.getStatus() == OrderStatus.VERIFIED
                          || o.getStatus() == OrderStatus.PICKED)
                .toList();
    }

    // Helper
    private Order getOrder(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
    }
}
