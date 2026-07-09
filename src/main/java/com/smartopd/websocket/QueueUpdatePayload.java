package com.smartopd.websocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueueUpdatePayload {

    private Long departmentId;
    private String departmentName;
    private String currentServingToken;
    private int totalWaiting;
    private int avgWaitMinutes;
}
