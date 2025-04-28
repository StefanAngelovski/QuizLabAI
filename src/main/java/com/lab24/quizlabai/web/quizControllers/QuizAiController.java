package com.lab24.quizlabai.web.quizControllers;

import com.lab24.quizlabai.model.EvaluationResult;
import com.lab24.quizlabai.model.Question;
import com.lab24.quizlabai.model.Quiz;
import com.lab24.quizlabai.service.AzureAI.AzureOpenAIService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/attempt/ai")
public class QuizAiController {

    @Autowired
    private AzureOpenAIService azureOpenAIService;

    @PostMapping("/submit")
    public String submitAndCheckWithAI(@RequestParam(value = "selectedOptions", required = false) List<String> selectedOptions,
                                       @RequestParam(value = "shortAnswer", required = false) String shortAnswer,
                                       HttpSession session, Model model) {

        Map<Long, List<String>> userAnswers = (Map<Long, List<String>>) session.getAttribute("userAnswers");
        Integer score = (Integer) session.getAttribute("score");
        List<Question> questions = (List<Question>) session.getAttribute("quizQuestions");
        Quiz quiz = (Quiz) session.getAttribute("quiz");

        if (userAnswers == null || questions == null || quiz == null) {
            model.addAttribute("error", "Session data is missing.");
            return "errorPage";
        }

        String timeTaken = (String) session.getAttribute("timeTaken");
        if (timeTaken == null) {
            Long startTime = (Long) session.getAttribute("startTime");
            if (startTime != null) {
                long duration = (System.currentTimeMillis() - startTime) / 1000;
                timeTaken = String.format("%d minutes %d seconds", duration / 60, duration % 60);
                session.setAttribute("timeTaken", timeTaken);
            } else {
                model.addAttribute("error", "Quiz start time is missing.");
                return "errorPage";
            }
        }

        int quizTime = quiz.getQuizTime();
        int totalQuestions = questions.size();
        int answeredQuestions = (int) userAnswers.values().stream().filter(answers -> !answers.isEmpty()).count();

        String aiEvaluationResults = azureOpenAIService.evaluateAnswersWithAI(questions, userAnswers);
        List<EvaluationResult> evaluationResults = parseEvaluationResults(aiEvaluationResults);

        model.addAttribute("evaluationResult", evaluationResults);
        model.addAttribute("score", score);
        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", questions);
        model.addAttribute("quizId", quiz.getId());
        model.addAttribute("quizTime", quizTime);
        model.addAttribute("timeTaken", timeTaken);
        model.addAttribute("answeredQuestions", answeredQuestions);
        model.addAttribute("totalQuestions", totalQuestions);

        return "quizResultAiPage";
    }

    private List<EvaluationResult> parseEvaluationResults(String aiEvaluationResults) {
        List<EvaluationResult> results = new ArrayList<>();
        if (aiEvaluationResults == null || aiEvaluationResults.isEmpty()) {
            return results;
        }

        String[] questionBlocks = aiEvaluationResults.split("\\n\\n+");

        for (String block : questionBlocks) {
            EvaluationResult result = new EvaluationResult();
            String[] lines = block.split("\\n");

            for (String line : lines) {
                if (line.startsWith("Question:")) {
                    result.setQuestionText(line.substring("Question:".length()).trim());
                } else if (line.startsWith("User's Answer:")) {
                    result.setUserAnswer(line.substring("User's Answer:".length()).trim());
                } else if (line.startsWith("Correct Answer:")) {
                    result.setCorrectAnswer(line.substring("Correct Answer:".length()).trim());
                } else if (line.startsWith("Feedback:")) {
                    String feedback = line.substring("Feedback:".length()).trim();
                    result.setExplanation(feedback);
                    result.setCorrect(feedback.toLowerCase().contains("correct") && !feedback.toLowerCase().contains("incorrect"));
                }
            }

            if (result.getQuestionText() != null) {
                results.add(result);
            }
        }
        return results;
    }
}
