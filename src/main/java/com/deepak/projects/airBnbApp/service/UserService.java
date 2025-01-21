package com.deepak.projects.airBnbApp.service;

import com.deepak.projects.airBnbApp.dto.ProfileUpdateRequestDto;
import com.deepak.projects.airBnbApp.dto.UserDto;
import com.deepak.projects.airBnbApp.entity.User;

public interface UserService {
    User getUserById(Long id);

    void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto);

    UserDto getMyProfile();
}
