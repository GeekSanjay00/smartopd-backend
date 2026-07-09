package com.smartopd.controller;

import com.smartopd.dto.response.ApiResponse;
import com.smartopd.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/queue")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;

    // Get live queue status
    @GetMapping("/{departmentId}")
    public ResponseEntity<ApiResponse<?>> getQueueStatus(
            @PathVariable Long departmentId) {
        var status = queueService.getQueueStatus(departmentId);
        return ResponseEntity.ok(
                ApiResponse.success("Queue status fetched!", status));
    }

    // Get queue size
    @GetMapping("/{departmentId}/size")
    public ResponseEntity<ApiResponse<?>> getQueueSize(
            @PathVariable Long departmentId) {
        int size = queueService.getQueueSize(departmentId);
        return ResponseEntity.ok(
                ApiResponse.success("Queue size fetched!", size));
    }
}