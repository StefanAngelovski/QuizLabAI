package com.lab24.quizlabai.service.impl;

import com.lab24.quizlabai.model.User;
import com.lab24.quizlabai.model.exceptions.InvalidArgumentsException;
import com.lab24.quizlabai.model.exceptions.InvalidUserCredentialsException;
import com.lab24.quizlabai.repository.UserRepository;
import com.lab24.quizlabai.service.AuthService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    public AuthServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User login(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            throw new InvalidArgumentsException();
        }
        return userRepository.findByUsernameAndPassword(username, password)
                .orElseThrow(InvalidUserCredentialsException::new);
    }
}
