package com.deepak.projects.airBnbApp.dto;

import com.deepak.projects.airBnbApp.entity.User;
import com.deepak.projects.airBnbApp.entity.enums.Gender;
import jakarta.persistence.*;
import lombok.Data;

@Data
public class GuestDto {
    private Long id;
    private User user;
    private String name;
    private Gender gender;
    private Integer age;
}
