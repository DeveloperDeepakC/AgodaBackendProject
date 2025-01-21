package com.deepak.projects.airBnbApp.util;

import com.deepak.projects.airBnbApp.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;

public class AppUtils {
    public static User getCurrentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
