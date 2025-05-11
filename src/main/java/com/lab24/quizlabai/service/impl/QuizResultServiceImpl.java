package com.lab24.quizlabai.service.impl;

import com.lab24.quizlabai.model.Quiz;
import com.lab24.quizlabai.model.QuizResult;
import com.lab24.quizlabai.model.Student;
import com.lab24.quizlabai.model.User;
import com.lab24.quizlabai.repository.QuizResultRepository;
import com.lab24.quizlabai.service.QuizResultService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class QuizResultServiceImpl implements QuizResultService {

    private final QuizResultRepository quizResultRepository;

    public QuizResultServiceImpl(QuizResultRepository quizResultRepository) {
        this.quizResultRepository = quizResultRepository;
    }

    @Override
    @Transactional
    public List<QuizResult> getCompletedQuizResultsForStudent(Student student) {
        return quizResultRepository.findByUserAndCompletedTrue(student);
    }

    @Override
    @Transactional
    public List<QuizResult> getQuizResultsByQuiz(Quiz quiz) {
        return quizResultRepository.findByQuizId(quiz.getId());
    }


    @Override
    public void saveResult(Quiz quiz, User user, Double score, Double timeTaken) {
        QuizResult result = new QuizResult();
        result.setQuiz(quiz);
        result.setUser(user);
        result.setScore(score);
        result.setCompleted(true);
        result.setTimeSpent(timeTaken);
        quizResultRepository.save(result);
    }
}