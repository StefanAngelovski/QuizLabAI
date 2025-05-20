package com.lab24.quizlabai.service;

import com.lab24.quizlabai.model.*;

import java.util.List;

public interface QuizResultService {

    List<QuizResult> getCompletedQuizResultsForStudent(Student student);
    List<QuizResult> getQuizResultsByQuiz(Quiz quiz);
    void saveResult(Quiz quiz, User user, Double score, Double timeTaken);
}
