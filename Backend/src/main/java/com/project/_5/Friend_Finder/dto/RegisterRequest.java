package com.project._5.Friend_Finder.dto;

import lombok.Data;

import jakarta.validation.constraints.*;

@Data
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is wrong!")
    private String email;

    @NotBlank(message = "Password required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[@#$%^&+=!]).*$", message = "كلمة المرور يجب أن تحتوي على حرف كبير ورمز واحد على الأقل")
    private String password;
}