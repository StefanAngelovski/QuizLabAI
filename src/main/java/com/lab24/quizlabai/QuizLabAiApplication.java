package com.lab24.quizlabai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@Configuration
@SpringBootApplication
@RestController
public class QuizLabAiApplication {


    @GetMapping("/message")
    public String message() {
        return "Hello from QuizLabAI!";
    }
    
    public static void main(String[] args) {
        SpringApplication.run(QuizLabAiApplication.class, args);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }


}
