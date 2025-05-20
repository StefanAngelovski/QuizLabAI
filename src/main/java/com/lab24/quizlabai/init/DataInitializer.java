package com.lab24.quizlabai.init;

import com.lab24.quizlabai.model.Professor;
import com.lab24.quizlabai.model.Role;
import com.lab24.quizlabai.repository.ProfessorRepository;
import com.lab24.quizlabai.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDefaultProfessors(UserRepository userRepository,
                                            ProfessorRepository professorRepository,
                                            PasswordEncoder passwordEncoder) {
        return args -> {
            for (int i = 1; i <= 3; i++) {
                String username = "Professor" + i;
                if (userRepository.findByUsername(username).isEmpty()) {
                    Professor professor = new Professor(username,
                            passwordEncoder.encode("password" + i),
                            "professor" + i + "@quizlab.ai");
                    professor.setRole(Role.ROLE_PROFESSOR);
                    professorRepository.save(professor);
                }
            }
        };
    }
}
