package com.smartopd.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenBookRequest {

    @NotNull(message = "Department ID is required")
    private Long departmentId;
}
