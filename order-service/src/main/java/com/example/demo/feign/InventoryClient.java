package com.example.demo.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.example.demo.OrderDTO.Drug;

@FeignClient(name = "SUPPLIER-INVENTORY-SERVICE", url = "${inventory.service.url}")
public interface InventoryClient {

    @GetMapping("/inventory/drug/{id}")
    Drug getDrug(@PathVariable("id") Long id);

    @PutMapping("/inventory/drug/reduce/{id}/{qty}")
    Drug reduceStock(@PathVariable("id") Long id,
                     @PathVariable("qty") int qty);

    @PutMapping("/inventory/drug/increase/{id}/{qty}")
    Drug increaseStock(@PathVariable("id") Long id,
                       @PathVariable("qty") int qty);
}
