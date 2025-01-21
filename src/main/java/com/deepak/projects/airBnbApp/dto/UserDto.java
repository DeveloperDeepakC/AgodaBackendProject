package com.deepak.projects.airBnbApp.dto;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private String gender;
    private String dateOfBirth;
}

