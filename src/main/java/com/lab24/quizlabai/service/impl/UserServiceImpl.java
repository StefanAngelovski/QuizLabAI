package com.lab24.quizlabai.service.impl;

import com.lab24.quizlabai.model.Role;
import com.lab24.quizlabai.model.User;
import com.lab24.quizlabai.model.exceptions.EmailAlreadyExistsException;
import com.lab24.quizlabai.model.exceptions.InvalidArgumentsException;
import com.lab24.quizlabai.model.exceptions.PasswordsDoNotMatchException;
import com.lab24.quizlabai.model.exceptions.UsernameAlreadyExistsException;
import com.lab24.quizlabai.repository.UserRepository;
import com.lab24.quizlabai.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void register(String username, String password, String repeatPassword, String email, Role role) {
        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            throw new InvalidArgumentsException();
        }

        if (!password.equals(repeatPassword)) {
            throw new PasswordsDoNotMatchException();
        }

        if (this.userRepository.findByUsername(username).isPresent()) {
            throw new UsernameAlreadyExistsException(username);
        }

        if (this.userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException(email);
        }

        User user = new User(username, passwordEncoder.encode(password), email, role);

        userRepository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
    }
}


