package com.lab24.quizlabai.web.quizControllers;

import com.lab24.quizlabai.model.Quiz;
import com.lab24.quizlabai.model.QuizStatistics;
import com.lab24.quizlabai.service.QuizService;
import com.lab24.quizlabai.service.QuizStatisticsService;
import com.lab24.quizlabai.service.impl.QuizStatisticsServiceImpl;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/attempt/result")
public class QuizResultController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private QuizStatisticsService quizStatisticsService;

    @GetMapping("/{quizId}")
    public String showResult(@PathVariable Long quizId, HttpSession session, Model model) {
        Integer score = (Integer) session.getAttribute("score");
        Integer totalPoints = (Integer) session.getAttribute("totalPoints");
        Quiz quiz = quizService.getQuizById(quizId);

        if (quiz == null) {
            return "redirect:/error";
        }

        double percentage = (score.doubleValue() / totalPoints) * 100;
        Map<Long, List<String>> userAnswers = (Map<Long, List<String>>) session.getAttribute("userAnswers");
        int answeredQuestions = (int) userAnswers.values().stream().filter(ans -> !ans.isEmpty()).count();

        model.addAttribute("answeredQuestions", answeredQuestions);
        model.addAttribute("score", score);
        model.addAttribute("totalPoints", totalPoints);
        model.addAttribute("percentage", percentage);
        model.addAttribute("quiz", quiz);

        return "quizResultPage";
    }

    @GetMapping("/{quizId}/statistics")
        public String showQuizStatistics(@PathVariable Long quizId, Model model) {
            QuizStatistics quizStatistics = quizStatisticsService.calculateStatistics(quizId);

            model.addAttribute("quizStatistics", quizStatistics);

            return "quizStatistics";
        }
    }

