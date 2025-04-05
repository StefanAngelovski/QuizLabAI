package com.lab24.quizlabai.service;

import com.lab24.quizlabai.model.Role;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface UserService extends UserDetailsService {
    void register(String username, String password, String repeatPassword, String email, Role role);
}


