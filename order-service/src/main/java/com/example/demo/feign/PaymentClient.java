package com.example.demo.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.OrderDTO.PaymentResponse;

@FeignClient(name = "PAYMENT-SERVICE", url = "${payment.service.url}")
public interface PaymentClient {

    @PostMapping("/payment/create")
    PaymentResponse createPayment(@RequestParam("orderId") Long orderId,
                                  @RequestParam("amount") Double amount);
}
