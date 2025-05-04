package com.lab24.quizlabai.web.quizControllers;

import com.lab24.quizlabai.dto.QuizRequestDto;
import com.lab24.quizlabai.dto.QuizResponseDto;
import com.lab24.quizlabai.model.Quiz;
import com.lab24.quizlabai.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
public class QuizRestController {

    private final QuizService quizService;

    @Autowired
    public QuizRestController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping("/generate")
    public ResponseEntity<QuizResponseDto> generateQuiz(@ModelAttribute QuizRequestDto requestDto,
                                                        @RequestParam(value = "pdfFile", required = false) MultipartFile file) throws IOException {

        QuizResponseDto quizResponse = quizService.generateQuiz(requestDto, file);


        if (quizResponse != null) {
            return ResponseEntity.ok(quizResponse);
        } else {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<Quiz>> getAllQuizzes() {
        return ResponseEntity.ok(quizService.getAllQuizzes());
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<Quiz> getQuizById(@PathVariable Long quizId) {
        Quiz quiz = quizService.getQuizById(quizId);
        return quiz != null ? ResponseEntity.ok(quiz) : ResponseEntity.notFound().build();
    }

}