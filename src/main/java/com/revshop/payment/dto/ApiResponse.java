package com.revshop.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String status;
    private String message;
    private T data;

    public ApiResponse(String message, T data) {
        this.status = "success";
        this.message = message;
        this.data = data;
    }
}
