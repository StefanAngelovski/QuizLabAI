package com.lab24.quizlabai.web.quizControllers;

import com.lab24.quizlabai.model.Quiz;
import com.lab24.quizlabai.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class QuizWebController {

    private final QuizService quizService;

    @Autowired
    public QuizWebController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping("/quiz-attempt/{id}")
    public String attemptQuiz(@PathVariable Long id, Model model) {
        Quiz quiz = quizService.getQuizById(id);
        if (quiz == null) {
            return "redirect:/error";
        }
        model.addAttribute("quiz", quiz);
        return "quizAttempt";
    }
}
