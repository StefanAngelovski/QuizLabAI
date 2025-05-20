package com.lab24.quizlabai.repository;

import com.lab24.quizlabai.model.QuizResult;
import com.lab24.quizlabai.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {
    List<QuizResult> findByQuizId(Long quizId);
    List<QuizResult> findByUserAndCompletedTrue(User user);
    List<QuizResult> findByUser(User user);
}
