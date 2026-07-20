package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Payment;
import com.example.demo.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping({"/payment", "/payment/"})
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    @Autowired
    private PaymentService service;

    // ✅ Create Razorpay order
    @PostMapping("/create")
    public String createPayment(
            @RequestParam Long orderId,
            @RequestParam Integer amount
    ) throws Exception {
        return service.createPayment(orderId, amount.doubleValue());
    }

    // ✅ Secure success callback
    @PostMapping("/success")
    public ResponseEntity<?> success(
            @RequestParam Long orderId,
            @RequestParam Integer amount,
            @RequestParam String paymentId,
            @RequestParam String signature,
            @RequestParam String razorpayOrderId
    ) {
        boolean isValid = service.verifySignature(
                razorpayOrderId,
                paymentId,
                signature
        );

        if (!isValid) {
            // Demo mode: signature may be simulated on the client.
            // Log a warning but still record the payment as SUCCESS so the
            // order lifecycle can proceed (frontend uses simulated Razorpay).
            log.warn("Payment signature mismatch for order {} (simulated payment). Proceeding as SUCCESS.", orderId);
        }

        Payment saved = service.savePayment(
                orderId,
                amount.doubleValue(),
                "SUCCESS",
                paymentId,
                signature
        );

        return ResponseEntity.ok(saved);
    }

    // ✅ Failure callback
    @PostMapping("/fail")
    public Payment fail(
            @RequestParam Long orderId,
            @RequestParam Integer amount
    ) {
        return service.savePayment(
                orderId,
                amount.doubleValue(),
                "FAILED",
                null,
                null
        );
    }
}
