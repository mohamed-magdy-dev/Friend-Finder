package com.project._5.Friend_Finder.dto;
import lombok.Data;

import java.time.LocalDate;
@Data
public class ProfileUpdateRequestDto {
    private String fullName;
    private String bio;
    private LocalDate birthDate;
}
