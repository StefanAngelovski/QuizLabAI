package com.lab24.quizlabai.repository;

import com.lab24.quizlabai.model.Role;
import com.lab24.quizlabai.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(Role role);
    Optional<User> findByUsernameAndPassword(String username, String password);
}

