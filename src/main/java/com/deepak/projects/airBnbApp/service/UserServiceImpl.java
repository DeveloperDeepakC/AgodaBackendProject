package com.deepak.projects.airBnbApp.service;

import com.deepak.projects.airBnbApp.dto.ProfileUpdateRequestDto;
import com.deepak.projects.airBnbApp.dto.UserDto;
import com.deepak.projects.airBnbApp.entity.User;
import com.deepak.projects.airBnbApp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import static com.deepak.projects.airBnbApp.util.AppUtils.getCurrentUser;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(()-> new RuntimeException("User not found with id: "+id));
    }

    @Override
    public void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto) {
        User user= getCurrentUser();
        if(profileUpdateRequestDto.getDateOfBirth()!=null){
            user.setDateOfBirth(profileUpdateRequestDto.getDateOfBirth());
        }
        if(profileUpdateRequestDto.getGender()!=null){
            user.setGender(profileUpdateRequestDto.getGender());
        }
        if(profileUpdateRequestDto.getName()!=null){
            user.setName(profileUpdateRequestDto.getName());
        }

        userRepository.save(user);
    }

    @Override
    public UserDto getMyProfile() {
        log.info("Getting profile of current user with id: {}", getCurrentUser().getId());
        User user= getCurrentUser();
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElse(null);
    }
}
