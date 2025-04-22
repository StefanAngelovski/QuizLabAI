package com.lab24.quizlabai.service.impl;


import com.lab24.quizlabai.dto.QuizRequestDto;
import com.lab24.quizlabai.dto.QuizResponseDto;
import com.lab24.quizlabai.model.Question;

import com.lab24.quizlabai.model.Quiz;
import com.lab24.quizlabai.repository.QuizRepository;
import com.lab24.quizlabai.service.AzureAI.AzureOpenAIService;
import com.lab24.quizlabai.service.QuizService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
        }
        return quiz;
    }
}
