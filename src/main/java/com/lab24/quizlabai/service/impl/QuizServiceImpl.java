package com.lab24.quizlabai.service.impl;


import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.HttpResponse;
import com.lab24.quizlabai.dto.QuizRequestDto;
import com.lab24.quizlabai.dto.QuizResponseDto;
import com.lab24.quizlabai.model.Question;

import com.lab24.quizlabai.model.Quiz;
import com.lab24.quizlabai.repository.QuizRepository;
import com.lab24.quizlabai.service.AzureAI.AzureOpenAIService;
import com.lab24.quizlabai.service.QuizService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

@Service
public class QuizServiceImpl implements QuizService {

    private final AzureOpenAIService azureOpenAIService;
    private final QuizRepository quizRepository;

    @Autowired
    public QuizServiceImpl(AzureOpenAIService azureOpenAIService,
                           QuizRepository quizRepository) {
        this.azureOpenAIService = azureOpenAIService;
        this.quizRepository = quizRepository;
    }

    @Override
    public QuizResponseDto generateQuiz(QuizRequestDto requestDto, MultipartFile file) throws IOException {
        Quiz quiz = new Quiz();
        quiz.setSubject(requestDto.getSubject());
        quiz.setTopic(requestDto.getTopic());
        quiz.setNumQuestions(requestDto.getNumQuestions());
        quiz.setQuizTime(requestDto.getQuizTime());
        quiz.setDifficulty(requestDto.getDifficulty());
        quiz.setQuestionTypes(requestDto.getQuestionTypes());
        quiz.setLanguage(requestDto.getLanguage());
        if (file != null && !file.isEmpty()) {
            quiz.setPdfFile(file.getBytes());
        }


        List<Question> questions = azureOpenAIService.generateQuestions(quiz);

        quiz.setQuestions(questions);
        questions.forEach(q -> q.setQuiz(quiz));

        Quiz savedQuiz = quizRepository.save(quiz);


        QuizResponseDto responseDto = new QuizResponseDto();
        responseDto.setSubject(savedQuiz.getSubject());
        responseDto.setTopic(savedQuiz.getTopic());
        responseDto.setNumQuestions(savedQuiz.getNumQuestions());
        responseDto.setQuizTime(savedQuiz.getQuizTime());
        responseDto.setDifficulty(savedQuiz.getDifficulty());
        responseDto.setQuestions(savedQuiz.getQuestions());
        responseDto.setLanguage(savedQuiz.getLanguage());
        return responseDto;
    }

    @Override
    public List<Quiz> getAllQuizzes() {
        return quizRepository.findAll();
    }

    @Override
    @Transactional
    public Quiz getQuizById(Long quizId) {
        Quiz quiz = quizRepository.findById(quizId).orElse(null);
        if (quiz != null) {

            quiz.getQuestions().forEach(q -> q.getOptions().size());

            if (quiz.getQuestionTypes() != null) {
                quiz.getQuestionTypes().size();
            }
        }
        return quiz;
    }

    @Override
    public QuizResponseDto updateQuiz(Long id,
                                      QuizRequestDto requestDto,
                                      MultipartFile file) throws IOException {

        Quiz existing = quizRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found: " + id));

        existing.setSubject(requestDto.getSubject());
        existing.setTopic(requestDto.getTopic());
        existing.setNumQuestions(requestDto.getNumQuestions());
        existing.setQuizTime(requestDto.getQuizTime());
        existing.setDifficulty(requestDto.getDifficulty());
        existing.setQuestionTypes(requestDto.getQuestionTypes());
        existing.setLanguage(requestDto.getLanguage());
        if (file != null && !file.isEmpty()) {
            existing.setPdfFile(file.getBytes());
        }

        Quiz saved = quizRepository.save(existing);
        QuizResponseDto dto = new QuizResponseDto();
        dto.setSubject(saved.getSubject());
        dto.setTopic(saved.getTopic());
        dto.setNumQuestions(saved.getNumQuestions());
        dto.setQuizTime(saved.getQuizTime());
        dto.setDifficulty(saved.getDifficulty());
        dto.setQuestions(saved.getQuestions());
        dto.setLanguage(saved.getLanguage());
        return dto;
    }


    public void deleteQuiz(Long id) {
        quizRepository.deleteById(id);
    }
}
