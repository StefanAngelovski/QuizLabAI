package com.lab24.quizlabai.service.impl;

import com.lab24.quizlabai.model.Professor;
import com.lab24.quizlabai.model.Role;
import com.lab24.quizlabai.model.Student;
import com.lab24.quizlabai.model.User;
import com.lab24.quizlabai.model.exceptions.*;
import com.lab24.quizlabai.repository.ProfessorRepository;
import com.lab24.quizlabai.repository.StudentRepository;
import com.lab24.quizlabai.repository.UserRepository;
import com.lab24.quizlabai.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ProfessorRepository professorRepository;
    private final PasswordEncoder passwordEncoder;
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB

    public UserServiceImpl(UserRepository userRepository,
                           StudentRepository studentRepository,
                           ProfessorRepository professorRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.professorRepository = professorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
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

        User user;
        switch (role) {
            case ROLE_STUDENT:
                Student student = new Student(username, passwordEncoder.encode(password), email);
                user = studentRepository.save(student);
                break;
            case ROLE_PROFESSOR:
                Professor professor = new Professor(username, passwordEncoder.encode(password), email);
                user = professorRepository.save(professor);
                break;
            default:
                user = new User(username, passwordEncoder.encode(password), email, role);
                user = userRepository.save(user);
        }
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException(username));
    }

    @Override
    public void updateUser(User existingUser, User updatedUser, MultipartFile image) throws MaximumFileSizeException {
        if (!existingUser.getUsername().equals(updatedUser.getUsername())) {
            Optional<User> userByUsername = userRepository.findByUsername(updatedUser.getUsername());
            if (userByUsername.isPresent() && !userByUsername.get().getId().equals(existingUser.getId())) {
                throw new UsernameAlreadyExistsException(updatedUser.getUsername());
            }
        }

        if (!existingUser.getEmail().equals(updatedUser.getEmail())) {
            Optional<User> userByEmail = userRepository.findByEmail(updatedUser.getEmail());
            if (userByEmail.isPresent() && !userByEmail.get().getId().equals(existingUser.getId())) {
                throw new EmailAlreadyExistsException(updatedUser.getEmail());
            }
        }

        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setEmail(updatedUser.getEmail());

        if (image != null && !image.isEmpty()) {
            if (image.getSize() > MAX_FILE_SIZE) {
                throw new MaximumFileSizeException(2);
            }
            try {
                existingUser.setProfileImage(image.getBytes());
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload image", e);
            }
        }

        userRepository.save(existingUser);

        Authentication authentication = new UsernamePasswordAuthenticationToken(existingUser, null, existingUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Override
    public void removeProfileImage(Long id) {
        Optional<User> optionalUser = userRepository.findById(Long.valueOf(id));
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setProfileImage(null);
            userRepository.save(user);
        }
    }

    @Override
    public byte[] getProfileImage(Long id) throws ProfilePictureNotFoundException {
        return userRepository.findById(Long.valueOf(id))
                .filter(user -> user.getProfileImage() != null)
                .map(User::getProfileImage)
                .orElseThrow(ProfilePictureNotFoundException::new);
    }

    @Override
    public List<User> findAllStudents() {
        return userRepository.findByRole(Role.ROLE_STUDENT);
    }

}
