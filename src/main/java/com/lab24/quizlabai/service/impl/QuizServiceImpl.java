package com.lab24.quizlabai.service.impl;

import com.lab24.quizlabai.dto.QuizRequestDto;
import com.lab24.quizlabai.dto.QuizResponseDto;
import com.lab24.quizlabai.model.*;
import com.lab24.quizlabai.repository.QuizRepository;
import com.lab24.quizlabai.repository.SubjectRepository;
import com.lab24.quizlabai.repository.UserRepository;
import com.lab24.quizlabai.service.AzureAI.AzureOpenAIService;
import com.lab24.quizlabai.service.QuizService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class QuizServiceImpl implements QuizService {

    private final AzureOpenAIService azureOpenAIService;
    private final QuizRepository quizRepository;
    private final UserRepository userRepository;
    private final SubjectRepository subjectRepository;

    @Autowired
    public QuizServiceImpl(AzureOpenAIService azureOpenAIService,
                           QuizRepository quizRepository,
                           UserRepository userRepository,
                           SubjectRepository subjectRepository) {
        this.azureOpenAIService = azureOpenAIService;
        this.quizRepository = quizRepository;
        this.userRepository = userRepository;
        this.subjectRepository = subjectRepository;
    }

    @Override
    public QuizResponseDto generateQuiz(QuizRequestDto requestDto, MultipartFile file) throws IOException {
        Subject subject = subjectRepository.findById(requestDto.getSubjectId())
                .orElseThrow(() -> new EntityNotFoundException("Subject not found: " + requestDto.getSubjectId()));

        Quiz quiz = new Quiz();
        quiz.setSubject(subject);
        quiz.setTopic(requestDto.getTopic());
        quiz.setNumQuestions(requestDto.getNumQuestions());
        quiz.setQuizTime(requestDto.getQuizTime());
        quiz.setDifficulty(requestDto.getDifficulty());
        quiz.setQuestionTypes(requestDto.getQuestionTypes());
        quiz.setLanguage(requestDto.getLanguage());
        if (file != null && !file.isEmpty()) {
            quiz.setPdfFile(file.getBytes());
        }

        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = currentUser.getUsername();

        Professor professor = (Professor) userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Professor not found: " + username));
        quiz.setCreator(professor);

        List<Question> questions = azureOpenAIService.generateQuestions(quiz);
        quiz.setQuestions(questions);
        questions.forEach(q -> q.setQuiz(quiz));

        Quiz savedQuiz = quizRepository.save(quiz);

        QuizResponseDto responseDto = new QuizResponseDto();
        responseDto.setSubjectName(savedQuiz.getSubject().getName());
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
    public QuizResponseDto updateQuiz(Long id, QuizRequestDto requestDto, MultipartFile file) throws IOException {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = currentUser.getUsername();

        Quiz existing = quizRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found: " + id));

        if (!existing.getCreator().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only update quizzes you created.");
        }

        Subject subject = subjectRepository.findById(requestDto.getSubjectId())
                .orElseThrow(() -> new EntityNotFoundException("Subject not found: " + requestDto.getSubjectId()));

        existing.setSubject(subject);
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
        dto.setSubjectName(saved.getSubject().getName());
        dto.setTopic(saved.getTopic());
        dto.setNumQuestions(saved.getNumQuestions());
        dto.setQuizTime(saved.getQuizTime());
        dto.setDifficulty(saved.getDifficulty());
        dto.setQuestions(saved.getQuestions());
        dto.setLanguage(saved.getLanguage());
        return dto;
    }


    @Override
    public void deleteQuiz(Long id) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username = currentUser.getUsername();

        Quiz quiz = quizRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found: " + id));

        if (!quiz.getCreator().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only delete quizzes you created.");
        }

        quizRepository.deleteById(id);
    }

    @Override
    public List<Quiz> getQuizzesByCreator(Professor professor) {
        return quizRepository.findByCreator(professor);
    }

    @Transactional
    public List<Quiz> getQuizzesForStudent(Student student) {
        List<Quiz> quizzes = new ArrayList<>();
        for (Subject subject : student.getEnrolledSubjects()) {
            quizzes.addAll(quizRepository.findBySubject(subject));
        }
        return quizzes;
    }
}
