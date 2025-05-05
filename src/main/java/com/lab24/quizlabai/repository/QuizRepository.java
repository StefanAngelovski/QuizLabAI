package com.lab24.quizlabai.repository;

import com.lab24.quizlabai.model.Professor;
import com.lab24.quizlabai.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findByCreator(Professor creator);
}
