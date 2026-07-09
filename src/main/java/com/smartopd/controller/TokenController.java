package com.smartopd.controller;

import com.smartopd.dto.request.TokenBookRequest;
import com.smartopd.dto.response.ApiResponse;
import com.smartopd.service.TokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tokens")
@RequiredArgsConstructor
public class TokenController {

    private final TokenService tokenService;

    // Book a token
    @PostMapping("/book")
    public ResponseEntity<ApiResponse<?>> bookToken(
            @Valid @RequestBody TokenBookRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        var response = tokenService.bookToken(
                userDetails.getUsername(), request);
        return ResponseEntity.ok(
                ApiResponse.success("Token booked successfully!", response));
    }

    // Get my tokens
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<?>> getMyTokens(
            @AuthenticationPrincipal UserDetails userDetails) {
        var tokens = tokenService.getMyTokens(userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Tokens fetched successfully!", tokens));
    }

    // Cancel token
    @DeleteMapping("/cancel/{tokenId}")
    public ResponseEntity<ApiResponse<?>> cancelToken(
            @PathVariable Long tokenId,
            @AuthenticationPrincipal UserDetails userDetails) {
        tokenService.cancelToken(tokenId, userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Token cancelled successfully!"));
    }
}