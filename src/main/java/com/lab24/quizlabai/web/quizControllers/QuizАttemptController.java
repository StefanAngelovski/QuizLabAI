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

import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/attempt")
public class QuizАttemptController {

    @Autowired
    private QuizService quizService;

    @GetMapping("/{quizId}")
    public String startQuiz(@PathVariable Long quizId, Model model, HttpSession session) {
        Quiz quiz = quizService.getQuizById(quizId);
        if (quiz == null) return "redirect:/error";

        List<Question> questions = quiz.getQuestions();
        session.setAttribute("quizQuestions", questions);
        session.setAttribute("currentQuestionIndex", 0);
        session.setAttribute("quizId", quizId);
        session.setAttribute("score", 0);
        session.setAttribute("quiz", quiz);
        session.setAttribute("totalPoints", questions.stream().mapToInt(Question::getPoints).sum());
        session.setAttribute("userAnswers", new HashMap<Long, List<String>>());
        session.setAttribute("quizTime", quiz.getQuizTime());


        session.setAttribute("startTime", System.currentTimeMillis());

        model.addAttribute("question", questions.get(0));
        model.addAttribute("quiz", quiz);
        model.addAttribute("questionNumber", 1);
        model.addAttribute("totalQuestions", questions.size());
        model.addAttribute("isLastQuestion", questions.size() == 1);

        return "quizPage1";
    }
}

