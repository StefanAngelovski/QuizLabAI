package com.lab24.quizlabai.web.quizControllers;

import com.lab24.quizlabai.model.Question;
import com.lab24.quizlabai.service.QuizService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;

@Controller
@RequestMapping("/attempt")
public class QuizNavigationController {

    @Autowired
    private QuizService quizService;

    @PostMapping("/next")
    public String nextQuestion(@RequestParam(value = "selectedOptions", required = false) List<String> selectedOptions,
                               @RequestParam(value = "shortAnswer", required = false) String shortAnswer,
                               HttpSession session,
                               Model model) {
        processAnswer(session, selectedOptions, shortAnswer);

        List<Question> questions = (List<Question>) session.getAttribute("quizQuestions");
        Integer index = (Integer) session.getAttribute("currentQuestionIndex");

        index++;
        if (index >= questions.size()) {
            return "redirect:/attempt/result/" + session.getAttribute("quizId");
        }

        session.setAttribute("currentQuestionIndex", index);
        model.addAttribute("question", questions.get(index));
        model.addAttribute("quiz", session.getAttribute("quiz"));
        model.addAttribute("questionNumber", index + 1);
        model.addAttribute("totalQuestions", questions.size());
        model.addAttribute("isLastQuestion", index == questions.size() - 1);

        return "quizPage1";
    }

    @PostMapping("/result/{quizId}")
    public String submitLastQuestion(@RequestParam(value = "selectedOptions", required = false) List<String> selectedOptions,
                                     @RequestParam(value = "shortAnswer", required = false) String shortAnswer,
                                     @PathVariable Long quizId, HttpSession session) {
        processAnswer(session, selectedOptions, shortAnswer);
        return "redirect:/attempt/result/" + quizId;
    }

    private void processAnswer(HttpSession session, List<String> selectedOptions, String shortAnswer) {
        List<Question> questions = (List<Question>) session.getAttribute("quizQuestions");
        Integer index = (Integer) session.getAttribute("currentQuestionIndex");
        Question currentQuestion = questions.get(index);
        Integer currentScore = (Integer) session.getAttribute("score");
        Map<Long, List<String>> userAnswers = (Map<Long, List<String>>) session.getAttribute("userAnswers");

        if (userAnswers == null) userAnswers = new HashMap<>();

        if ("multiple-choice".equals(currentQuestion.getType())) {
            if (selectedOptions == null) selectedOptions = new ArrayList<>();
            userAnswers.put(currentQuestion.getId(), selectedOptions);

            List<String> correctAnswers = Arrays.asList(currentQuestion.getCorrectAnswer().split("\\s*,\\s*"));

            if (selectedOptions.containsAll(correctAnswers) && correctAnswers.containsAll(selectedOptions)) {
                session.setAttribute("score", currentScore + currentQuestion.getPoints());
            }

        } else if ("short-answer".equals(currentQuestion.getType())) {
            List<String> answerList = new ArrayList<>();
            if (shortAnswer != null && !shortAnswer.trim().isEmpty()) {
                answerList.add(shortAnswer.trim());
            }
            userAnswers.put(currentQuestion.getId(), answerList);

            if (!answerList.isEmpty() && answerList.get(0).equalsIgnoreCase(currentQuestion.getCorrectAnswer())) {
                session.setAttribute("score", currentScore + currentQuestion.getPoints());
            }
        }

        session.setAttribute("userAnswers", userAnswers);
    }
}

