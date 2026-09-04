package com.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class SecureAdminController {

    private static final Logger log = LoggerFactory.getLogger(SecureAdminController.class);

    // SECURE: Strict @PreAuthorize role-based check
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/deleteAccount")
    public String deleteAccount(@RequestBody SecureUserDeleteRequest req) {
        log.info("Admin executed account deletion for identifier: {}", req.userId);
        return "Deleted successfully";
    }

    public static class SecureUserDeleteRequest {
        public String userId;
    }
}
