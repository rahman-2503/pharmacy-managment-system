package com.example.demo.service;

import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Payment;
import com.example.demo.feign.OrderClient;
import com.example.demo.repository.PaymentRepository;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private PaymentRepository repo;

    @Autowired
    private OrderClient orderClient;

    @Autowired
    private RabbitMQProducer producer;

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    // ✅ Create Razorpay order (robust: never throws, returns JSON with success/error)
    public String createPayment(Long orderId, Double amount) {
        JSONObject response = new JSONObject();
        try {
            if (orderId == null) {
                response.put("success", false);
                response.put("message", "orderId is required");
                return response.toString();
            }
            if (amount == null || amount <= 0) {
                response.put("success", false);
                response.put("message", "amount must be greater than zero");
                return response.toString();
            }

            RazorpayClient client = new RazorpayClient(keyId, keySecret);

            JSONObject options = new JSONObject();
            options.put("amount", Math.round(amount * 100)); // convert to paise
            options.put("currency", "INR");
            options.put("receipt", "order_rcptid_" + orderId);
            options.put("notes", new JSONObject().put("orderId", orderId));

            Order order = client.orders.create(options);

            response.put("success", true);
            response.put("razorpayOrderId", order.get("id").toString());
            response.put("amount", Integer.parseInt(order.get("amount").toString()));
            response.put("currency", order.get("currency").toString());
            response.put("orderId", orderId);
            response.put("keyId", keyId);
            return response.toString();

        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed for order {}: {}", orderId, e.getMessage());
            response.put("success", false);
            response.put("message", "Razorpay order creation failed: " + e.getMessage());
            response.put("orderId", orderId);
            return response.toString();
        } catch (Exception e) {
            log.error("Unexpected error creating Razorpay order for order {}", orderId, e);
            response.put("success", false);
            response.put("message", "Could not create payment order. Please try again.");
            response.put("orderId", orderId);
            return response.toString();
        }
    }

    // ✅ Verify Razorpay signature
    public boolean verifySignature(String razorpayOrderId,
                                   String paymentId,
                                   String signature) {
        try {
            if (razorpayOrderId == null || paymentId == null || signature == null) {
                return false;
            }
            String payload = razorpayOrderId + "|" + paymentId;

            SecretKeySpec secretKey = new SecretKeySpec(
                    keySecret.getBytes(),
                    "HmacSHA256"
            );

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);

            byte[] hash = mac.doFinal(payload.getBytes());
            String generatedSignature = Hex.encodeHexString(hash);

            return generatedSignature.equals(signature);

        } catch (Exception e) {
            log.error("Signature verification failed", e);
            return false;
        }
    }

    // ✅ Save payment and update order
    public Payment savePayment(Long orderId,
                               Double amount,
                               String status,
                               String paymentId,
                               String signature) {

        Payment existing = repo.findByOrderId(orderId);
        if (existing != null) {
            return existing;
        }

        Payment p = new Payment();
        p.setOrderId(orderId);
        p.setAmount(amount);
        p.setStatus(status);
        p.setTransactionId(UUID.randomUUID().toString());
        p.setRazorpayPaymentId(paymentId);
        p.setRazorpaySignature(signature);

        Payment saved = repo.save(p);

        // ✅ Update order status correctly
        try {
            if ("SUCCESS".equalsIgnoreCase(status)) {
                // Payment succeeded → move order to PLACED
                orderClient.updateStatus(orderId, "PLACED");
            } else if ("FAILED".equalsIgnoreCase(status)) {
                // Payment failed → mark order as FAILED
                orderClient.updateStatus(orderId, "FAILED");
            }
        } catch (Exception e) {
            log.error("Order update failed", e);
        }

        // ✅ Send notification
        try {
            producer.sendMessage("Payment " + status + " for order " + orderId);
        } catch (Exception e) {
            log.error("RabbitMQ failed", e);
        }

        return saved;
    }
}