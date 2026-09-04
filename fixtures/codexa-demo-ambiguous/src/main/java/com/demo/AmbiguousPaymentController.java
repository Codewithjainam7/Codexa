package com.demo;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/payments")
public class AmbiguousPaymentController {

    @PostMapping("/process")
    public ResponseEntity<String> processPayment(@RequestBody PaymentPayload payload, HttpServletRequest request) {
        // AMBIGUOUS: Non-standard manual token check in controller rather than Spring Security filter chain or @PreAuthorize.
        // Static analysis flags non-standard access control requiring manual verification.
        String authHeader = request.getHeader("X-Custom-Tenant-Token");
        if (authHeader == null || !authHeader.startsWith("TENANT-SEC-")) {
            return ResponseEntity.status(403).body("Invalid or missing tenant authorization");
        }

        // Custom validation check
        if (payload == null || payload.getAmount() <= 0) {
            return ResponseEntity.badRequest().body("Invalid payment amount");
        }

        return ResponseEntity.ok("Payment processed: " + payload.getOrderId());
    }

    public static class PaymentPayload {
        private String orderId;
        private double amount;

        public String getOrderId() { return orderId; }
        public void setOrderId(String orderId) { this.orderId = orderId; }
        public double getAmount() { return amount; }
        public void setAmount(double amount) { this.amount = amount; }
    }
}
