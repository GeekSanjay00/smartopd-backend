package com.smartopd.controller;

import com.smartopd.dto.response.ApiResponse;
import com.smartopd.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    // Get overall stats
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<?>> getOverallStats() {
        var stats = analyticsService.getOverallStats();
        return ResponseEntity.ok(
                ApiResponse.success("Stats fetched successfully!", stats));
    }

    // Get avg wait time per department
    @GetMapping("/wait-time")
    public ResponseEntity<ApiResponse<?>> getAvgWaitTime() {
        var waitTime = analyticsService.getAvgWaitTimeByDepartment();
        return ResponseEntity.ok(
                ApiResponse.success("Wait time fetched!", waitTime));
    }

    // Get today tokens per department
    @GetMapping("/tokens-today")
    public ResponseEntity<ApiResponse<?>> getTodayTokens() {
        var tokens = analyticsService.getTodayTokensByDepartment();
        return ResponseEntity.ok(
                ApiResponse.success("Today tokens fetched!", tokens));
    }

    // Get peak hours
    @GetMapping("/peak-hours")
    public ResponseEntity<ApiResponse<?>> getPeakHours() {
        var peakHours = analyticsService.getPeakHours();
        return ResponseEntity.ok(
                ApiResponse.success("Peak hours fetched!", peakHours));
    }
}