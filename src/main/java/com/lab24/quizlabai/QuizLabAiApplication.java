package com.lab24.quizlabai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

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

}
