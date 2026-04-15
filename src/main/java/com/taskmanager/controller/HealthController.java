package com.taskmanager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
 
import java.time.Instant;
import java.util.Map;
 
@RestController
public class HealthController {
 
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "timestamp", Instant.now().toString(),
            "service", "TaskFlow Backend"
        ));
    }
 
    // /actuator/health already exists if you have actuator dependency
    // /ping is a simpler alternative for the cron job
}