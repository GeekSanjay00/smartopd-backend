package com.smartopd.dto.response;

import com.smartopd.enums.TokenStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    private Long id;
    private String tokenNumber;
    private String departmentName;
    private TokenStatus status;
    private int position;
    private String estimatedWait;
    private LocalDateTime bookedAt;
}