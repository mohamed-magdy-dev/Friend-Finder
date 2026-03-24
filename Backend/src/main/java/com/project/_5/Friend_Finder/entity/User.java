package com.project._5.Friend_Finder.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "APP_USERS") //Oracle has USER .. so i had to change it.
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    private String role;

    private String bio;
    private String profilePictureUrl;
    private String coverPictureUrl;
    private java.time.LocalDate birthDate;
}