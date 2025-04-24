package com.lab24.quizlabai.service;

import com.lab24.quizlabai.dto.QuizRequestDto;
import com.lab24.quizlabai.dto.QuizResponseDto;
import com.lab24.quizlabai.model.Quiz;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface QuizService {

    QuizResponseDto generateQuiz(QuizRequestDto requestDto, MultipartFile file) throws IOException;

    List<Quiz> getAllQuizzes();

    Quiz getQuizById(Long quizId);

    QuizResponseDto updateQuiz(Long id, QuizRequestDto requestDto, MultipartFile file) throws IOException;

    void deleteQuiz(Long id);
}
