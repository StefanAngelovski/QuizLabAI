package com.lab24.quizlabai.web.quizControllers;

import com.lab24.quizlabai.model.Question;
import com.lab24.quizlabai.model.Quiz;
import com.lab24.quizlabai.model.QuizResult;
import com.lab24.quizlabai.model.QuizStatistics;
import com.lab24.quizlabai.service.AzureAI.AzureOpenAIService;
import com.lab24.quizlabai.service.QuizResultService;
import com.lab24.quizlabai.service.QuizService;
import com.lab24.quizlabai.service.QuizStatisticsService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/attempt/result")
public class QuizResultController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private QuizStatisticsService quizStatisticsService;

    @Autowired
    private QuizResultService quizResultService;

    @Autowired
    private AzureOpenAIService azureOpenAIService;


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
        int answeredQuestions = userAnswers != null ? (int) userAnswers.values().stream().filter(ans -> !ans.isEmpty()).count() : 0;

        String timeTaken = (String) session.getAttribute("timeTaken");
        if (timeTaken == null) {
            long startTime = (Long) session.getAttribute("startTime");
            long endTime = System.currentTimeMillis();
            long timeDifference = endTime - startTime;
            long timeInSeconds = timeDifference / 1000;
            timeTaken = String.format("%d minutes %d seconds", timeInSeconds / 60, timeInSeconds % 60);
            session.setAttribute("timeTaken", timeTaken);
        }

        int quizTime = quiz.getQuizTime();
        int totalQuestions = quiz.getQuestions().size();

        model.addAttribute("answeredQuestions", answeredQuestions);
        model.addAttribute("score", score);
        model.addAttribute("totalPoints", totalPoints);
        model.addAttribute("percentage", percentage);
        model.addAttribute("quiz", quiz);
        model.addAttribute("quizTime", quizTime);
        model.addAttribute("timeTaken", timeTaken);
        model.addAttribute("totalQuestions", totalQuestions);

        return "quizResultPage";
    }

    @GetMapping("/{quizId}/statistics")
    public String showQuizStatistics(@PathVariable Long quizId, Model model) {
        QuizStatistics quizStatistics = quizStatisticsService.calculateStatistics(quizId);
        model.addAttribute("quizStatistics", quizStatistics);

        return "quizStatistics";
    }
}
