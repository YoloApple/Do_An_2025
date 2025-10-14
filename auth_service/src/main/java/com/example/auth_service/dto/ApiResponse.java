package com.example.auth_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private String status;
    private T data;
    private String message;
    private LocalDateTime timestamp= LocalDateTime.now();

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>("success", data, message, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> success(T data){
        return  new ApiResponse<>("success",data,null,LocalDateTime.now());
    }

    public static <T> ApiResponse<T> success(String message){
        return  new ApiResponse<>("success",null,message,LocalDateTime.now());
    }
    public static <T> ApiResponse<T> error(String message){
        return  new ApiResponse<>("error",null,message,LocalDateTime.now());
    }
}
