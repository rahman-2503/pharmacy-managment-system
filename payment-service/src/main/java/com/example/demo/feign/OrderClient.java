package com.example.demo.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@FeignClient(name = "ORDER-SERVICE", url = "${order.service.url}")
public interface OrderClient {

    @PutMapping("/orders/update-status/{orderId}/{status}")
    void updateStatus(@PathVariable("orderId") Long orderId,
                      @PathVariable("status") String status);
}