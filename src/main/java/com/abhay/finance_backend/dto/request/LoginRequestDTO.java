package com.abhay.finance_backend.dto.request;
import lombok.Data;

@Data
public class LoginRequestDTO {
    private String email;
    private String password;
}