package com.project._5.Friend_Finder.dto;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
@Data
public class ProfileUpdateRequestDto {
    @Size(max = 60, message = "Full name is too long")
    private String fullName;

    @Size(max = 300, message = "Bio must be 300 characters or fewer")
    private String bio;

    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;
}
