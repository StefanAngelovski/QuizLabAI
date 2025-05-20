package com.lab24.quizlabai.service;

import com.lab24.quizlabai.model.User;

public interface AuthService {

    User login(String username, String password);

}
