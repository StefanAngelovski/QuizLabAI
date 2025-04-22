package com.lab24.quizlabai.web.quizControllers;

import com.lab24.quizlabai.model.Question;
import com.lab24.quizlabai.model.Quiz;
import com.lab24.quizlabai.service.QuizService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
@RequestMapping("/attempt/review")
public class QuizReviewController {

    @Autowired
    private QuizService quizService;

    @GetMapping("/{quizId}")
    public String reviewQuiz(@PathVariable Long quizId, HttpSession session, Model model) {
        Quiz quiz = quizService.getQuizById(quizId);
        if (quiz == null) {
            return "redirect:/error";
        }

        List<Question> questions = quiz.getQuestions();
        Map<Long, List<String>> userAnswers = (Map<Long, List<String>>) session.getAttribute("userAnswers");
        if (userAnswers == null) {
            userAnswers = new HashMap<>();
        }

        Map<Long, Integer> questionScoreMap = new HashMap<>();
        for (Question question : questions) {
            int points = question.getPoints();
            List<String> answers = userAnswers.getOrDefault(question.getId(), new ArrayList<>());

            if ("multiple-choice".equals(question.getType())) {
                List<String> correctAnswers = Arrays.asList(question.getCorrectAnswer().split("\\s*,\\s*"));
                int correctCount = (int) answers.stream().filter(correctAnswers::contains).count();
                boolean exactMatch = correctCount == correctAnswers.size() && correctCount == answers.size();
                questionScoreMap.put(question.getId(), exactMatch ? points : 0);
            } else if ("short-answer".equals(question.getType()) && !answers.isEmpty()) {
                questionScoreMap.put(question.getId(),
                        answers.get(0).equalsIgnoreCase(question.getCorrectAnswer()) ? points : 0);
            } else {
                questionScoreMap.put(question.getId(), 0);
            }
        }

        model.addAttribute("quiz", quiz);
        model.addAttribute("quizQuestions", questions);
        model.addAttribute("userAnswers", userAnswers);
        model.addAttribute("quizId", quizId);
        model.addAttribute("questionScoreMap", questionScoreMap);

        return "quizReviewPage";
    }
}

