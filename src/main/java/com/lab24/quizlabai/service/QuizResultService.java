package com.lab24.quizlabai.service;

import com.lab24.quizlabai.model.Quiz;
import com.lab24.quizlabai.model.QuizResult;
import com.lab24.quizlabai.model.Student;
import com.lab24.quizlabai.model.User;

import java.util.List;

public interface QuizResultService {

    List<QuizResult> getCompletedQuizResultsForStudent(Student student);

    List<QuizResult> getAllResultsByStudent(Student student);

    QuizResult findResultById(Long id);

    QuizResult saveResult(Quiz quiz, User user, Double score, Double timeTaken);
}
