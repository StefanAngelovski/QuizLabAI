package com.lab24.quizlabai.service;

import com.lab24.quizlabai.model.Role;
import com.lab24.quizlabai.model.User;
import com.lab24.quizlabai.model.exceptions.MaximumFileSizeException;
import com.lab24.quizlabai.model.exceptions.ProfilePictureNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserService extends UserDetailsService {
    void register(String username, String password, String repeatPassword, String email, Role role);
    void updateUser(User existingUser, User updatedUser, MultipartFile image) throws MaximumFileSizeException;
    void removeProfileImage(Long id);
    byte[] getProfileImage(Long id) throws ProfilePictureNotFoundException;
    List<User> findAllStudents();
}



