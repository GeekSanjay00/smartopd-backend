package com.smartopd.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueStatusResponse {

    private Long departmentId;
    private String departmentName;
    private String currentServingToken;
    private int totalWaiting;
    private int avgWaitMinutes;
    private List<TokenInfo> waitingTokens;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TokenInfo {
        private String tokenNumber;
        private int position;
        private String estimatedWait;
    }
}
