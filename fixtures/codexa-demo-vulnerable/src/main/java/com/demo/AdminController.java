package com.demo;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    // VULNERABLE: CR-AUTH-001 (Missing @PreAuthorize on Admin endpoint)
    // VULNERABLE: CR-OPS-004 (Unvalidated @RequestBody)
    @PostMapping("/deleteAccount")
    public String deleteAccount(@RequestBody UserDeleteRequest req) {
        try {
            // Processing logic
            System.out.println("Processing account deletion for: " + req);
        } catch (Exception e) {
            // VULNERABLE: CR-QUAL-006 (Empty catch block)
        }
        return "Deleted";
    }

    public static class UserDeleteRequest {
        public String userId;
    }
}
